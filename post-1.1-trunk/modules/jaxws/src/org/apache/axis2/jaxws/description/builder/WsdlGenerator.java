package org.apache.axis2.jaxws.description.builder;

import javax.wsdl.Definition;

public interface WsdlGenerator {

	public Definition generateWsdl(Class implClass);
}
