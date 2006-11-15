/**
 * 
 */
package org.apache.axis2.jaxws.description.builder;

import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;

public class DescriptionBuilderComposite {
	/*
	 * This structure contains the full reflected class, as well as, the
	 * possible annotations found for this class...the class description 
	 * must be complete enough for full validation between class info and annotations
	 * The annotations will be added to the corresponding class members.
	 */

	public DescriptionBuilderComposite () {
		
	}

	//Class type within the module
	private enum ModuleClassType { SERVICEIMPL, SEI, SERVICE, SUPER, PROVIDER, FAULT }
	
	//Note: a WSDL is not necessary
	private Definition	wsdlDefinition;

	// Class-level annotations
	private WebServiceAnnot 			webServiceAnnot;	
	private WebServiceProviderAnnot 	webServiceProviderAnnot; //	TODO EDIT CHECK: WebService and WebServiceProvider are mutually exclusive
	private ServiceModeAnnot 			serviceModeAnnot;	//	TODO EDIT CHECK: valid only if is a provider class	
	private WebServiceClientAnnot 		webServiceClientAnnot;
	private WebFaultAnnot 				webFaultAnnot;
	private HandlerChainAnnot 			handlerChainAnnot;
	private SoapBindingAnnot 			soapBindingAnnot;
	private List<WebServiceRefAnnot> 	webServiceRefAnnotList;
	private BindingTypeAnnot 			bindingTypeAnnot;
	private WebServiceContextAnnot 		webServiceContextAnnot;
	
	// Class information
	private String 			className;
	private String[] 		classModifiers; //public, abstract, final, strictfp...
	private String			extendsClass;	//Set to the name of the super class
	private List<String>	interfacesList; //Set this for all implemented interfaces

	private List<MethodDescriptionComposite> methodDescriptions;		
	private List<FieldDescriptionComposite> fieldDescriptions;		

	// Methods
	public WebServiceAnnot getWebServiceAnnot() {
		return this.webServiceAnnot;
	}

	/**
	 * @return Returns the classModifiers.
	 */
	public String[] getClassModifiers() {
		return classModifiers;
	}

	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return Returns the super class name.
	 */
	public String getSuperClassName() {
		return extendsClass;
	}

	/**
	 * @return Returns the list of implemented interfaces.
	 */
	public List<String> getInterfacesList() {
		return interfacesList;
	}

	/**
	 * @return Returns the handlerChainAnnotImpl.
	 */
	public HandlerChainAnnot getHandlerChainAnnot() {
		return handlerChainAnnot;
	}

	/**
	 * @return Returns the serviceModeAnnot.
	 */
	public ServiceModeAnnot getServiceModeAnnot() {
		return serviceModeAnnot;
	}

	/**
	 * @return Returns the soapBindingAnnot.
	 */
	public SoapBindingAnnot getSoapBindingAnnot() {
		return soapBindingAnnot;
	}

	/**
	 * @return Returns the webFaultAnnot.
	 */
	public WebFaultAnnot getWebFaultAnnot() {
		return webFaultAnnot;
	}

	/**
	 * @return Returns the webServiceClientAnnot.
	 */
	public WebServiceClientAnnot getWebServiceClientAnnot() {
		return webServiceClientAnnot;
	}

	/**
	 * @return Returns the webServiceProviderAnnot.
	 */
	public WebServiceProviderAnnot getWebServiceProviderAnnot() {
		return webServiceProviderAnnot;
	}

	/**
	 * @return Returns the webServiceRefAnnot.
	 */
	public WebServiceRefAnnot getWebServiceRefAnnot(String name) {
		
		WebServiceRefAnnot wsra = null;
		Iterator<WebServiceRefAnnot> iter = 
							webServiceRefAnnotList.iterator();
			
		while(iter.hasNext()) {
			wsra = iter.next();
			if (wsra.name().equals(name))
				return wsra;
		}
		return wsra;
	}

	/**
	 * @return Returns the webServiceRefAnnot.
	 */
	public BindingTypeAnnot getBindingTypeAnnot() {
		return bindingTypeAnnot;
	}

	/**
	 * @return Returns the webServiceContextAnnot.
	 */
	public WebServiceContextAnnot getWebServiceContextAnnot() {
		return webServiceContextAnnot;
	}

	/**
	 * @return Returns the wsdlDefinition.
	 */
	public Definition getWsdlDefinition() {
		return wsdlDefinition;
	}
	
	/**
	 * Returns the nth occurence of this MethodComposite. Since
	 * method names are not unique, we have to account for multiple occurrences
	 *
	 * @return Returns the methodDescriptionComposite.
	 */
	public MethodDescriptionComposite getMethodDescriptionComposite(
						String 	methodName,
						int		occurence) {

		MethodDescriptionComposite composite = null;
		Iterator<MethodDescriptionComposite> iter = 
							methodDescriptions.iterator();
		int hits = 0;
		while(iter.hasNext()) {
			composite = iter.next();
			
			if (composite.getMethodName().equals(methodName)){
				hits++;
				if (hits == occurence)
					return composite;
			}
		}
		
		return composite;
	}
	
	/**
	 *
	 * @return Returns the methodDescriptionComposite..null if not found
	 */
	public FieldDescriptionComposite getFieldDescriptionComposite(String fieldName){

		FieldDescriptionComposite composite = null;
		Iterator<FieldDescriptionComposite> iter = 
							fieldDescriptions.iterator();
			
		while(iter.hasNext()) {
			composite = iter.next();
			if (composite.getFieldName().equals(fieldName))
				return composite;
		}
		return composite;
	}
	
	//++++++++
	//Setters
	//++++++++
	public void setWebServiceAnnot(WebServiceAnnot webServiceAnnot) {
		this.webServiceAnnot = webServiceAnnot;
	}

	/**
	 * @param classModifiers The classModifiers to set.
	 */
	public void setClassModifiers(String[] classModifiers) {
		this.classModifiers = classModifiers;
	}

	/**
	 * @param className The className to set.
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * @param extendsClass The name of the super class to set.
	 */
	public void setSuperClassName(String extendsClass) {
		this.extendsClass = extendsClass;
	}

	/**
	 * @param interfacesList  The interfacesList to set.
	 */
	public void getInterfacesList(List<String> interfacesList) {
		this.interfacesList = interfacesList;
	}
	
	/**
	 * @param handlerChainAnnot The handlerChainAnnot to set.
	 */
	public void setHandlerChainAnnot(HandlerChainAnnot handlerChainAnnot) {
		this.handlerChainAnnot = handlerChainAnnot;
	}

	/**
	 * @param serviceModeAnnot The serviceModeAnnot to set.
	 */
	public void setServiceModeAnnot(ServiceModeAnnot serviceModeAnnot) {
		this.serviceModeAnnot = serviceModeAnnot;
	}

	/**
	 * @param soapBindingAnnot The soapBindingAnnot to set.
	 */
	public void setSoapBindingAnnot(SoapBindingAnnot soapBindingAnnot) {
		this.soapBindingAnnot = soapBindingAnnot;
	}

	/**
	 * @param webFaultAnnot The webFaultAnnot to set.
	 */
	public void setWebFaultAnnot(WebFaultAnnot webFaultAnnot) {
		this.webFaultAnnot = webFaultAnnot;
	}

	/**
	 * @param webServiceClientAnnot The webServiceClientAnnot to set.
	 */
	public void setWebServiceClientAnnot(
			WebServiceClientAnnot webServiceClientAnnot) {
		this.webServiceClientAnnot = webServiceClientAnnot;
	}

	/**
	 * @param webServiceProviderAnnot The webServiceProviderAnnot to set.
	 */
	public void setWebServiceProviderAnnot(
			WebServiceProviderAnnot webServiceProviderAnnot) {
		this.webServiceProviderAnnot = webServiceProviderAnnot;
	}

	/**
	 * @param webServiceRefAnnot The webServiceRefAnnot to add to the list.
	 */
	public void addWebServiceRefAnnot(
			WebServiceRefAnnot webServiceRefAnnot) {
		webServiceRefAnnotList.add(webServiceRefAnnot);
	}

	/**
	 * @param wsdlDefinition The wsdlDefinition to set.
	 */
	public void setWsdlDefinition(Definition wsdlDefinition) {
		this.wsdlDefinition = wsdlDefinition;
	}

	/**
	 * @param BindingTypeAnnot The BindingTypeAnnot to set.
	 */
	public void setBindingTypeAnnot(
			BindingTypeAnnot bindingTypeAnnot) {
		this.bindingTypeAnnot = bindingTypeAnnot;
	}

	/**
	 * @param webServiceContextAnnot The webServiceContextAnnot to set.
	 */
	public void setWebServiceContextAnnot(
			WebServiceContextAnnot webServiceContextAnnot) {
		this.webServiceContextAnnot = webServiceContextAnnot;
	}

	/**
	 *  @param methodDescription The methodDescription to add to the set.
	 */
	public void addMethodDescriptionComposite(MethodDescriptionComposite methodDescription) {
		methodDescriptions.add(methodDescription);
	}
	
	/**
	 *  @param methodDescription The methodDescription to add to the set.
	 */
	public void addFieldDescriptionComposite(FieldDescriptionComposite fieldDescription) {
		fieldDescriptions.add(fieldDescription);
	}
	
}
