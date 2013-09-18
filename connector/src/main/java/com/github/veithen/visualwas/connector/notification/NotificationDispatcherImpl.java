package com.github.veithen.visualwas.connector.notification;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

final class NotificationDispatcherImpl implements NotificationDispatcher, Runnable {
    private static final Log log = LogFactory.getLog(NotificationDispatcherImpl.class);
    
    private static final int MIN_DELAY = 2000;
    private static final int MAX_DELAY = 30000;
    
    private final RemoteNotificationService service;
    private final boolean autoReregister;
    private final List<NotificationListenerRegistration> registrations = new LinkedList<NotificationListenerRegistration>();
    // We use a single subscription for all listeners (to avoid blocking multiple threads on the server side)
    private SubscriptionInfo subscriptionInfo = new SubscriptionInfo();
    private SubscriptionHandle subscriptionHandle;
    private int delay = MIN_DELAY;

    NotificationDispatcherImpl(RemoteNotificationService service, boolean autoReregister) {
        this.service = service;
        this.autoReregister = autoReregister;
    }

    @Override
    public synchronized void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws IOException {
        boolean subscribed = !registrations.isEmpty();
        NotificationSelector selector = new NotificationSelector(name, filter);
        registrations.add(new NotificationListenerRegistration(selector, listener, handback));
        subscriptionInfo.addSelector(selector);
        if (subscribed) {
            try {
                service.updateSubscription(subscriptionHandle, subscriptionInfo);
            } catch (SubscriptionNotFoundException ex) {
                if (!autoReregister) {
                    subscriptionInfo = new SubscriptionInfo();
                    subscriptionInfo.addSelector(selector);
                }
                subscriptionHandle = service.addSubscription(subscriptionInfo, null);
            }
        } else {
            subscriptionHandle = service.addSubscription(subscriptionInfo, null);
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        boolean doDelay = false;
        while (true) {
            if (doDelay) {
                delay = Math.min(delay*2, MAX_DELAY);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                    break;
                }
            }
            SubscriptionHandle subscriptionHandle;
            synchronized (this) {
                subscriptionHandle = this.subscriptionHandle;
            }
            if (subscriptionHandle == null) {
                break;
            }
            try {
                Notification[] notifications = service.pullNotifications(subscriptionHandle, 20);
                synchronized (this) {
                    // TODO: listeners should be invoked asynchronously
                    for (Notification notification : notifications) {
                        for (NotificationListenerRegistration registration : registrations) {
                            if (registration.getSelector().isNotificationEnabled(notification)) {
                                registration.handleNotification(notification);
                            }
                        }
                    }
                }
                if (notifications.length > 0) {
                    doDelay = false;
                    // Reset delay for next time
                    delay = MIN_DELAY;
                } else {
                    doDelay = true;
                }
            } catch (SubscriptionNotFoundException ex) {
                synchronized (this) {
                    // If the handle changed, then somebody else has reregistered the subscription already
                    if (subscriptionHandle == this.subscriptionHandle) {
                        if (autoReregister) {
                            try {
                                this.subscriptionHandle = service.addSubscription(subscriptionInfo, null);
                            } catch (IOException ex2) {
                                log.warn("Failed to renew subscription " + subscriptionHandle, ex2);
                            }
                        } else {
                            this.subscriptionHandle = null;
                        }
                    }
                }
            } catch (IOException ex) {
                log.warn("Failed to retrieve notifications for subscription " + subscriptionHandle, ex);
                doDelay = true;
            }
        }
    }
}