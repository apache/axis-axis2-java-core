package org.apache.axis2.jaxws.provider.soapbinding.string;

import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.apache.axis2.jaxws.Constants;
@WebServiceProvider(serviceName="SOAPBindingStringProviderService", 
		targetNamespace="http://StringProvider.soapbinding.provider.jaxws.axis2.apache.org",
		portName="SOAPBindingStringProviderPort")
@ServiceMode(value=Service.Mode.PAYLOAD)
@BindingType(Constants.SOAP_HTTP_BINDING)
/*
 * Provider with PAYLOAD Mode on Server, will receive soap11 and soap12 requests.
 */
public class StringProvider implements Provider<String> {
	public String invoke(String obj) {
		if(obj == null){
			return null;
		}
		return "<return>Hello String Provider</return>";
	}

}
