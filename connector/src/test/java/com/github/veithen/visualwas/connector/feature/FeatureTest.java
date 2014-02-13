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
package com.github.veithen.visualwas.connector.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.github.veithen.visualwas.connector.Callback;
import com.github.veithen.visualwas.connector.Connector;
import com.github.veithen.visualwas.connector.Handler;
import com.github.veithen.visualwas.connector.Invocation;
import com.github.veithen.visualwas.connector.description.AdminServiceDescription;
import com.github.veithen.visualwas.connector.description.AdminServiceDescriptionFactory;
import com.github.veithen.visualwas.connector.description.OperationDescription;
import com.github.veithen.visualwas.connector.factory.ConnectorConfiguration;
import com.github.veithen.visualwas.connector.factory.ConnectorFactory;
import com.github.veithen.visualwas.connector.transport.Endpoint;

public class FeatureTest {
    @Test
    public void testInvocationInterceptorWithAdminServiceExtension() throws Exception {
        ConnectorConfiguration config = ConnectorConfiguration.custom().addFeatures(new Feature() {
            @Override
            public void configureConnector(Configurator configurator) {
                AdminServiceDescription desc = AdminServiceDescriptionFactory.getInstance().createDescription(DummyAdminServiceExtension.class);
                configurator.addAdminServiceDescription(desc);
                final OperationDescription operation = desc.getOperation("echo");
                configurator.addInvocationInterceptor(new Interceptor<Invocation,Object,Throwable>() {
                    public void invoke(InvocationContext context, Invocation invocation, Callback<Object,Throwable> callback, Handler<Invocation,Object,Throwable> nextHandler) {
                        if (invocation.getOperation() == operation) {
                            callback.onResponse(invocation.getArgs()[0]);
                        } else {
                            nextHandler.invoke(context, invocation, callback);
                        }
                    }
                });
            }
        }).build();
        Connector connector = ConnectorFactory.getInstance().createConnector(new Endpoint("localhost", 8880, false), config, null);
        DummyAdminServiceExtension extension = connector.getAdapter(DummyAdminServiceExtension.class);
        assertNotNull(extension);
        assertEquals("test", extension.echo("test"));
    }
}
