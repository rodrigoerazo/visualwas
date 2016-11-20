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
package com.github.veithen.visualwas.connector.federation;

import java.util.Set;

import javax.management.ObjectName;
import javax.management.QueryExp;

import com.github.veithen.visualwas.connector.AdminService;
import com.github.veithen.visualwas.connector.Handler;
import com.github.veithen.visualwas.connector.Invocation;
import com.github.veithen.visualwas.connector.description.OperationDescription;
import com.github.veithen.visualwas.connector.feature.Interceptor;
import com.github.veithen.visualwas.connector.feature.InvocationContext;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

final class InvocationInterceptor implements Interceptor<Invocation,Object> {
    private static final OperationDescription getServerMBeanOperation = AdminService.DESCRIPTION.getOperation("getServerMBean");
    private static final OperationDescription queryNamesOperation = AdminService.DESCRIPTION.getOperation("queryNames");
    
    private ListenableFuture<ObjectNameMapper> mapperFuture;
    
    @Override
    public ListenableFuture<?> invoke(final InvocationContext context, Invocation invocation, final Handler<Invocation,Object> nextHandler) {
        final ListenableFuture<ObjectNameMapper> mapperFuture;
        synchronized (this) {
            if (this.mapperFuture == null) {
                this.mapperFuture = Futures.transform(
                        nextHandler.invoke(context, new Invocation(getServerMBeanOperation)),
                        new Function<Object,ObjectNameMapper>() {
                            @Override
                            public ObjectNameMapper apply(Object response) {
                                ObjectName serverMBean = (ObjectName)response;
                                return new ObjectNameMapper(serverMBean.getKeyProperty("cell"), serverMBean.getKeyProperty("node"), serverMBean.getKeyProperty("process"));
                            }
                        },
                        context.getExecutor());
            }
            mapperFuture = this.mapperFuture;
        }
        if (invocation.getOperation() == queryNamesOperation) {
            final SettableFuture<Object> result = SettableFuture.create();
            Object[] args = invocation.getArgs();
            final ObjectName objectName = (ObjectName)args[0];
            final QueryExp queryExp = (QueryExp)args[1];
            return Futures.dereference(Futures.transform(
                    mapperFuture,
                    new Function<ObjectNameMapper,ListenableFuture<Set<ObjectName>>>() {
                        @Override
                        public ListenableFuture<Set<ObjectName>> apply(ObjectNameMapper mapper) {
                            return mapper.query(objectName, queryExp, new QueryExecutor<ObjectName>() {
                                @Override
                                public ListenableFuture<Set<ObjectName>> execute(ObjectName objectName, QueryExp queryExp) {
                                    return Futures.transform(
                                            nextHandler.invoke(context, new Invocation(queryNamesOperation, objectName, queryExp)),
                                            new Function<Object,Set<ObjectName>>() {
                                                @Override
                                                public Set<ObjectName> apply(Object input) {
                                                    return (Set<ObjectName>)input;
                                                }
                                            });
                                }
                            });
                        }
                    },
                    context.getExecutor()));
        } else {
            return nextHandler.invoke(context, invocation);
        }
    }
}
