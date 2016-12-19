/*
 * #%L
 * VisualWAS
 * %%
 * Copyright (C) 2013 - 2016 Andreas Veithen
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
package com.github.veithen.visualwas.connector.proxy;

import java.util.Iterator;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.github.veithen.visualwas.connector.AdminService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class ObjectNamePatternMBeanLocator implements MBeanLocator {
    private final ObjectName pattern;

    public ObjectNamePatternMBeanLocator(ObjectName pattern) {
        this.pattern = pattern;
    }
    
    public ObjectNamePatternMBeanLocator(String pattern) throws MalformedObjectNameException {
        this(new ObjectName(pattern));
    }

    @Override
    public ListenableFuture<ObjectName> locateMBean(AdminService adminService) {
        final SettableFuture<ObjectName> result = SettableFuture.create();
        Futures.addCallback(
                adminService.queryNames(pattern, null),
                new FutureCallback<Set<ObjectName>>() {
                    @Override
                    public void onSuccess(Set<ObjectName> names) {
                        Iterator<ObjectName> it = names.iterator();
                        if (it.hasNext()) {
                            ObjectName mbean = it.next();
                            if (it.hasNext()) {
                                result.setException(new InstanceNotFoundException("Found multiple MBeans matching " + pattern));
                            } else {
                                result.set(mbean);
                            }
                        } else {
                            result.setException(new InstanceNotFoundException(pattern + " not found"));
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        result.setException(t);
                    }
                });
        return result;
    }
}
