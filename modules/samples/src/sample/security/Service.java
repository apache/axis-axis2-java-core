package sample.security;

import org.apache.axis2.om.OMElement;

public class Service {

	public OMElement echo(OMElement elem) {
		elem.build();
		elem.detach();
		return elem;
	}
	
}
