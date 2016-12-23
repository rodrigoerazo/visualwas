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
package com.github.veithen.visualwas.connector.feature;

import com.github.veithen.visualwas.connector.AdminService;
import com.github.veithen.visualwas.connector.Invocation;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public abstract class ContextPopulatingInterceptor<T> implements Interceptor<Invocation,Object> {
    private final Class<T> type;
    private ListenableFuture<T> future;

    public ContextPopulatingInterceptor(Class<T> type) {
        this.type = type;
    }

    @Override
    public final ListenableFuture<?> invoke(final InvocationContext context, final Invocation request,
            final Handler<Invocation, Object> nextHandler) {
        ListenableFuture<T> future;
        synchronized (this) {
            future = this.future;
            if (future == null) {
                this.future = future = produceValue(context.getAdminService(nextHandler));
                Futures.addCallback(future, new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        // Remove the future so that subsequent invocations will retry
                        synchronized (ContextPopulatingInterceptor.this) {
                            ContextPopulatingInterceptor.this.future = null;
                        }
                    }
                });
            }
        }
        final SettableFuture<Object> futureResult = SettableFuture.create();
        Futures.addCallback(future, new FutureCallback<T>() {
            @Override
            public void onSuccess(T value) {
                // TODO: should the context really be mutable?
                context.setAttribute(type, value);
                futureResult.setFuture(nextHandler.invoke(context, request));
            }

            @Override
            public void onFailure(Throwable t) {
                futureResult.setException(t);
            }
        });
        return futureResult;
    }

    protected abstract ListenableFuture<T> produceValue(AdminService adminService);
}