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

import com.github.veithen.visualwas.connector.AdminService;

/**
 * Interceptor that intercepts calls at the {@link AdminService} level. To intercept SOAP messages
 * sent and received by the connector, use {@link Interceptor}.
 */
public interface AdminServiceInterceptor {
    /**
     * Create an {@link AdminService} proxy. The proxy should implement the necessary interception
     * logic.
     * 
     * @param adminService
     *            the {@link AdminService} instance to create a proxy for
     * @return the proxy; never <code>null</code>
     */
    AdminService createProxy(AdminService adminService);
}
