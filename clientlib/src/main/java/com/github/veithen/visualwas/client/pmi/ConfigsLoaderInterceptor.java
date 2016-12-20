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
package com.github.veithen.visualwas.client.pmi;

import javax.management.ObjectName;

import com.github.veithen.visualwas.connector.AdminService;
import com.github.veithen.visualwas.connector.feature.ContextPopulatingInterceptor;
import com.github.veithen.visualwas.connector.proxy.SingletonMBeanLocator;
import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

final class ConfigsLoaderInterceptor extends ContextPopulatingInterceptor<Configs> {
    ConfigsLoaderInterceptor() {
        super(Configs.class);
    }

    @Override
    protected ListenableFuture<Configs> produceValue(final AdminService adminService) {
        return Futures.dereference(Futures.transform(
                new SingletonMBeanLocator("Perf").locateMBean(adminService),
                new Function<ObjectName,ListenableFuture<Configs>>() {
                    @Override
                    public ListenableFuture<Configs> apply(ObjectName perfMBean) {
                        return Futures.transform(
                                adminService.invokeAsync(perfMBean, "getConfigs", null, null),
                                new Function<Object,Configs>() {
                                    @Override
                                    public Configs apply(Object input) {
                                        return new Configs((PmiModuleConfig[])input);
                                    }
                                });
                    }
                }));
    }
}
