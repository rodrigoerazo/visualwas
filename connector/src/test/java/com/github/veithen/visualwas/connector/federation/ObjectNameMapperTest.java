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

import static org.junit.Assert.assertEquals;

import javax.management.ObjectName;

import org.junit.Test;

public class ObjectNameMapperTest {
    @Test
    public void testLocalToRemote() throws Exception {
        ObjectNameMapper mapper = new ObjectNameMapper("test", "node1", "server1");
        assertEquals(new ObjectName("WebSphere:type=MyMBean,cell=test,node=node1,process=server1"),
                mapper.localToRemote(new ObjectName("WebSphere:type=MyMBean")));
    }
    
    @Test
    public void testPropertyListPattern() throws Exception {
        ObjectNameMapper mapper = new ObjectNameMapper("test", "node1", "server1");
        assertEquals(new ObjectName("WebSphere:type=Server,cell=test,node=node1,process=server1,*"),
                mapper.localToRemote(new ObjectName("WebSphere:type=Server,*")));
    }
    
    @Test
    public void testNonRoutable() throws Exception {
        ObjectNameMapper mapper = new ObjectNameMapper("test", "node1", "server1");
        ObjectName name = new ObjectName("java.lang:type=Runtime");
        assertEquals(name, mapper.localToRemote(name));
    }
}
