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
package com.github.veithen.visualwas.connector.impl;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletableFuture;

import com.github.veithen.visualwas.connector.ConnectorException;
import com.github.veithen.visualwas.connector.feature.Handler;
import com.github.veithen.visualwas.connector.feature.Interceptor;
import com.github.veithen.visualwas.connector.feature.InvocationContext;
import com.github.veithen.visualwas.connector.util.CompletableFutures;
import com.github.veithen.visualwas.framework.proxy.Invocation;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Wraps undeclared exception in {@link ConnectorException} to avoid
 * {@link UndeclaredThrowableException}.
 */
final class UndeclaredExceptionInterceptor implements Interceptor<Invocation,Object> {
    static final UndeclaredExceptionInterceptor INSTANCE = new UndeclaredExceptionInterceptor();

    private UndeclaredExceptionInterceptor() {}

    @Override
    public CompletableFuture<? extends Object> invoke(InvocationContext context, final Invocation invocation, Handler<Invocation,Object> nextHandler) {
        CompletableFuture<? extends Object> future = nextHandler.invoke(context, invocation);
        final CompletableFuture<Object> transformedFuture = new CompletableFuture<>();
        CompletableFutures.addCallback(future, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object value) {
                transformedFuture.complete(value);
            }

            @Override
            public void onFailure(Throwable t) {
                if (!(t instanceof RuntimeException || t instanceof Error)) {
                    boolean isDeclared = false;
                    for (Class<?> exceptionType : invocation.getOperation().getExceptionTypes()) {
                        if (exceptionType.isInstance(t)) {
                            isDeclared = true;
                            break;
                        }
                    }
                    if (!isDeclared) {
                        t = new ConnectorException("Received unexpected exception", t);
                    }
                }
                transformedFuture.completeExceptionally(t);
            }
        }, MoreExecutors.directExecutor());
        return transformedFuture;
    }
}
