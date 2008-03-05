package org.apache.axis2.jaxws.context;

import org.apache.axis2.jaxws.context.sei.MessageContext;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;

@WebService(serviceName="MessageContextService",
        portName="MessageContextPort",
        targetNamespace = "http://context.jaxws.axis2.apache.org/",
        endpointInterface = "org.apache.axis2.jaxws.context.sei.MessageContext")
public class MessageContextImpl implements MessageContext {

    @Resource
    WebServiceContext ctxt;

    public void isPropertyPresent(
            Holder<String> propertyName,
            Holder<String> value,
            Holder<String> type,
            Holder<Boolean> isFound) {
        System.out.println(">> isPropertyPresent(" + propertyName.value + ")");
        javax.xml.ws.handler.MessageContext msgCtxt = ctxt.getMessageContext();
        if (msgCtxt != null) {
            isFound.value = msgCtxt.containsKey(propertyName.value);
            Object val = msgCtxt.get(propertyName.value);
            System.out.println("msgCtxt.containsKey=" + isFound.value);
            System.out.println("msgCtxt.get=" + val);

            if (val != null) {
                type.value = val.getClass().getName();
                value.value = val.toString();
            }
        }
        System.out.println("<< isPropertyPresent()");
    }
}