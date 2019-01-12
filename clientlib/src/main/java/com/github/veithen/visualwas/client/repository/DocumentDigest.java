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
package com.github.veithen.visualwas.client.repository;

import java.io.Serializable;

import org.apache.commons.codec.binary.Hex;

import com.github.veithen.visualwas.connector.mapped.MappedClass;

// TODO: implement equals and hashCode
@MappedClass("com.ibm.ws.management.repository.DocumentDigestImpl")
public class DocumentDigest implements Serializable {
    private static final long serialVersionUID = 3015221028590796750L;

    private byte[] digest;

    @Override
    public String toString() {
        return Hex.encodeHexString(digest);
    }
}
