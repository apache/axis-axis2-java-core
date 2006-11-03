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


package org.apache.axis2.jaxws.description;

import javax.xml.ws.WebFault;

/**
 * A FaultDescription corresponds to a fault that can be thrown from an operation.  NOTE this it not 
 * implemented yet!
 * 
 * FaultDescriptons contain information that is only relevent for and SEI-based service, i.e. one that is invoked via specific
 * methods.  This class does not exist for Provider-based services (i.e. those that specify WebServiceProvider)
 * 
 * <pre>
 * <b>OperationDescription details</b>
 * 
 *     CORRESPONDS TO:      An exception thrown by an operation on an SEI (on both Client and Server)      
 *         
 *     AXIS2 DELEGATE:      None
 *     
 *     CHILDREN:            None
 *     
 *     ANNOTATIONS:
 *         WebFault [224]
 *     
 *     WSDL ELEMENTS:
 *         fault message
 *         
 *  </pre>       
 */
// TODO: This class is not implemented yet or used from OperationDescription
class FaultDescriptionImpl implements FaultDescription, FaultDescriptionJava, FaultDescriptionWSDL {
    
    private String exceptionClassName;
    private String beanName;
    private WebFault annotation;
    private OperationDescription parent;

    public FaultDescriptionImpl(String exceptionClassName, String beanName, WebFault annotation, OperationDescription parent) {
        this.exceptionClassName = exceptionClassName;
        this.beanName = beanName;
        this.annotation = annotation;
        this.parent = parent;
    }

    public WebFault getAnnoWebFault() {
        return annotation;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

    public OperationDescription getOperationDescription() {
        return parent;
    }
    
}
