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

import java.util.ArrayList;
import java.util.List;

import com.github.veithen.visualwas.connector.Handler;
import com.github.veithen.visualwas.connector.feature.Interceptor;

final class InterceptorChainBuilder<S,T> {
    private final List<Interceptor<S,T>> interceptors = new ArrayList<>();
    
    void add(Interceptor<S,T> interceptor) {
        interceptors.add(0, interceptor);
    }
    
    Handler<S,T> buildHandler(Handler<S,T> targetHandler) {
        Handler<S,T> handler = targetHandler;
        for (Interceptor<S,T> interceptor : interceptors) {
            handler = new InterceptorHandler<>(interceptor, handler);
        }
        return handler;
    }
}
