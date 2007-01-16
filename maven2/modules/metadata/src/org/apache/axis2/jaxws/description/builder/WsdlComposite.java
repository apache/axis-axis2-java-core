package org.apache.axis2.jaxws.description.builder;

import java.util.HashMap;

import javax.wsdl.Definition;

import org.apache.axiom.om.OMDocument;

public class WsdlComposite {

	HashMap <String, Definition> wsdlDefinitionsMap;
	
	HashMap <String, OMDocument> schemaMap;
	
	String wsdlFileName;

	public WsdlComposite() {
		super();
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
	public HashMap<String, Definition> getWsdlDefinitionsMap() {
		return wsdlDefinitionsMap;
	}
	
	/**
	 * @return Returns the root WSDL Definition
	 */
	public Definition getRootWsdlDefinition() {
		
		return wsdlDefinitionsMap.get(getWsdlFileName().toLowerCase());
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
	public void setWsdlDefinition(HashMap<String, Definition> wsdlDefinitionsMap) {
		this.wsdlDefinitionsMap = wsdlDefinitionsMap;
	}

	/**
	 * @param wsdlFileName The wsdlFileName to set.
	 */
	public void setWsdlFileName(String wsdlFileName) {
		this.wsdlFileName = wsdlFileName;
	}
	
	
}
