package org.apache.axis2.tool.codegen.eclipse.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axis2.util.URLProcessor;



/**
 * This class presents a convenient way of reading the 
 * WSDL file(url) and producing a useful set of information
 * It does NOT use any of the standard WSDL classes from 
 * Axis2, rather it uses wsdl4j to read the wsdl and extract 
 * the properties (This is meant as a convenience for the UI
 * only. We may not need the whole conversion the WSDLpump 
 * goes through)
 * One would need to change this to suit a proper WSDL 
 * @author Ajith
 *
 */
public class WSDLPropertyReader {
    private Definition wsdlDefinition = null;
    
	public void readWSDL(String filepath) throws Exception{
		WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
		wsdlDefinition = reader.readWSDL(filepath); 
	}
	
	//get the default package derived by the targetNamespace
	
	public String packageFromTargetNamespace(){
		return  URLProcessor.makePackageName(wsdlDefinition.getTargetNamespace());
		
	}
	/**
	 * Returns a list of service names
	 * the names are QNames
	 * @return
	 */
	public List getServiceList(){
		List returnList = new ArrayList();
		Service service = null;
		Map serviceMap = wsdlDefinition.getServices();
		if(serviceMap!=null && !serviceMap.isEmpty()){
		   Iterator serviceIterator = serviceMap.values().iterator();
		   while(serviceIterator.hasNext()){
			   service = (Service)serviceIterator.next();
			   returnList.add(service.getQName());
		   }
		}
		
		return returnList;
	}

	/**
	 * Returns a list of ports for a particular service
	 * the names are QNames
	 * @return
	 */
	public List getPortNameList(QName serviceName){
		List returnList = new ArrayList();
		Service service = wsdlDefinition.getService(serviceName);
		Port port = null; 
		if(service!=null){
		   Map portMap = service.getPorts();
		   if (portMap!=null && !portMap.isEmpty()){
			   Iterator portIterator = portMap.values().iterator();
			   while(portIterator.hasNext()){
				 port = (Port)portIterator.next();
				 returnList.add(port.getName());
			   }
		   }
		  
		}
		
		return returnList;
	}
}
