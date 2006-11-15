/**
 * 
 */
package org.apache.axis2.jaxws.description.builder;


public class FieldDescriptionComposite {
	
	//Method reflective information
	private String 	fieldName;		//field name
	private String 	modifierType;	//field modifier

	private HandlerChainAnnot	handlerChainAnnot;	
	private WebServiceRefAnnot 	webServiceRefAnnot;	

	/*
	 * Default Constructor
	 */
	public FieldDescriptionComposite () {
		
	}
	
	public FieldDescriptionComposite (	
			String 					fieldName,
			String 					modifierType,
			HandlerChainAnnot 		handlerChainAnnot,
			WebServiceRefAnnot 		webServiceRefAnnot
	) {
		this.fieldName = fieldName;
		this.modifierType = modifierType;
		this.handlerChainAnnot = handlerChainAnnot;
		this.webServiceRefAnnot = webServiceRefAnnot;
	}

	/**
	 * @return Returns the fieldName.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @return Returns the handlerChainAnnot.
	 */
	public HandlerChainAnnot getHandlerChainAnnot() {
		return handlerChainAnnot;
	}

	/**
	 * @return Returns the modifierType.
	 */
	public String getModifierType() {
		return modifierType;
	}

	/**
	 * @return Returns the webServiceRefAnnot.
	 */
	public WebServiceRefAnnot getWebServiceRefAnnot() {
		return webServiceRefAnnot;
	}

	/**
	 * @param fieldName The fieldName to set.
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @param handlerChainAnnot The handlerChainAnnot to set.
	 */
	public void setHandlerChainAnnot(HandlerChainAnnot handlerChainAnnot) {
		this.handlerChainAnnot = handlerChainAnnot;
	}

	/**
	 * @param modifierType The modifierType to set.
	 */
	public void setModifierType(String modifierType) {
		this.modifierType = modifierType;
	}

	/**
	 * @param webServiceRefAnnot The webServiceRefAnnot to set.
	 */
	public void setWebServiceRefAnnot(WebServiceRefAnnot webServiceRefAnnot) {
		this.webServiceRefAnnot = webServiceRefAnnot;
	}
}
	
