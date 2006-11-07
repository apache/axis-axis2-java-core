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
import java.util.StringTokenizer;

import javax.xml.ws.WebFault;

import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.FaultDescriptionJava;
import org.apache.axis2.jaxws.description.FaultDescriptionWSDL;
import org.apache.axis2.jaxws.description.OperationDescription;

/**
 * @see ../FaultDescription
 *
 */


class FaultDescriptionImpl implements FaultDescription, FaultDescriptionJava, FaultDescriptionWSDL {
    
    private Class exceptionClass;
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
    public FaultDescriptionImpl(Class exceptionClass, WebFault annotation, OperationDescription parent) {
        this.exceptionClass = exceptionClass;
        this.annotation = annotation;
        this.parent = parent;
    }

    public WebFault getAnnoWebFault() {
        return annotation;
    }

    public String getFaultBean() {
        if (faultBean.length() > 0) {
            return faultBean;
        }
        else if (annotation.faultBean().length() > 0) {
            faultBean = annotation.faultBean();
        }
        else {
            /*
             * we need to figure out a default.  The JAXWS spec
             * has no defaults defined, except what can be interpreted
             * from 2.5, 2.8, 2.8.1, and 7.2
             *
             * specifically, section 2.5 says "using the mapping
             * described in section 2.4."
             * 
             * I have a better way:
             * The exception class defined by the JAXWS spec has two constructors where the second parameter
             * of each is the faultBean.  Let's see if we can figure what the faultBean is from that:
             */

            try {
                Constructor[] cons = exceptionClass.getConstructors();
                Class[] parms = cons[0].getParameterTypes();
                faultBean = parms[1].getCanonicalName();
            } catch (Exception e) {
                /* 
                 * if faultBean is still not set, then something is wrong with the exception
                 * class that the code generators generated, or someone instantiated a FaultDescription
                 * object for a generic exception.
                 *
                 * TODO log it?  I don't think we need to worry about throwing an exception here, as we're just
                 * doing a best-effort to determine the faultBean name.  If the try{} fails, something is really
                 * messed up in the generated code anyway, and I suspect nothing would work right.
                 */
            }
                
            // If all else fails, this might get us what we want:
            if ((faultBean == null) || (faultBean.length() == 0))
                faultBean = getOperationDescription().getRequestWrapperClassName().toString() + FAULT;

        }
        return faultBean;
    }

    public String getName() {
        if (name.length() > 0) {
            return name;
        }
        else if (annotation.name().length() > 0) {
            name = annotation.name();
        }
        else {
            // need to figure out a default.  Let's use the logic in getFaultBean()
            name = getSimpleName(getFaultBean());
        }
        return name;
    }

    public String getTargetNamespace() {
        if (targetNamespace.length() > 0) {
            return targetNamespace;
        }
        else if (annotation.targetNamespace().length() > 0) {
            targetNamespace = annotation.targetNamespace();
        }
        else {
            // need to figure out a default.  Let's use the logic in getFaultBean() and make a namespace out of the package
            
            targetNamespace = makeNamespace(getFaultBean());
        }
        return targetNamespace;
    }

    public String getExceptionClassName() {
        // no need for defaults here.  The exceptionClass stored in this
        // FaultDescription object is one that has been declared to be
        // thrown from the service method
        return exceptionClass.getCanonicalName();
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
    
}
