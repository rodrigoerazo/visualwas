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
package com.github.veithen.visualwas.connector.notification;

import com.github.veithen.visualwas.connector.feature.Configurator;
import com.github.veithen.visualwas.connector.feature.Dependencies;
import com.github.veithen.visualwas.connector.feature.Feature;

@Dependencies(RemoteNotificationFeature.class)
public class NotificationDispatcherFeature implements Feature {
    public static final NotificationDispatcherFeature INSTANCE = new NotificationDispatcherFeature();

    private NotificationDispatcherFeature() {}

    @Override
    public void configureConnector(Configurator configurator) {
        configurator.registerAdminServiceAdapter(
                NotificationDispatcher.class,
                (adminService, executor) -> new NotificationDispatcherImpl((RemoteNotificationService)adminService, executor));
    }
}
