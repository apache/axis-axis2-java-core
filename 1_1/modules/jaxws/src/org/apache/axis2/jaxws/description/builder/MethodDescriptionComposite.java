/**
 * 
 */
package org.apache.axis2.jaxws.description.builder;

import java.util.List;

public class MethodDescriptionComposite {
	
	//Method reflective information
	private String 		methodName;	//a public method name in this class
	private String 		returnType;	//Methods return type
	private String[]	exceptions;

	boolean	oneWayAnnotated;	
	private WebMethodAnnot	webMethodAnnot;	
	private WebResultAnnot 	webResultAnnot;
	private WebServiceContextAnnot 	webServiceContextAnnot;	
	private HandlerChainAnnot	handlerChainAnnot;	
	private SoapBindingAnnot 	soapBindingAnnot;
	private WebServiceRefAnnot 	webServiceRefAnnot;	
	private WebEndpointAnnot 	webEndpointAnnot;
	private RequestWrapperAnnot requestWrapperAnnot; //TODO EDIT CHECK: only on methods of SEI
	private ResponseWrapperAnnot responseWrapperAnnot;//TODO EDIT CHECK: only on methods of SEI
	private List<ParameterDescriptionComposite> parameterDescriptions;//TODO EDIT CHECK: only on methods of SEI

	/*
	 * Default Constructor
	 */
	public MethodDescriptionComposite () {
		
	}
	
	public MethodDescriptionComposite (	
			String 					methodName,
			String 					returnType,
			WebMethodAnnot 			webMethodAnnot,
			WebResultAnnot 			webResultAnnot,
			boolean 				oneWayAnnotated,
			HandlerChainAnnot 		handlerChainAnnot,
			SoapBindingAnnot 		soapBindingAnnot,
			WebServiceRefAnnot 		webServiceRefAnnot,
			WebEndpointAnnot 		webEndpointAnnot,
			RequestWrapperAnnot 	requestWrapperAnnot,
			ResponseWrapperAnnot 	responseWrapperAnnot,
			WebServiceContextAnnot	webServiceContextAnnot
	) {
		
		this.methodName 			= methodName;
		this.returnType				= returnType;
		this.webMethodAnnot 		= webMethodAnnot;
		this.webResultAnnot 		= webResultAnnot;
		this.oneWayAnnotated 		= oneWayAnnotated;
		this.handlerChainAnnot 		= handlerChainAnnot;
		this.soapBindingAnnot 		= soapBindingAnnot;
		this.webServiceRefAnnot 	= webServiceRefAnnot;
		this.webEndpointAnnot 		= webEndpointAnnot;
		this.requestWrapperAnnot 	= requestWrapperAnnot;
		this.responseWrapperAnnot 	= responseWrapperAnnot;
		this.webServiceContextAnnot = webServiceContextAnnot;
	}
	
	/**
	 * @return Returns the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return Returns the returnType
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * @return returns whether this is OneWay
	 */
	public boolean isOneWay() {
		return oneWayAnnotated;
	}

	/**
	 * @return Returns the webEndpointAnnot.
	 */
	public WebEndpointAnnot getWebEndpointAnnot() {
		return webEndpointAnnot;
	}

	/**
	 * @return Returns the requestWrapperAnnot.
	 */
	public RequestWrapperAnnot getRequestWrapperAnnot() {
		return requestWrapperAnnot;
	}

	/**
	 * @return Returns the responseWrapperAnnot.
	 */
	public ResponseWrapperAnnot getResponseWrapperAnnot() {
		return responseWrapperAnnot;
	}

	/**
	 * @return Returns the webServiceContextAnnot.
	 */
	public WebServiceContextAnnot getWebServiceContextAnnot() {
		return webServiceContextAnnot;
	}

	/**
	 * @return Returns the handlerChainAnnot.
	 */
	public HandlerChainAnnot getHandlerChainAnnot() {
		return handlerChainAnnot;
	}

	/**
	 * @return Returns the soapBindingAnnot.
	 */
	public SoapBindingAnnot getSoapBindingAnnot() {
		return soapBindingAnnot;
	}

	/**
	 * @return Returns the webMethodAnnot.
	 */
	public WebMethodAnnot getWebMethodAnnot() {
		return webMethodAnnot;
	}

	/**
	 * @return Returns the webResultAnnot.
	 */
	public WebResultAnnot getWebResultAnnot() {
		return webResultAnnot;
	}

	/**
	 * @return Returns the webServiceRefAnnot.
	 */
	public WebServiceRefAnnot getWebServiceRefAnnot() {
		return webServiceRefAnnot;
	}

	/**
	 * @return Returns the exceptions.
	 */
	public String[] getExceptions() {
		return exceptions;
	}

	/**
	 * @param methodName The methodName to set.
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @param returnType The returnType to set.
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * @param oneWayAnnotated The oneWay boolean to set
	 */
	public void setOneWayAnnot(boolean oneWayAnnotated) {
		this.oneWayAnnotated = oneWayAnnotated;
	}

	/**
	 * @param webEndpointAnnotImpl The webEndpointAnnotImpl to set.
	 */
	public void setWebEndpointAnnot(WebEndpointAnnot webEndpointAnnot) {
		this.webEndpointAnnot = webEndpointAnnot;
	}

	/**
	 * @param requestWrapperAnnot The requestWrapperAnnot to set.
	 */
	public void setRequestWrapperAnnot(
			RequestWrapperAnnot requestWrapperAnnot) {
		this.requestWrapperAnnot = requestWrapperAnnot;
	}

	/**
	 * @param responseWrapperAnnot The responseWrapperAnnot to set.
	 */
	public void setResponseWrapperAnnot(
			ResponseWrapperAnnot responseWrapperAnnot) {
		this.responseWrapperAnnot = responseWrapperAnnot;
	}
	
	/**
	 * @param webServiceContextAnnot The webServiceContextAnnot to set.
	 */
	private void setWebServiceContextAnnot(WebServiceContextAnnot webServiceContextAnnot) {
		this.webServiceContextAnnot = webServiceContextAnnot;
	}


	/**
	 * @param handlerChainAnnot The handlerChainAnnot to set.
	 */
	public void setHandlerChainAnnot(HandlerChainAnnot handlerChainAnnot) {
		this.handlerChainAnnot = handlerChainAnnot;
	}

	/**
	 * @param soapBindingAnnot The soapBindingAnnot to set.
	 */
	public void setSoapBindingAnnot(SoapBindingAnnot soapBindingAnnot) {
		this.soapBindingAnnot = soapBindingAnnot;
	}

	/**
	 * @param webMethodAnnot The webMethodAnnot to set.
	 */
	public void setWebMethodAnnot(WebMethodAnnot webMethodAnnot) {
		this.webMethodAnnot = webMethodAnnot;
	}

	/**
	 * @param webResultAnnot The webResultAnnot to set.
	 */
	public void setWebResultAnnot(WebResultAnnot webResultAnnot) {
		this.webResultAnnot = webResultAnnot;
	}

	/**
	 * @param webServiceRefAnnot The webServiceRefAnnot to set.
	 */
	public void setWebServiceRefAnnot(WebServiceRefAnnot webServiceRefAnnot) {
		this.webServiceRefAnnot = webServiceRefAnnot;
	}
	
	/**
	 *  @param parameterDescription The parameterDescription to add to the set.
	 */
	public void addParameterDescriptionComposite(ParameterDescriptionComposite parameterDescription) {
		parameterDescriptions.add(parameterDescription);
	}
	
	/**
	 *  @param parameterDescription The parameterDescription to add to the set.
	 *  @param index The index at which to place this parameterDescription
	 */
	public void addParameterDescriptionComposite(ParameterDescriptionComposite parameterDescription, int index) {
		parameterDescriptions.add(index, parameterDescription);
	}
	
	/**
	 *  @param parameterDescription The parameterDescription to add to the set.
	 */
	public void setParameterDescriptionCompositeList(List<ParameterDescriptionComposite> parameterDescriptionList) {
		this.parameterDescriptions = parameterDescriptionList;
	}
	
	/**
	 *  @param parameterDescription The parameterDescription to add to the set.
	 */
	public ParameterDescriptionComposite getParameterDescriptionComposite(int index) {
		return parameterDescriptions.get(index);
	}

	/**
	 * @param exceptions The exceptions to set.
	 */
	public void setExceptions(String[] exceptions) {
		this.exceptions = exceptions;
	}
}
