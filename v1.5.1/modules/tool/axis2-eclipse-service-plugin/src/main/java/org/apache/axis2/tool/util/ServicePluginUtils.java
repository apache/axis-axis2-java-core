package org.apache.axis2.tool.util;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.axis2.tool.service.eclipse.plugin.ServiceArchiver;
import org.apache.axis2.tool.util.Constants.ServiceConstants;

public class ServicePluginUtils {
	/**
	 * Validates the given xml file against the axis2 services schema. 
	 * @return return true if the xml is valid
	 */
	public static boolean isServicesXMLValid(String servicesXmlPath){
        SchemaFactory factory = 
            SchemaFactory.newInstance(ServiceConstants.XML_SCHEMA);
        
        try {
        	String resourcePath=addAnotherNodeToPath(
        			ServiceConstants.RESOURCE_FOLDER, ServiceConstants.SERVICES_XSD_SCHEMA_NAME);
            Schema schema = factory.newSchema(
            		 ServiceArchiver.getDefault().getBundle().getEntry(resourcePath));
            Validator validator = schema.newValidator();
            Source source = new StreamSource(new File(servicesXmlPath));
            validator.validate(source);
            return true;
        }
        catch (Exception ex) {
            return false;
        }  
	}
	
	public static String addAnotherNodeToPath(String currentPath, String newNode) {
		return currentPath + File.separator + newNode;
	}
}
