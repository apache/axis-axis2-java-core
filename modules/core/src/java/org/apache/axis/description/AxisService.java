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

import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Provider;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.impl.WSDLServiceImpl;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Class AxisService
 */
public class AxisService extends WSDLServiceImpl
        implements WSDLService, ParameterInclude, FlowInclude, PhasesInclude,
        DescriptionConstants {
    /**
     * TODO this should be in the WSDLInterface, yet we want it to have in the the
     * Services, so we put this here for M1 until we foud better way to do that
     */
    protected final HashMap operationsMap = new HashMap();

    /**
     * Constructor AxisService
     */
    public AxisService() {
        this.setComponentProperty(MODULEREF_KEY, new ArrayList());
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
        this.setComponentProperty(PHASES_KEY, new PhasesIncludeImpl());
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
        Collection collectionModule =
                (Collection) this.getComponentProperty(MODULEREF_KEY);
        collectionModule.add(moduleref);
    }

    /*
     * (non-Javadoc)
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

        // todo The key has been changed from the qname to the local name because
        // todo when comparing the namespace will not be available
        return (AxisOperation) this.operationsMap.get(
                operationName.getLocalPart());
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis.description.AxisService#addOperation(org.apache.axis.description.AxisOperation)
     */

    /**
     * Method addOperation
     *
     * @param operation
     */
    public void addOperation(AxisOperation operation) {

        // todo The key has been changed from the qname to the local name because
        // todo when comparing the namespace will not be available
        if (operation != null) {
            this.operationsMap.put(operation.getName().getLocalPart(),
                    operation);
        }
    }

    /*
     * (non-Javadoc)
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
     * @see org.apache.axis.description.AxisService#setProvider(org.apache.axis.engine.Provider)
     */

    /**
     * Method setProvider
     *
     * @param provider
     */
    public void setProvider(Provider provider) {
        if (provider != null) {
            this.setComponentProperty(PROVIDER_KEY, provider);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis.description.AxisService#getProvider()
     */

    /**
     * Method getProvider
     *
     * @return
     */
    public Provider getProvider() {
        return (Provider) this.getComponentProperty(PROVIDER_KEY);
    }

    /*
     * (non-Javadoc)
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
     * @see org.apache.axis.description.PhasesInclude#getPhases(java.util.ArrayList, int)
     */

    /**
     * Method setPhases
     *
     * @param phases
     * @param flow
     * @throws AxisFault
     */
    public void setPhases(ArrayList phases, int flow) throws AxisFault {
        if (phases == null) {
            return;
        }
        PhasesIncludeImpl phaseInclude =
                (PhasesIncludeImpl) this.getComponentProperty(PHASES_KEY);
        phaseInclude.setPhases(phases, flow);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis.description.PhasesInclude#getPhases(int)
     */

    /**
     * Method getPhases
     *
     * @param flow
     * @return
     * @throws AxisFault
     */
    public ArrayList getPhases(int flow) throws AxisFault {
        PhasesIncludeImpl phaseInclude =
                (PhasesIncludeImpl) this.getComponentProperty(PHASES_KEY);
        return (ArrayList) phaseInclude.getPhases(flow);
    }

    /*
     * (non-Javadoc)
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
     * @see org.apache.axis.description.FlowInclude#getFaultFlow()
     */

    /**
     * Method getFaultFlow
     *
     * @return
     */
    public Flow getFaultFlow() {
        return (Flow) this.getComponentProperty(FAULTFLOW_KEY);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis.description.FlowInclude#setFaultFlow(org.apache.axis.description.Flow)
     */

    /**
     * Method setFaultFlow
     *
     * @param faultFlow
     */
    public void setFaultFlow(Flow faultFlow) {
        if (faultFlow != null) {
            this.setComponentProperty(FAULTFLOW_KEY, faultFlow);
        }
    }

    /**
     * Method setServiceClass
     *
     * @param serviceclass
     */
    public void setServiceClass(Class serviceclass) {
        if (serviceclass != null) {
            this.setComponentProperty(DescriptionConstants.SERVICE_CLASS,
                    serviceclass);
        }
    }

    /**
     * Method getServiceClass
     *
     * @return
     */
    public Class getServiceClass() {
        return (Class) this.getComponentProperty(
                DescriptionConstants.SERVICE_CLASS);
    }

    /**
     * Method getOperations
     *
     * @return
     */
    public HashMap getOperations() {
        return operationsMap;
    }
}
