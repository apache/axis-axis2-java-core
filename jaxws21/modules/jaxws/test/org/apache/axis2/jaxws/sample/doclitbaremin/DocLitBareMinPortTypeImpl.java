package org.apache.axis2.jaxws.sample.doclitbaremin;

import javax.jws.WebService;
import javax.xml.ws.Holder;

import org.apache.axis2.jaxws.sample.doclitbaremin.sei.DocLitBareMinPortType;

/**
 * Test DocLitBareMinPort
 */
@WebService(endpointInterface="org.apache.axis2.jaxws.sample.doclitbaremin.sei.DocLitBareMinPortType")
public class DocLitBareMinPortTypeImpl implements DocLitBareMinPortType {

    /* 
     * echo
     */
    public String echo(String allByMyself) {
        return allByMyself;
    }

}
