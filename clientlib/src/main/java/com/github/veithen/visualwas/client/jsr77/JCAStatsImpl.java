/*
 * #%L
 * VisualWAS
 * %%
 * Copyright (C) 2013 - 2020 Andreas Veithen
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
package com.github.veithen.visualwas.client.jsr77;

import javax.management.j2ee.statistics.JCAConnectionPoolStats;
import javax.management.j2ee.statistics.JCAConnectionStats;
import javax.management.j2ee.statistics.JCAStats;

import com.github.veithen.visualwas.connector.mapped.MappedClass;

@MappedClass("com.ibm.ws.pmi.j2ee.JCAStatsImpl")
final class JCAStatsImpl extends StatsImpl implements JCAStats {
    private static final long serialVersionUID = 565826631682061849L;

    @Override
    public JCAConnectionStats[] getConnections() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public JCAConnectionPoolStats[] getConnectionPools() {
        // TODO
        throw new UnsupportedOperationException();
    }
}
