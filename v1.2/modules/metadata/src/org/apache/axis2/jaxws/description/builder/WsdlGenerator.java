package org.apache.axis2.jaxws.description.builder;

import javax.xml.ws.WebServiceException;

public interface WsdlGenerator {

    public WsdlComposite generateWsdl(String implClass, String bindingType)
            throws WebServiceException;
}
