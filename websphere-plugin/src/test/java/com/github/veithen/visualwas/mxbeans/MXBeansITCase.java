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
package com.github.veithen.visualwas.mxbeans;

import static com.google.common.truth.Truth.assertThat;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.Test;

import com.github.veithen.visualwas.jmx.soap.SOAPJMXConnector;
import com.github.veithen.visualwas.x509.PromiscuousTrustManager;

public class MXBeansITCase {
    private interface Action<T> {
        void run(T mxbean) throws Exception;
    }
    
    private static <T> void run(String role, String objectName, Class<T> interfaceClass, Action<T> action) throws Exception {
        Map<String,Object> env = new HashMap<String,Object>();
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "com.github.veithen.visualwas.jmx");
        env.put(SOAPJMXConnector.PROXY, Proxy.NO_PROXY);
        env.put(JMXConnector.CREDENTIALS, new String[] { role, "changeme" });
        env.put(SOAPJMXConnector.TRUST_MANAGER, PromiscuousTrustManager.INSTANCE);
        JMXServiceURL url = new JMXServiceURL("soap", "localhost", Integer.parseInt(System.getProperty("was.soapPort")));
        JMXConnector connector = JMXConnectorFactory.connect(url, env);
        MBeanServerConnection connection = connector.getMBeanServerConnection();
        action.run(JMX.newMXBeanProxy(connection, new ObjectName(objectName), interfaceClass));
        connector.close();
    }
    
    @Test
    public void testRuntimeMXBean() throws Exception {
        run("monitor", ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class, (runtimeMXBean) -> {
            Map<String,String> systemProperties = runtimeMXBean.getSystemProperties();
            assertThat(systemProperties).containsEntry("java.util.logging.manager", "com.ibm.ws.bootstrap.WsLogManager");
        });
    }
    
    @Test(expected=SecurityException.class)
    public void testAccessDenied() throws Exception {
        run("monitor", ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class, (memoryMXBean) -> {
            memoryMXBean.gc();
        });
    }
    
    @Test
    public void testAccessGranted() throws Exception {
        run("operator", ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class, (memoryMXBean) -> {
            memoryMXBean.gc();
        });
    }
}
