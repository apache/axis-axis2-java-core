
package org.apache.axis2.jaxws.sample.mtom;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.test.mtom.ImageDepot;

@WebService(name = "MtomSample", 
            targetNamespace = "http://org/apache/axis2/jaxws/sample/mtom", 
            wsdlLocation = "META-INF/ImageDepot.wsdl")
public interface MtomSample {


    /**
     * 
     * @param input
     * @return
     *     returns org.test.mtom.ImageDepot
     */
    @WebMethod
    @WebResult(name = "output", targetNamespace = "urn://mtom.test.org")
    @RequestWrapper(localName = "sendImage", targetNamespace = "urn://mtom.test.org", className = "org.test.mtom.SendImage")
    @ResponseWrapper(localName = "sendImageResponse", targetNamespace = "urn://mtom.test.org", className = "org.test.mtom.SendImageResponse")
    public ImageDepot sendImage(
        @WebParam(name = "input", targetNamespace = "urn://mtom.test.org")
        ImageDepot input);

    /**
     * 
     * @param input
     * @return
     *     returns org.test.mtom.ImageDepot
     */
    @WebMethod
    @WebResult(name = "output", targetNamespace = "urn://mtom.test.org")
    @RequestWrapper(localName = "sendText", targetNamespace = "urn://mtom.test.org", className = "org.test.mtom.SendText")
    @ResponseWrapper(localName = "sendTextResponse", targetNamespace = "urn://mtom.test.org", className = "org.test.mtom.SendTextResponse")
    public ImageDepot sendText(
        @WebParam(name = "input", targetNamespace = "urn://mtom.test.org")
        byte[] input);

}
