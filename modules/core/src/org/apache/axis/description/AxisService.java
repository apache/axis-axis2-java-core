/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.apache.axis.description;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.impl.WSDLServiceImpl;

/**
 * Class AxisService
 */
public class AxisService
    extends WSDLServiceImpl
    implements WSDLService, ParameterInclude, FlowInclude, DescriptionConstants {
    /**
     * TODO this should be in the WSDLInterface, yet we want it to have in the
     * the Services, so we put this here for M1 until we foud better way to do
     * that
     */
    protected final HashMap operationsMap = new HashMap();

    /**
     * Constructor AxisService
     */
    public AxisService() {
        this.setComponentProperty(MODULEREF_KEY, new ArrayList());
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
    }

    /**
     * Constructor AxisService
     * 
     * @param qName
     */
    public AxisService(QName qName) {
        this();
        this.setName(qName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.AxisService#addModule(javax.xml.namespace.QName)
     */

    /**
     * Method addModule
     * 
     * @param moduleref
     */
    public void addModule(QName moduleref) {
        if (moduleref == null) {
            return;
        }
        Collection collectionModule = (Collection) this.getComponentProperty(MODULEREF_KEY);
        collectionModule.add(moduleref);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.AxisService#getModules()
     */

    /**
     * Method getModules
     * 
     * @return
     */
    public Collection getModules() {
        return (Collection) this.getComponentProperty(MODULEREF_KEY);
    }

    /**
     * Method getOperation
     * 
     * @param operationName
     * @return
     */
    public AxisOperation getOperation(QName operationName) {

        // todo The key has been changed from the qname to the local name
        // because
        // todo when comparing the namespace will not be available
        return (AxisOperation) this.operationsMap.get(operationName.getLocalPart());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.AxisService#addOperation(org.apache.axis.description.AxisOperation)
     */

    /**
     * Method addOperation
     * 
     * @param operation
     */
    public void addOperation(AxisOperation operation) {

        // todo The key has been changed from the qname to the local name
        // because
        // todo when comparing the namespace will not be available
        if (operation != null) {
            this.operationsMap.put(operation.getName().getLocalPart(), operation);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.AxisService#setClassLoader(java.lang.ClassLoader)
     */

    /**
     * Method setClassLoader
     * 
     * @param classLoader
     */
    public void setClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            this.setComponentProperty(CLASSLOADER_KEY, classLoader);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.AxisService#getClassLoader()
     */

    /**
     * Method getClassLoader
     * 
     * @return
     */
    public ClassLoader getClassLoader() {
        return (ClassLoader) this.getComponentProperty(CLASSLOADER_KEY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.AxisService#setContextPath(java.lang.String)
     */

    /**
     * Method setContextPath
     * 
     * @param contextPath
     */
    public void setContextPath(String contextPath) {
        if (contextPath != null) {
            this.setComponentProperty(CONTEXTPATH_KEY, contextPath);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.AxisService#getContextPath()
     */

    /**
     * Method getContextPath
     * 
     * @return
     */
    public String getContextPath() {
        return (String) this.getComponentProperty(CONTEXTPATH_KEY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.AxisService#setStyle(javax.swing.text.Style)
     */

    /**
     * Method setStyle
     * 
     * @param style
     */
    public void setStyle(String style) {
        if (style != null) {
            this.setComponentProperty(STYLE_KEY, style);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.AxisService#getStyle()
     */

    /**
     * Method getStyle
     * 
     * @return
     */
    public String getStyle() {
        return (String) this.getComponentProperty(STYLE_KEY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.PhasesInclude#getPhases(java.util.ArrayList,
     *      int)
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.ParameterInclude#addParameter(org.apache.axis.description.Parameter)
     */

    /**
     * Method addParameter
     * 
     * @param param
     */
    public void addParameter(Parameter param) {
        if (param == null) {
            return;
        }
        ParameterIncludeImpl paramInclude =
            (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
        paramInclude.addParameter(param);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.ParameterInclude#getParameter(java.lang.String)
     */

    /**
     * Method getParameter
     * 
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        ParameterIncludeImpl paramInclude =
            (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
        return (Parameter) paramInclude.getParameter(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.FlowInclude#getInFlow()
     */

    /**
     * Method getInFlow
     * 
     * @return
     */
    public Flow getInFlow() {
        return (Flow) this.getComponentProperty(INFLOW_KEY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.FlowInclude#setInFlow(org.apache.axis.description.Flow)
     */

    /**
     * Method setInFlow
     * 
     * @param inFlow
     */
    public void setInFlow(Flow inFlow) {
        if (inFlow != null) {
            this.setComponentProperty(INFLOW_KEY, inFlow);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.FlowInclude#getOutFlow()
     */

    /**
     * Method getOutFlow
     * 
     * @return
     */
    public Flow getOutFlow() {
        return (Flow) this.getComponentProperty(OUTFLOW_KEY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.FlowInclude#setOutFlow(org.apache.axis.description.Flow)
     */

    /**
     * Method setOutFlow
     * 
     * @param outFlow
     */
    public void setOutFlow(Flow outFlow) {
        if (outFlow != null) {
            this.setComponentProperty(OUTFLOW_KEY, outFlow);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.FlowInclude#getFaultInFlow()
     */

    /**
     * Method getFaultInFlow
     * 
     * @return
     */
    public Flow getFaultInFlow() {
        return (Flow) this.getComponentProperty(IN_FAULTFLOW_KEY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.axis.description.FlowInclude#setFaultInFlow(org.apache.axis.description.Flow)
     */

    /**
     * Method setFaultInFlow
     * 
     * @param faultFlow
     */
    public void setFaultInFlow(Flow faultFlow) {
        if (faultFlow != null) {
            this.setComponentProperty(IN_FAULTFLOW_KEY, faultFlow);
        }
    }

    public Flow getFaultOutFlow() {
        return (Flow) this.getComponentProperty(OUT_FAULTFLOW_KEY);
    }

    public void setFaultOutFlow(Flow faultFlow) {
        if (faultFlow != null) {
            this.setComponentProperty(OUT_FAULTFLOW_KEY, faultFlow);
        }
    }

    /**
     * Method getOperations
     * 
     * @return
     */
    public HashMap getOperations() {
        return operationsMap;
    }

    /**
     * This finds the ServiceContext provided that the incomming message that
     * has have some serviceInstanceID. Currently this will not be added to the
     * EngineContext's ServiceContextMap.
     * 
     * @param msgContext
     * @return
     */
    public ServiceContext findServiceContext(MessageContext msgContext) {
        ServiceContext serviceContext = null;
        if (null == msgContext.getServiceInstanceID()) {
            serviceContext = new ServiceContext(this, msgContext.getEngineContext());
            //TODO Once the ServiceContext is bound to an incomming serviceContext ID(like a cookie,reference Property) FIX this
            //			msgContext.getEngineContext().registerServiceContext(serviceContext.getServiceInstanceID(),
            // serviceContext);
        } else {
            serviceContext =
                (ServiceContext) msgContext.getEngineContext().getServiceContext(
                    msgContext.getServiceInstanceID());
        }

        return serviceContext;

    }

}