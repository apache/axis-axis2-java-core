
package org.apache.axis2.jaxws.sample.doclitbaremin.sei;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.Holder;



/**
 * Tests doc/lit bare minimal
 * (Minimal indicates that no ObjectFactory is available to do the parameter marshalling/demarshalling)
 * 
 */
@WebService(name = "DocLitBareMinPortType", targetNamespace = "http://doclitbaremin.sample.test.org")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public interface DocLitBareMinPortType {

    
    /**
     * echo
     * @param allByMyself
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(name = "String", targetNamespace = "http://doclitbaremin.sample.test.org", partName = "allByMyself")
    public String echo(
        @WebParam(name = "String", targetNamespace = "http://doclitbaremin.sample.test.org", partName = "allByMyself")
        String allByMyself);
        

}
