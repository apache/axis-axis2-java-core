/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.apache.axis2.jaxws.sample.doclitbaremin.sei.BareDocLitMinService;
import org.apache.axis2.jaxws.sample.doclitbaremin.sei.DocLitBareMinPortType;

import junit.framework.TestCase;


public class DocLitBareMinTests extends TestCase {
	
    public void testEcho() throws Exception {
        System.out.println("------------------------------");
        System.out.println("Test : "+getName());
        
        
        BareDocLitMinService service = new BareDocLitMinService();
        DocLitBareMinPortType proxy = service.getBareDocLitMinPort();
        BindingProvider p = (BindingProvider) proxy;
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        p.getRequestContext().put(
                BindingProvider.SOAPACTION_URI_PROPERTY, "echo");
        String request = "dlroW elloH";
        String response = proxy.echo(request);
        
        assertTrue(request.equals(response));
        
    }
}
