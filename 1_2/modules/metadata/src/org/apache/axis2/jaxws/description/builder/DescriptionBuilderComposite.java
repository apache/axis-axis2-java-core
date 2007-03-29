/**
 * 
 */
package org.apache.axis2.jaxws.description.builder;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Definition;

public class DescriptionBuilderComposite implements TMAnnotationComposite, TMFAnnotationComposite {
	/*
	 * This structure contains the full reflected class, as well as, the
	 * possible annotations found for this class...the class description 
	 * must be complete enough for full validation between class info and annotations
	 * The annotations will be added to the corresponding class members.
	 */

	public DescriptionBuilderComposite () {
		
		methodDescriptions 		= new ArrayList<MethodDescriptionComposite>();
		fieldDescriptions 		= new ArrayList<FieldDescriptionComposite>();
		webServiceRefAnnotList 	= new ArrayList<WebServiceRefAnnot>();	
		interfacesList 			= new ArrayList<String>();
	}

	//Class type within the module
	public static enum ModuleClassType { SERVICEIMPL, SEI, SERVICE, SUPER, PROVIDER, FAULT}
	private ModuleClassType moduleClassType = null;
	
	//Note: a WSDL is not necessary
	private Definition 	wsdlDefinition = null;
	private URL			wsdlURL = null;

	// Class-level annotations
	private WebServiceAnnot 			webServiceAnnot;	
	private WebServiceProviderAnnot 	webServiceProviderAnnot;
	private ServiceModeAnnot 			serviceModeAnnot;	
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
	private boolean			isInterface = false;
	
	private List<MethodDescriptionComposite> methodDescriptions;		
	private List<FieldDescriptionComposite> fieldDescriptions;		
	
	private WsdlGenerator	wsdlGenerator;
	private ClassLoader		classLoader;

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
	 * @return Returns the webServiceRefAnnot list.
	 */
	public List<WebServiceRefAnnot> getAllWebServiceRefAnnots() {		
		return webServiceRefAnnotList;
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
	 * @return Returns the wsdlDefinition
	 */
	public Definition getWsdlDefinition() {
		return wsdlDefinition;
	}
	
	/**
	 * @return Returns the wsdlURL
	 */
	public URL getWsdlURL() {
		return this.wsdlURL;
	}
    /** Returns a collection of all MethodDescriptionComposites that match the 
     * specified name 
     */
    public List<MethodDescriptionComposite> getMethodDescriptionComposite(String methodName) {
        ArrayList<MethodDescriptionComposite> matchingMethods = new ArrayList<MethodDescriptionComposite>();
        Iterator<MethodDescriptionComposite> iter = methodDescriptions.iterator();
        while(iter.hasNext()) {
            MethodDescriptionComposite composite = iter.next();
            
            if (composite.getMethodName() != null) {
                if (composite.getMethodName().equals(methodName)){
                    matchingMethods.add(composite);
                }
            }
        }
        
        return matchingMethods;
    }

    /**
     * Returns the nth occurence of this MethodComposite. Since
     * method names are not unique, we have to account for multiple occurrences
     *
     * @param methodName
     * @param occurence The nth occurance to return; not this is NOT 0 based
     * @return Returns the methodDescriptionComposite
     */
    public MethodDescriptionComposite getMethodDescriptionComposite(
                        String  methodName,
                        int     occurence) {
        MethodDescriptionComposite returnMDC = null;
        List<MethodDescriptionComposite> matchingMethods = getMethodDescriptionComposite(methodName);
        if (matchingMethods != null && !matchingMethods.isEmpty() && 
            occurence > 0 && occurence <= matchingMethods.size() ) {
            returnMDC = matchingMethods.get(--occurence);
        }
        return returnMDC;
    }
	
	public List<MethodDescriptionComposite> getMethodDescriptionsList() {
		return methodDescriptions;
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
	
	/**
	 * @return Returns the ModuleClassType.
	 */
	public WsdlGenerator getCustomWsdlGenerator() {
		
		return this.wsdlGenerator;
	}	

	/**
	 * @return Returns the ClassLoader.
	 */
	public ClassLoader getClassLoader() {
		
		return this.classLoader;
	}	

	/**
	 *
	 * @return Returns true if this is an interface
	 */
	public boolean isInterface(){

		return isInterface;
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
	public void setInterfacesList(List<String> interfacesList) {
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
	
	public void setWebServiceRefAnnot(WebServiceRefAnnot webServiceRefAnnot) {
		addWebServiceRefAnnot(webServiceRefAnnot);
	}

	/**
	 * @param wsdlDefinition The wsdlDefinition to set.
	 */
	public void setWsdlDefinition(Definition wsdlDefinition) {
		this.wsdlDefinition = wsdlDefinition;
	}

	/**
	 * @param wsdlURL The wsdlURL to set.
	 */
	public void setwsdlURL(URL wsdlURL) {
		this.wsdlURL = wsdlURL;
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
	 * @param isInterface Sets whether this composite represents a class or interface
	 */
	public void setIsInterface(boolean isInterface){
		this.isInterface = isInterface;
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
		
	/**
	 * @return Returns the ModuleClassType.
	 */
	public ModuleClassType getClassType() {
		
		if (moduleClassType == null) {
			//TODO: Determine the class type
		}
		return moduleClassType;
	}
	
	/**
	 * @return Returns the ModuleClassType.
	 */
	public void setCustomWsdlGenerator(WsdlGenerator wsdlGenerator) {
		
		this.wsdlGenerator = wsdlGenerator;
	}
	
	/**
	 * @return Returns the ModuleClassType.
	 */
	public void setClassLoader(ClassLoader classLoader) {
		
		this.classLoader = classLoader;
	}
	
	/**
	 * Convenience method for unit testing. We will print all of the 
	 * data members here.
	 */
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		final String newLine = "\n";
        final String sameLine = "; ";
        sb.append(super.toString());
		sb.append(newLine);
		sb.append("ClassName: " + className);
		sb.append(sameLine);
		sb.append("SuperClass:" + extendsClass);
		
        sb.append(newLine);
        sb.append("Class modifiers: "); 
		if(classModifiers != null) {
			for(int i=0; i < classModifiers.length; i++) {
				sb.append(classModifiers[i]);
				sb.append(sameLine);
			}
		}
	
        sb.append(newLine);
        sb.append("Interfaces: ");
		Iterator<String> intIter = interfacesList.iterator();
		while(intIter.hasNext()) {
			String inter = intIter.next();
			sb.append(inter);
			sb.append(sameLine);
		}
		
		if(webServiceAnnot != null) {
            sb.append(newLine);
            sb.append("WebService: ");
			sb.append(webServiceAnnot.toString());
		}
		
		if(webServiceProviderAnnot != null) {
            sb.append(newLine);
            sb.append("WebServiceProvider: ");
			sb.append(webServiceProviderAnnot.toString());
		}
        
		if(bindingTypeAnnot != null) {
            sb.append(newLine);
            sb.append("BindingType: ");
			sb.append(bindingTypeAnnot.toString());
		}
        
		if(webServiceClientAnnot != null) {
            sb.append(newLine);
            sb.append("WebServiceClient: ");
			sb.append(webServiceClientAnnot.toString());
		}
        
		if(webFaultAnnot != null) {
            sb.append(newLine);
            sb.append("WebFault: ");
			sb.append(webFaultAnnot.toString());
		}
        
		if(serviceModeAnnot != null) {
            sb.append(newLine);
            sb.append("ServiceMode: ");
			sb.append(serviceModeAnnot.toString());
		}
        
		if(soapBindingAnnot != null) {
            sb.append(newLine);
            sb.append("SOAPBinding: ");
			sb.append(soapBindingAnnot.toString());
		}
        
		if(handlerChainAnnot != null) {
            sb.append(newLine);
            sb.append("HandlerChain: ");
			sb.append(handlerChainAnnot.toString());
		}
		
        if (webServiceRefAnnotList.size() > 0) {
            sb.append(newLine);
            sb.append("Number of WebServiceRef:  " + webServiceRefAnnotList.size());
            Iterator<WebServiceRefAnnot> wsrIter = webServiceRefAnnotList.iterator();
            while(wsrIter.hasNext()) {
                WebServiceRefAnnot wsr = wsrIter.next();
                sb.append(wsr.toString());
                sb.append(sameLine);
            }
        }
        
		sb.append(newLine);
        sb.append("Number of Method Descriptions: " + methodDescriptions.size());
		Iterator<MethodDescriptionComposite> mdcIter =  methodDescriptions.iterator();
		while(mdcIter.hasNext()) {
            sb.append(newLine);
			MethodDescriptionComposite mdc = mdcIter.next();
			sb.append(mdc.toString());
		}
		
        sb.append(newLine);
        sb.append("Number of Field Descriptions: " + fieldDescriptions.size());
		Iterator<FieldDescriptionComposite> fdcIter = fieldDescriptions.iterator();
		while(fdcIter.hasNext()) {
            sb.append(newLine);
			FieldDescriptionComposite fdc = fdcIter.next();
			sb.append(fdc.toString());
		}
		return sb.toString();
	}
	
}
