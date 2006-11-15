
package org.apache.axis2.jaxws.resourceinjection.sei;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(name = "ResourceInjectionPortType", targetNamespace = "http://resourceinjection.sample.test.org")
public interface ResourceInjectionPortType {


    /**
     * 
     * @param arg
     * @return
     *     returns java.lang.String
     */
    @WebMethod(action = "http://resourceinjection.sample.test.org/NewOperation")
    @WebResult(name = "response", targetNamespace = "")
    @RequestWrapper(localName = "echo", targetNamespace = "http://resourceinjection.sample.test.org", className = "org.test.sample.resourceinjection.Echo")
    @ResponseWrapper(localName = "echoResponse", targetNamespace = "http://resourceinjection.sample.test.org", className = "org.test.sample.resourceinjection.EchoResponse")
    public String echo(
        @WebParam(name = "arg", targetNamespace = "")
        String arg);

}
