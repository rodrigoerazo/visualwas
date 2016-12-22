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
package com.github.veithen.visualwas.connector.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.veithen.visualwas.connector.description.Interface;
import com.github.veithen.visualwas.framework.proxy.InvocationTarget;

public final class ProxyFactory {
    private ProxyFactory() {}
    
    public static Object createProxy(ClassLoader classLoader, Interface[] ifaces, InvocationTarget target) {
        Set<Class<?>> javaInterfaces = new HashSet<>();
        Map<Method,InvocationHandlerDelegate> invocationHandlerDelegates = new HashMap<>();
        for (Interface iface : ifaces) {
            javaInterfaces.add(iface.getInterface());
            invocationHandlerDelegates.putAll(((InterfaceImpl)iface).getInvocationHandlerDelegates());
        }
        return Proxy.newProxyInstance(
                classLoader,
                javaInterfaces.toArray(new Class<?>[javaInterfaces.size()]),
                new ProxyInvocationHandler(
                        invocationHandlerDelegates,
                        target));
    }
}
