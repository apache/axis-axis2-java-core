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

import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.phaseresolver.PhaseResolver;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.impl.WSDLServiceImpl;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class ServiceDescription
 */
public class ServiceDescription
        extends WSDLServiceImpl
        implements WSDLService, ParameterInclude, FlowInclude, DescriptionConstants {
    /**
     * TODO this should be in the WSDLInterface, yet we want it to have in the
     * the Services, so we put this here for M1 until we foud better way to do
     * that
     */
    protected final HashMap operationsMap = new HashMap();

    private String serviceDescription = "Not Specified";

    /**
     * Constructor ServiceDescription
     */
    public ServiceDescription() {
        this.setComponentProperty(MODULEREF_KEY, new ArrayList());
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
    }

    /**
     * Constructor ServiceDescription
     * 
     * @param qName
     */
    public ServiceDescription(QName qName) {
        this();
        this.setName(qName);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis.description.ServiceDescription#addModule(javax.xml.namespace.QName)
    */

    /**
     * To ebgage a module it is reuired to use this method
     * @param moduleref
     * @throws AxisFault
     */
    public void engageModule(ModuleDescription moduleref) throws AxisFault {
        if (moduleref == null) {
            return;
        }
        if (moduleref != null) {
            Collection collectionModule = (Collection) this.getComponentProperty(MODULEREF_KEY);
            for (Iterator iterator = collectionModule.iterator(); iterator.hasNext();) {
                ModuleDescription   modu = (ModuleDescription) iterator.next();
                if(modu.getName().equals(moduleref.getName())){
                    throw new AxisFault(moduleref.getName().getLocalPart()+ " module has alredy engaged to the seevice" +
                            "  operation terminated !!!");
                }

            }
        }
        new PhaseResolver().engageModuleToService(this,moduleref);
        Collection collectionModule = (Collection) this.getComponentProperty(MODULEREF_KEY);
        collectionModule.add(moduleref);
    }

    /**
     * To add a opeartion to a service if a module requird to do so
     * @param module
     */
    public void addModuleOperations(ModuleDescription module){
        HashMap map = module.getOperations();
        Collection col =  map.values();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            OperationDescription operation = (OperationDescription) iterator.next();
            this.addOperation(operation);
        }
    }

    public void addToEngagModuleList(ModuleDescription moduleName){
        Collection collectionModule = (Collection) this.getComponentProperty(MODULEREF_KEY);
        collectionModule.add(moduleName);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis.description.ServiceDescription#getEngadgedModules()
    */

    /**
     * Method getEngadgedModules
     * 
     * @return
     */
    public Collection getEngagedModules() {
        return (Collection) this.getComponentProperty(MODULEREF_KEY);
    }

    /**
     * Method getOperation
     * 
     * @param operationName
     * @return
     */
    public OperationDescription getOperation(QName operationName) {

        // todo The key has been changed from the qname to the local name
        // because
        // todo when comparing the namespace will not be available
        return (OperationDescription) this.operationsMap.get(operationName.getLocalPart());
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis.description.ServiceDescription#addOperation(org.apache.axis.description.OperationDescription)
    */

    /**
     * Method addOperation
     * 
     * @param operation
     */
    public void addOperation(OperationDescription operation) {

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
    * @see org.apache.axis.description.ServiceDescription#setClassLoader(java.lang.ClassLoader)
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
    * @see org.apache.axis.description.ServiceDescription#getClassLoader()
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
    * @see org.apache.axis.description.ServiceDescription#setContextPath(java.lang.String)
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
    * @see org.apache.axis.description.ServiceDescription#getContextPath()
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
    * @see org.apache.axis.description.ServiceDescription#setStyle(javax.swing.text.Style)
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
    * @see org.apache.axis.description.ServiceDescription#getStyle()
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
            serviceContext = new ServiceContext(this, msgContext.getSystemContext());
            //TODO Once the ServiceContext is bound to an incomming serviceContext ID(like a cookie,reference Property) FIX this
            //			msgContext.getSystemContext().registerServiceContext(serviceContext.getServiceInstanceID(),
            // serviceContext);
        } else {
            serviceContext =
                    (ServiceContext) msgContext.getSystemContext().getServiceContext(
                            msgContext.getServiceInstanceID());
        }

        return serviceContext;

    }

    /**
     * To get the description about the service
     * @return
     */
    public String getServiceDescription() {
        return serviceDescription;
    }

    /**
     * Set the description about the service
     * @param serviceDescription
     */
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

}