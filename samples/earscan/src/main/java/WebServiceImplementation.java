/*
 * #%L
 * VisualWAS
 * %%
 * Copyright (C) 2013 Andreas Veithen
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

public class WebServiceImplementation {
    private final String className;
    private final String endpointInterface;
    private final String wsdlLocation;
    private final String targetNamespace;
    private final String serviceName;
    private final String portName;
    
    public WebServiceImplementation(String className, String endpointInterface,
            String wsdlLocation, String targetNamespace, String serviceName,
            String portName) {
        this.className = className;
        this.endpointInterface = endpointInterface;
        this.wsdlLocation = wsdlLocation;
        this.targetNamespace = targetNamespace;
        this.serviceName = serviceName;
        this.portName = portName;
    }

    public String getClassName() {
        return className;
    }

    public String getEndpointInterface() {
        return endpointInterface;
    }

    public String getWsdlLocation() {
        return wsdlLocation;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPortName() {
        return portName;
    }
}
