package com.github.veithen.visualwas.connector;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;

public class OperationHandler {
    private final String operationName;
    private final String requestElementName;
    private final String responseElementName;
    private final ParamHandler[] paramHandlers;
    private final TypeHandler returnValueHandler;
    
    public OperationHandler(String operationName, String requestElementName, String responseElementName, ParamHandler[] paramHandlers, TypeHandler returnValueHandler) {
        this.operationName = operationName;
        this.requestElementName = requestElementName;
        this.responseElementName = responseElementName;
        this.paramHandlers = paramHandlers;
        this.returnValueHandler = returnValueHandler;
    }

    public void createRequest(SOAPBody body, Object[] args) {
        OMFactory factory = body.getOMFactory();
        OMNamespace ns = factory.createOMNamespace("urn:AdminService", "ns");
        OMElement element = factory.createOMElement(requestElementName, ns, body);
        element.addAttribute("encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", body.getNamespace());
        int paramCount = paramHandlers.length;
        if (paramCount > 0) {
            OMNamespace xsiNS = element.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
            for (int i=0; i<paramCount; i++) {
                paramHandlers[i].createOMElement(element, xsiNS, args[i]);
            }
        }
    }
    
    public Object processResponse(OMElement responseElement) throws OperationHandlerException {
        // TODO: check element names
        // TODO: check xsi:type???
        try {
            return returnValueHandler.extractValue(responseElement.getFirstElement());
        } catch (TypeHandlerException ex) {
            throw new OperationHandlerException("Failed to extract return value for operation " + operationName, ex);
        }
    }
}
