/*
 * #%L
 * VisualWAS
 * %%
 * Copyright (C) 2013 - 2019 Andreas Veithen
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
package com.github.veithen.visualwas.connector.federation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.QueryExp;

final class ObjectNameMapper implements Mapper<ObjectName> {
    private static final Set<String> nonRoutableDomains = new HashSet<>(Arrays.asList("JMImplementation", "java.lang"));
    private static final Set<String> routingPropertyKeys = new HashSet<>(Arrays.asList("cell", "node", "process"));
    
    private final String cell;
    private final String node;
    private final String process;
    private final Set<ObjectName> nonRoutableMBeans = Collections.synchronizedSet(new HashSet<ObjectName>());
    private final Map<ObjectName,ObjectName> localToRemoteCache = new WeakHashMap<>();
    // TODO: for localToRemoteCache, WeakHashMap is a good choice, but not for remoteToLocalCache
    private final Map<ObjectName,ObjectName> remoteToLocalCache = new WeakHashMap<>();
    
    ObjectNameMapper(String cell, String node, String process) {
        this.cell = cell;
        this.node = node;
        this.process = process;
    }

    ObjectName localToRemote(ObjectName localName) throws IOException {
        if (nonRoutableDomains.contains(localName.getDomain()) || nonRoutableMBeans.contains(localName)) {
            return localName;
        } else {
            synchronized (localToRemoteCache) {
                ObjectName remoteName = localToRemoteCache.get(localName);
                if (remoteName == null) {
                    Hashtable<String,String> newProps = new Hashtable<>(localName.getKeyPropertyList());
                    newProps.put("cell", cell);
                    newProps.put("node", node);
                    newProps.put("process", process);
                    try {
                        remoteName = new ObjectName(localName.getDomain(), newProps);
                        if (localName.isPropertyListPattern()) {
                            remoteName = new ObjectName(remoteName + ",*");
                        }
                    } catch (MalformedObjectNameException ex) {
                        // It's unlikely that we ever get here
                        throw new Error(ex);
                    }
                    localToRemoteCache.put(localName, remoteName);
                }
                return remoteName;
            }
        }
    }
    
    public ObjectName remoteToLocal(ObjectName remoteName) {
        if (nonRoutableMBeans.contains(remoteName)) {
            return remoteName;
        } else {
            synchronized (remoteToLocalCache) {
                ObjectName localName = remoteToLocalCache.get(remoteName);
                if (localName == null) {
                    Hashtable<String,String> props = remoteName.getKeyPropertyList();
                    Hashtable<String,String> newProps = new Hashtable<>();
                    int routingPropertyKeyCount = 0;
                    for (Map.Entry<String,String> prop : props.entrySet()) {
                        String key = prop.getKey();
                        if (routingPropertyKeys.contains(key)) {
                            routingPropertyKeyCount++;
                        } else {
                            newProps.put(key, prop.getValue());
                        }
                    }
                    if (routingPropertyKeyCount == 0) {
                        nonRoutableMBeans.add(remoteName);
                        return remoteName;
                    } else if (routingPropertyKeyCount == 3) {
                        try {
                            localName = new ObjectName(remoteName.getDomain(), newProps);
                        } catch (MalformedObjectNameException ex) {
                            // It's unlikely that we ever get here
                            throw new Error(ex);
                        }
                    } else {
                        throw new IllegalArgumentException("Expected exactly 3 routing key properties");
                    }
                    remoteToLocalCache.put(remoteName, localName);
                }
                return localName;
            }
        }
    }
    
    Set<ObjectName> remoteToLocal(Set<ObjectName> remoteNames) {
        Set<ObjectName> result = new HashSet<>();
        for (ObjectName name : remoteNames) {
            result.add(remoteToLocal(name));
        }
        return result;
    }
    
    CompletableFuture<Set<ObjectName>> query(ObjectName objectName, QueryExp queryExp, QueryExecutor<ObjectName> queryExecutor) {
        return query(objectName, queryExp, queryExecutor, this);
    }
    
    private <T> CompletableFuture<Set<T>> query(ObjectName objectName, QueryExp queryExp, QueryExecutor<T> queryExecutor, final Mapper<T> mapper) {
        if (queryExp != null) {
            // TODO
            throw new UnsupportedOperationException();
        }
        if (objectName == null) {
            try {
                List<CompletableFuture<Set<T>>> futures = new ArrayList<>();
                futures.add(queryExecutor
                        .execute(new ObjectName("*:cell=" + cell + ",node=" + node + ",process=" + process + ",*"), queryExp)
                        .thenApply(input -> input.stream().map(mapper::remoteToLocal).collect(Collectors.toSet())));
                for (String domain : nonRoutableDomains) {
                    futures.add(queryExecutor.execute(new ObjectName(domain + ":*"), queryExp));
                }
                return CompletableFuture
                        .allOf(futures.toArray(new CompletableFuture<?>[futures.size()]))
                        .thenApply(unused -> futures.stream().map(x -> x.getNow(null)).flatMap(Set::stream).collect(Collectors.toSet()));
            } catch (MalformedObjectNameException ex) {
                throw new Error(ex);
            }
        } else if (nonRoutableDomains.contains(objectName.getDomain())) {
            return queryExecutor.execute(objectName, queryExp);
        } else {
            // TODO
            throw new UnsupportedOperationException();
        }
    }
}
