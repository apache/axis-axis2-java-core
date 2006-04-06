package org.apache.axis2.security.rahas;
import org.apache.axiom.om.OMElement;



public class Service {

	public OMElement echo(OMElement elem) {
		elem.build();
		elem.detach();
        System.out.println("Service invoked");
		return elem;
	}
	
}
