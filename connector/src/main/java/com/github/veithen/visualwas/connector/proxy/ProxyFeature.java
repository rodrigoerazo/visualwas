/*
 * #%L
 * VisualWAS
 * %%
 * Copyright (C) 2013 - 2014 Andreas Veithen
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

import com.github.veithen.visualwas.connector.AdminService;
import com.github.veithen.visualwas.connector.Connector;
import com.github.veithen.visualwas.connector.feature.AdapterFactory;
import com.github.veithen.visualwas.connector.feature.Configurator;
import com.github.veithen.visualwas.connector.feature.ConfiguratorAdapter;
import com.github.veithen.visualwas.connector.feature.Feature;

/**
 * Feature that enables the creation of MBean proxies. Proxies can be created in two different ways:
 * <ol>
 * <li>A feature can use {@link ProxyConfigurator#registerProxy(Class, MBeanLocator)} to register a
 * proxy. That proxy will be available through {@link Connector#getAdapter(Class)}. This will create
 * a single proxy instance per connection. This method is typically used for singleton MBeans.
 * <li>Application code can use {@link ProxyFactory} (obtained using
 * {@link Connector#getAdapter(Class)} to create proxies. These proxies are not cached.
 * </ol>
 */
@ConfiguratorAdapter(ProxyConfigurator.class)
public final class ProxyFeature implements Feature {
    public static final ProxyFeature INSTANCE = new ProxyFeature();
    
    private ProxyFeature() {}

    @Override
    public void configureConnector(Configurator configurator) {
        configurator.registerConfiguratorAdapter(ProxyConfigurator.class, new ProxyConfiguratorImpl(configurator));
        configurator.registerAdminServiceAdapter(ProxyFactory.class, new AdapterFactory<ProxyFactory>() {
            @Override
            public ProxyFactory createAdapter(AdminService adminService) {
                return new ProxyFactoryImpl(adminService);
            }
        });
    }
}