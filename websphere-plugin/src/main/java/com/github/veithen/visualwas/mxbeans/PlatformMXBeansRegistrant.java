/*
 * #%L
 * VisualWAS
 * %%
 * Copyright (C) 2013 - 2018 Andreas Veithen
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package com.github.veithen.visualwas.mxbeans;

import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import com.ibm.websphere.management.AdminService;
import com.ibm.websphere.management.AdminServiceFactory;
import com.ibm.ws.management.PlatformMBeanServer;
import com.ibm.ws.security.service.SecurityService;
import com.ibm.wsspi.runtime.component.WsComponent;
import com.ibm.wsspi.runtime.service.WsServiceRegistry;

public final class PlatformMXBeansRegistrant implements WsComponent {
    private static final Logger log = Logger.getLogger(PlatformMXBeansRegistrant.class.getName());
    
    private String state;
    
    /**
     * The MBean server where the platform MXBeans are registered. This will be <code>null</code> if
     * the MBean server can't be located during startup.
     */
    private MBeanServer mbs;
    
    private AccessChecker accessChecker;
    
    /**
     * The list of MBeans registered by {@link #start()}.
     */
    private final List<ObjectName> registeredMBeans = new ArrayList<ObjectName>();
    
    public String getName() {
        return "PlatformMXBeansRegistrant";
    }

    public String getState() {
        return state;
    }

    public void initialize(Object config) {
        state = INITIALIZED;
    }

    public void start() {
        state = STARTING;
        SecurityService securityService = (SecurityService)WsServiceRegistry.getService(this, SecurityService.class);
        MBeanServer wasMBeanServer = AdminServiceFactory.getMBeanFactory().getMBeanServer();
        if (log.isLoggable(Level.FINE)) {
            log.fine("AdminServiceFactory.getMBeanFactory().getMBeanServer() returned an instance of type "
                    + wasMBeanServer.getClass().getName());
        }
        if (wasMBeanServer instanceof PlatformMBeanServer) {
            // The PlatformMBeanServer instance automatically adds the cell, node and
            // process as key properties. This will not work for the platform MXBeans
            // (VisualVM would be unable to identify them). However,
            // PlatformMBeanServer is just a wrapper around a standard MBeanServer,
            // which can be retrieved using the getDefaultMBeanServer.
            mbs = ((PlatformMBeanServer)wasMBeanServer).getDefaultMBeanServer();
        } else {
            log.warning("The MBeanServer returned by MBeanFactory#getMBeanServer() is not an instance of "
                    + PlatformMBeanServer.class.getName() + "; instead it is an instance of "
                    + wasMBeanServer.getClass().getName());
            mbs = wasMBeanServer;
        }
        Authorizer authorizer;
        if (securityService.isSecurityEnabled()) {
            AdminService adminService = AdminServiceFactory.getAdminService();
            // See http://publib.boulder.ibm.com/infocenter/wasinfo/v6r1/topic/com.ibm.websphere.express.doc/info/exp/ae/tjmx_admin_finegr_mbsec.html
            String resource = "/nodes/" + adminService.getNodeName() + "/servers/" + adminService.getProcessName();
            if (log.isLoggable(Level.FINE)) {
                log.fine("resource = " + resource);
            }
            authorizer = new AuthorizerImpl(resource);
            log.info("MXBean access control enabled");
        } else {
            authorizer = new NoAuthorizer();
            log.info("MXBean access control not enabled");
        }
        log.fine("Configuring access rules for platform MXBeans");
        Properties accessProperties = new Properties();
        try {
            accessProperties.load(PlatformMXBeansRegistrant.class.getResourceAsStream("access.properties"));
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Failed to load access rules", ex);
        }
        Map<String,String> accessRules = new HashMap<String,String>();
        for (Map.Entry<Object,Object> entry : accessProperties.entrySet()) {
            accessRules.put((String)entry.getKey(), (String)entry.getValue());
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("accessRules = " + accessRules);
        }
        accessChecker = new AccessChecker(authorizer, accessRules);
        registerMBean(
                ManagementFactory.getClassLoadingMXBean(),
                ClassLoadingMXBean.class,
                ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
        registerMBean(
                ManagementFactory.getMemoryMXBean(),
                MemoryMXBean.class,
                ManagementFactory.MEMORY_MXBEAN_NAME);
        registerMBean(
                ManagementFactory.getThreadMXBean(),
                ThreadMXBean.class,
                ManagementFactory.THREAD_MXBEAN_NAME);
        registerMBean(
                ManagementFactory.getRuntimeMXBean(),
                RuntimeMXBean.class,
                ManagementFactory.RUNTIME_MXBEAN_NAME);
        registerMBean(
                ManagementFactory.getOperatingSystemMXBean(),
                OperatingSystemMXBean.class,
                ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);
        registerMBean(
                ManagementFactory.getCompilationMXBean(),
                CompilationMXBean.class,
                ManagementFactory.COMPILATION_MXBEAN_NAME);
        for (GarbageCollectorMXBean mbean : ManagementFactory.getGarbageCollectorMXBeans()) {
            registerMBean(
                    mbean,
                    GarbageCollectorMXBean.class,
                    ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",name=" + mbean.getName());
        }
        for (MemoryManagerMXBean mbean : ManagementFactory.getMemoryManagerMXBeans()) {
            registerMBean(
                    mbean,
                    MemoryManagerMXBean.class,
                    ManagementFactory.MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE + ",name=" + mbean.getName());
        }
        for (MemoryPoolMXBean mbean : ManagementFactory.getMemoryPoolMXBeans()) {
            registerMBean(
                    mbean,
                    MemoryPoolMXBean.class,
                    ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE + ",name=" + mbean.getName());
        }
        log.info("Registered " + registeredMBeans.size() + " platform MXBeans");
        state = STARTED;
    }
    
    /**
     * Register the given MBean and update {@link #registeredMBeans}.
     * 
     * @param object the MBean instance
     * @param name the MBean name
     */
    private <T> void registerMBean(T object, Class<T> iface, String name) {
        try {
            ObjectName objectName = new ObjectName(name);
            registeredMBeans.add(mbs.registerMBean(
                    new AccessControlProxy(
                            new StandardMBean(object, iface, true),
                            objectName.getKeyProperty("type"),
                            accessChecker),
                    objectName).getObjectName());
            if (log.isLoggable(Level.FINE)) {
                log.fine("Registered MBean " + name + " (type " + object.getClass().getName() + ")");
            }
        } catch (JMException ex) {
            log.severe("Failed to register MBean " + name + ": " + ex.getMessage());
        }
    }

    public void stop() {
        state = STOPPING;
        for (ObjectName name : registeredMBeans) {
            try {
                mbs.unregisterMBean(name);
            } catch (JMException ex) {
                log.severe("Failed to unregister MBean " + name + ": " + ex.getMessage());
            }
        }
        log.info("Unregistered " + registeredMBeans.size() + " platform MXBeans");
        registeredMBeans.clear();
        mbs = null;
        state = STOPPED;
    }

    public void destroy() {
        state = DESTROYED;
    }
}
