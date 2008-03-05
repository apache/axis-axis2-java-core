package org.apache.axis2.jaxws.context.sei;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


@WebService(name = "MessageContext", 
        portName="MessageContextPort",
        targetNamespace = "http://context.jaxws.axis2.apache.org/", 
        wsdlLocation = "META-INF/MessageContext.wsdl")
public interface MessageContext {
    /**
     * @param value
     * @param type
     * @param propertyName
     * @param isFound
     */
    @WebMethod
    @RequestWrapper(localName = "isPropertyPresent", targetNamespace = "http://context.jaxws.axis2.apache.org/", className = "org.apache.axis2.jaxws.context.sei.IsPropertyPresent")
    @ResponseWrapper(localName = "isPropertyPresentResponse", targetNamespace = "http://context.jaxws.axis2.apache.org/", className = "org.apache.axis2.jaxws.context.sei.IsPropertyPresentResponse")
    public void isPropertyPresent(
            @WebParam(name = "propertyName", targetNamespace = "", mode = WebParam.Mode.INOUT)
            Holder<String> propertyName,
            @WebParam(name = "value", targetNamespace = "", mode = WebParam.Mode.INOUT)
            Holder<String> value,
            @WebParam(name = "type", targetNamespace = "", mode = WebParam.Mode.INOUT)
            Holder<String> type,
            @WebParam(name = "isFound", targetNamespace = "", mode = WebParam.Mode.INOUT)
            Holder<Boolean> isFound);

}
