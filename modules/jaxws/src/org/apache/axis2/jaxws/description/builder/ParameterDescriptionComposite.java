/**
 * 
 */
package org.apache.axis2.jaxws.description.builder;

public class ParameterDescriptionComposite {
	
	private String 					parameterType;	
	private WebParamAnnot			webParamAnnot;
	private WebServiceRefAnnot 		webServiceRefAnnot;
	private WebServiceContextAnnot	webServiceContextAnnot;
	private int listOrder; //represents this composites order in the list

	public ParameterDescriptionComposite () {
		
	}
	
	public ParameterDescriptionComposite (	
			String 					parameterType,
			WebParamAnnot 			webParamAnnot,
			WebServiceRefAnnot 		webServiceRefAnnot,
			WebServiceContextAnnot	webServiceContextAnnot) {

		this.parameterType 			= parameterType;
		this.webParamAnnot 			= webParamAnnot;
		this.webServiceRefAnnot 	= webServiceRefAnnot;
		this.webServiceContextAnnot = webServiceContextAnnot;
	}
	

	/**
	 * @return Returns the parameterType.
	 */
	public String getParameterType() {
		return parameterType;
	}

	/**
	 * @return Returns the webParamAnnot.
	 */
	public WebParamAnnot getWebParamAnnot() {
		return webParamAnnot;
	}

	/**
	 * @return Returns the webServiceRefAnnot.
	 */
	public WebServiceRefAnnot getWebServiceRefAnnot() {
		return webServiceRefAnnot;
	}

	/**
	 * @return Returns the webServiceContextAnnot.
	 */
	public WebServiceContextAnnot getWebServiceContextAnnot() {
		return webServiceContextAnnot;
	}

	/**
	 * @return Returns the webServiceContextAnnot.
	 */
	public int getListOrder() {
		return listOrder;
	}

	/**
	 * @param parameterType The parameterType to set.
	 */
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * @param webParamAnnot The webParamAnnot to set.
	 */
	public void setWebParamAnnot(WebParamAnnot webParamAnnot) {
		this.webParamAnnot = webParamAnnot;
	}

	/**
	 * @param webServiceRefAnnot The webServiceRefAnnot to set.
	 */
	public void setWebServiceRefAnnot(WebServiceRefAnnot webServiceRefAnnot) {
		this.webServiceRefAnnot = webServiceRefAnnot;
	}

	/**
	 * @param webServiceContextAnnot The webServiceContextAnnot to set.
	 */
	public void setWebServiceContextAnnot(WebServiceContextAnnot webServiceContextAnnot) {
		this.webServiceContextAnnot = webServiceContextAnnot;
	}

	/**
	 * @param webServiceContextAnnot The webServiceContextAnnot to set.
	 */
	public void setListOrder(int listOrder) {
		this.listOrder = listOrder;
	}

	/**
	 * Convenience method for unit testing. We will print all of the 
	 * data members here.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String newLine = "\n";
		sb.append("***** BEGIN ParameterDescriptionComposite *****");
		sb.append("PDC.parameterType= " + parameterType);
		sb.append(newLine);
		if(webParamAnnot != null) {
			sb.append("\t @WebParam");
			sb.append(newLine);
			sb.append("\t" + webParamAnnot.toString());
		}
		sb.append(newLine);
		if(webServiceRefAnnot != null) {
			sb.append("\t @WebServiceRef");
			sb.append(newLine);
			sb.append("\t" + webServiceRefAnnot.toString());
		}
		sb.append(newLine);
		sb.append("***** END ParameterDescriptionComposite *****");
		return sb.toString();
	}
}
