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
package com.github.veithen.visualwas.client.pmi;

import com.github.veithen.visualwas.connector.mapped.MappedClass;

@MappedClass("com.ibm.ws.pmi.stat.RangeStatisticImpl")
public class RangeStatistic extends Statistic {
    private static final long serialVersionUID = -855214334683355657L;

    protected long highWaterMark;
    
    private long lowWaterMark;
    private long current;
    private double integral;
    private boolean initWaterMark;
    private RangeStatistic baseValue;
    
    public long getCurrent() {
        return current;
    }

    @Override
    void format(StringBuilder buffer) {
        super.format(buffer);
        buffer.append(", lowWaterMark=");
        buffer.append(lowWaterMark);
        buffer.append(", highWaterMark=");
        buffer.append(highWaterMark);
        buffer.append(", current=");
        buffer.append(current);
        buffer.append(", integral=");
        buffer.append(integral);
    }
}
