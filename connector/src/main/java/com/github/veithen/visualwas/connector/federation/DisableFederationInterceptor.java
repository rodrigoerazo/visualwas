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

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.management.ObjectName;
import javax.management.QueryExp;

import com.github.veithen.visualwas.connector.AdminService;
import com.github.veithen.visualwas.connector.feature.Handler;
import com.github.veithen.visualwas.connector.feature.Interceptor;
import com.github.veithen.visualwas.connector.feature.InvocationContext;
import com.github.veithen.visualwas.framework.proxy.Invocation;

final class DisableFederationInterceptor implements Interceptor<Invocation,Object> {
    private ObjectNameMapper mapper;
    
    @Override
    public CompletableFuture<?> invoke(final InvocationContext context, Invocation invocation, final Handler<Invocation,Object> nextHandler) {
        String operationName = invocation.getOperation().getName();
        if (operationName.equals("getServerMBean")) {
            return nextHandler.invoke(context, invocation);
        } else {
            ObjectNameMapper mapper;
            synchronized (this) {
                if (this.mapper == null) {
                    ServerIdentity identity = context.getAttribute(ServerIdentity.class);
                    this.mapper = new ObjectNameMapper(identity.getCell(), identity.getNode(), identity.getProcess());
                }
                mapper = this.mapper;
            }
            if (operationName.equals("queryNames")) {
                Object[] args = invocation.getParameters();
                final AdminService adminService = context.getAdminService(nextHandler);
                return mapper.query((ObjectName)args[0], (QueryExp)args[1], new QueryExecutor<ObjectName>() {
                    @Override
                    public CompletableFuture<Set<ObjectName>> execute(ObjectName objectName, QueryExp queryExp) {
                        return adminService.queryNamesAsync(objectName, queryExp);
                    }
                });
            } else {
                return nextHandler.invoke(context, invocation);
            }
        }
    }
}
