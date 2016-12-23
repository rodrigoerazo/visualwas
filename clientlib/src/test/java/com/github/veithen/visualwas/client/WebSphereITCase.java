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
package com.github.veithen.visualwas.client;

import org.junit.Before;

import com.github.veithen.visualwas.connector.Connector;
import com.github.veithen.visualwas.connector.factory.Attributes;
import com.github.veithen.visualwas.connector.factory.ConnectorConfiguration;
import com.github.veithen.visualwas.connector.factory.ConnectorFactory;
import com.github.veithen.visualwas.connector.feature.Feature;
import com.github.veithen.visualwas.connector.security.BasicAuthCredentials;
import com.github.veithen.visualwas.connector.security.Credentials;
import com.github.veithen.visualwas.connector.transport.Endpoint;
import com.github.veithen.visualwas.connector.transport.TransportConfiguration;

public abstract class WebSphereITCase {
    protected Connector connector;

    @Before
    public void initConnector() throws Exception {
        ConnectorConfiguration.Builder configBuilder = ConnectorConfiguration.custom();
        Attributes attributes = new Attributes();
        attributes.set(Credentials.class, new BasicAuthCredentials("wsadmin", "abcd1234"));
        configBuilder.setTransportConfiguration(TransportConfiguration.custom().disableCertificateValidation().build());
        configBuilder.addFeatures(getFeatures());
        Endpoint endpoint = new Endpoint("localhost", Integer.parseInt(System.getProperty("was.soapPort")), true);
        connector = ConnectorFactory.getInstance().createConnector(endpoint, configBuilder.build(), attributes);
    }

    protected abstract Feature[] getFeatures();
}