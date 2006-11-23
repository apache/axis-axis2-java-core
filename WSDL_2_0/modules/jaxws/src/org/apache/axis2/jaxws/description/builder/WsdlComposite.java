package org.apache.axis2.jaxws.description.builder;

import java.util.HashMap;
import javax.wsdl.Definition;
import org.apache.axiom.om.OMDocument;

public class WsdlComposite {

	Definition wsdlDefinition;
	
	HashMap <String, OMDocument> schemaMap;
	
	String wsdlFileName;

	public WsdlComposite() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return Returns the schemaMap.
	 */
	public HashMap<String, OMDocument> getSchemaMap() {
		return schemaMap;
	}

	/**
	 * @return Returns the wsdlDefinition.
	 */
	public Definition getWsdlDefinition() {
		return wsdlDefinition;
	}

	/**
	 * @return Returns the wsdlFileName.
	 */
	public String getWsdlFileName() {
		return wsdlFileName;
	}

	/**
	 * @param schemaMap The schemaMap to set.
	 */
	public void setSchemaMap(HashMap<String, OMDocument> schemaMap) {
		this.schemaMap = schemaMap;
	}

	/**
	 * @param wsdlDefinition The wsdlDefinition to set.
	 */
	public void setWsdlDefinition(Definition wsdlDefinition) {
		this.wsdlDefinition = wsdlDefinition;
	}

	/**
	 * @param wsdlFileName The wsdlFileName to set.
	 */
	public void setWsdlFileName(String wsdlFileName) {
		this.wsdlFileName = wsdlFileName;
	}
	
	
}
