/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.axis2.jaxws.description.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.WebFault;

import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.FaultDescriptionJava;
import org.apache.axis2.jaxws.description.FaultDescriptionWSDL;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.util.XMLRootElementUtil;

/**
 * @see ../FaultDescription
 *
 */


class FaultDescriptionImpl implements FaultDescription, FaultDescriptionJava, FaultDescriptionWSDL {
    
    private Class exceptionClass;
    private DescriptionBuilderComposite composite;
    private WebFault annotation;
    private OperationDescription parent;
    
    
    private String name = "";  // WebFault.name
    private String faultBean = "";  // WebFault.faultBean
    private String targetNamespace = ""; // WebFault.targetNamespace
    
    private static final String FAULT = "Fault";


    /**
     * The FaultDescriptionImpl class will only be used to describe exceptions declared to be thrown by a service that has
     * a WebFault annotation.  No generic exception should ever have a FaultDescription associated with it.  It is the
     * responsibility of the user of the FaultDescriptionImpl class to avoid instantiating this object for non-annotated
     * generic exceptions.
     * 
     * @param exceptionClass an exception declared to be thrown by the service on which this FaultDescription may apply.
     * @param beanName fully qualified package+classname of the bean associated with this exception
     * @param annotation the WebFault annotation object on this exception class
     * @param parent the OperationDescription that is the parent of this FaultDescription
     */
    FaultDescriptionImpl(Class exceptionClass, WebFault annotation, OperationDescription parent) {
        this.exceptionClass = exceptionClass;
        this.annotation = annotation;
        this.parent = parent;
    }

    FaultDescriptionImpl(DescriptionBuilderComposite faultDBC, OperationDescription parent) {
        this.composite = faultDBC;
        this.parent = parent;
    }

    public WebFault getAnnoWebFault() {
        
    	if (annotation == null) {
            if (isDBC()) {
            	annotation = this.composite.getWebFaultAnnot();              
            }
        }
    	
        return annotation;
    }

    
    public String getFaultBean() {
        if (faultBean.length() > 0) {
            // Return the faultBean if it was already calculated
            return faultBean;
        } else {
            // Load up the WebFault annotation and get the faultBean.
            // @WebFault may not be present
            WebFault annotation = getAnnoWebFault();
            
            if (annotation != null && annotation.faultBean() != null && 
                annotation.faultBean().length() > 0) {
                faultBean = annotation.faultBean();
            } else {
                // We don't have a faultBean. Try looking at the getFaultInfo method.
                if (!isDBC()) {
                    try {
                        Method method = exceptionClass.getMethod("getFaultInfo", null);
                        faultBean = method.getReturnType().getCanonicalName();
                    } catch (Exception e) {
                        // This must be a legacy exception
                    }
                }  else {
                    MethodDescriptionComposite mdc = 
                        composite.getMethodDescriptionComposite("getFaultInfo", 1);
                    if (mdc != null) {
                        faultBean = mdc.getReturnType();
                    }
                }
                
                // Still not found, this must be a non-compliant exception.
                // Use the JAX-WS chap 3.7 algorithm
                if (faultBean.length() <= 0) {
                    String simpleName = getSimpleName(getExceptionClassName()) + "Bean";
                    String packageName = null;
                    if (!isDBC()) {
                        Class clazz = getOperationDescription().getSEIMethod().getDeclaringClass();
                        packageName = clazz.getPackage().getName();
                    } else {
                        // Similar algorithm as the OperationDesc RequestWrapper and ResponseWrapper
                        String declaringClazz = ((OperationDescriptionImpl )getOperationDescription()).getMethodDescriptionComposite().getDeclaringClass();
                        packageName = declaringClazz.substring(0, declaringClazz.lastIndexOf("."));
                    }
                    faultBean = packageName + "." + simpleName;
                    
                    // The following call adjusts the package name if necessary
                    faultBean = DescriptionUtils.determineActualAritfactPackage(faultBean);
                }
            }
        }
        return faultBean;
    }

    public String getName() {
        if (name.length() > 0) {
            return name;
        } else {
            // Load the annotation. The annotation may not be present in WSGen cases
            WebFault annotation = this.getAnnoWebFault();
            if (annotation != null && 
                annotation.name().length() > 0) {
                name = annotation.name();
            } else {
                // The default is the name on the @XmlRootElement of the FaultBean since this
                // is what is flowed over the wire.
                try {
                    Class clazz = DescriptionUtils.loadClass(getFaultBean());
                    XmlRootElement root = (XmlRootElement) clazz.getAnnotation(XmlRootElement.class);
                    name = root.name();
                } catch (Exception e) {
                    // All else fails use the faultBean name
                    name = getSimpleName(getFaultBean());
                }
            }
        }
        return name;
    }

    public String getTargetNamespace() {
        if (targetNamespace.length() > 0) {
            return targetNamespace;
        } else {
            // Load the annotation. The annotation may not be present in WSGen cases
            WebFault annotation = this.getAnnoWebFault();
            if (annotation != null && 
                annotation.targetNamespace().length() > 0) {
                targetNamespace = annotation.targetNamespace();
            } else {
                // The default is the namespace on the @XmlRootElement of the FaultBean since this
                // is what is flowed over the wire.
                try {
                    Class clazz = DescriptionUtils.loadClass(getFaultBean());
                    targetNamespace = XMLRootElementUtil.getXmlRootElementQName(clazz).getNamespaceURI();
                } catch (Exception e) {
                    // All else fails use the faultBean to calculate a namespace
                    targetNamespace = makeNamespace(getFaultBean());
                }
            }
        }
        return targetNamespace;
    }

    public String getExceptionClassName() {
    	if (!isDBC()) {
    		// no need for defaults here.  The exceptionClass stored in this
    		// FaultDescription object is one that has been declared to be
    		// thrown from the service method
    		return exceptionClass.getCanonicalName();
    	} else {
    		return composite.getClassName();
    	}
    }

    public OperationDescription getOperationDescription() {
        return parent;
    }
    
    /**
     * utility method to get the last token in a "."-delimited package+classname string
     * @return
     */
    private static String getSimpleName(String in) {
        if (in == null || in.length() == 0) {
            return in;
        }
        String out = null;
        StringTokenizer tokenizer = new StringTokenizer(in, ".");
        if (tokenizer.countTokens() == 0)
            out = in;
        else {
            while (tokenizer.hasMoreTokens()) {
                out = tokenizer.nextToken();
            }
        }
        return out;
    }
    
    private static String makeNamespace(String packageAndClassname) {
        if (packageAndClassname == null || packageAndClassname.length() == 0) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(packageAndClassname, ".");
        String[] tokens;
        if (tokenizer.countTokens() == 0) {
            tokens = new String[0];
        } else {
            // -1 to skip the classname
            tokens = new String[tokenizer.countTokens()-1];
            // -2 to skip the classname
            for (int i=tokenizer.countTokens()-2; i >= 0; i--) {
                tokens[i] = tokenizer.nextToken();
            }
        }
        StringBuffer namespace = new StringBuffer("http://");
        String dot = "";
        for (int i=0; i<tokens.length; i++) {
            if (i==1)
                dot = ".";
            namespace.append(dot+tokens[i]);
        }
        namespace.append('/');
        return namespace.toString();
    }
    
    private boolean isDBC() {
    	if (this.composite != null)
    		return true;
    	else 
    		return false;
    }
    
}
