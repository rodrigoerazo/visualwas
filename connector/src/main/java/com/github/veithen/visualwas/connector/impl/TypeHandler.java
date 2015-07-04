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
package com.github.veithen.visualwas.connector.impl;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;

public interface TypeHandler {
    /**
     * Get the QName of the XML type produced by this type handler. This method is only used when
     * serializing a nil value.
     * 
     * @param context
     * @return the XML type
     */
    QName getXMLType(InvocationContextImpl context);

    /**
     * 
     * 
     * @param element
     * @param value
     * @param context
     * @return the XML type, which may depend on <code>value</code> and be different from the one
     *         returned by {@link #getXMLType(InvocationContextImpl)}
     */
    QName setValue(OMElement element, Object value, InvocationContextImpl context);
    
    /**
     * Extract the value from a given element.
     * 
     * @param element
     *            the element to extract the value from
     * @param context
     *            the invocation context
     * @return the value
     * @throws ClassNotFoundException
     *             if a required class could not be loaded by the class loader returned by
     *             {@link InvocationContextImpl#getClassLoader()}
     * @throws TypeHandlerException
     *             if another error occurred
     */
    Object extractValue(OMElement element, InvocationContextImpl context) throws ClassNotFoundException, TypeHandlerException;
}