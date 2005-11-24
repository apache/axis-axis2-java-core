package org.apache.axis2.jaxws.description;

import javax.xml.ws.ParameterMode;
import javax.xml.namespace.QName;

public class ParameterDesc {

	private QName name;
	
	private QName xmlType;
	
	private Class javaType;
	
	private ParameterMode mode = ParameterMode.IN;
	
	public ParameterDesc() {
		super();
	}

	/**
	 * @return Returns the javaType.
	 */
	public Class getJavaType() {
		return javaType;
	}

	/**
	 * @param javaType The javaType to set.
	 */
	public void setJavaType(Class javaType) {
		this.javaType = javaType;
	}

	/**
	 * @return Returns the mode.
	 */
	public ParameterMode getMode() {
		return mode;
	}
	
	/**
	 * @param mode The mode to set.
	 */
	public void setMode(ParameterMode mode) {
		this.mode = mode;
	}
	
	/**
	 * @return Returns the xmlType.
	 */
	public QName getXmlType() {
		return xmlType;
	}

	/**
	 * @param xmlType The xmlType to set.
	 */
	public void setXmlType(QName xmlType) {
		this.xmlType = xmlType;
	}

	/**
	 * @return Returns the name.
	 */
	public QName getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(QName name) {
		this.name = name;
	}

}
