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

package org.apache.axis2.description;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.*;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPOperation;
import org.apache.wsdl.impl.WSDLInterfaceImpl;
import org.apache.wsdl.impl.WSDLServiceImpl;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Class AxisService
 */
public class AxisService
        //    extends WSDLServiceImpl
        implements WSDLService,
        ParameterInclude,
        FlowInclude,
        DescriptionConstants {

    private Definition definition = null;

    private Log log = LogFactory.getLog(getClass());

    private HashMap moduleConfigmap;

    private AxisServiceGroup parent;
    //to store the wsdl definition , which is build at the deployment time

    //to keep the time that last update time of the service
    private long lastupdate;
    private String axisServiceName;
    private String fileName = "";

    private WSDLServiceImpl serviceimpl = null;

    private HashMap wasaction_opeartionmap = null;

    //to store module ref at deploy time parsing
    private ArrayList moduleRefs = new ArrayList();

    //to store engaged mdodules
    private ArrayList engagedModules = new ArrayList();


    /**
     * Constructor AxisService
     */

    public AxisService(WSDLServiceImpl serviceimpl) {
        this.serviceimpl = serviceimpl;
        this.wasaction_opeartionmap = new HashMap();
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
        this.setServiceInterface(new WSDLInterfaceImpl());
        moduleConfigmap = new HashMap();

    }

    public AxisService() {
        this.serviceimpl = new WSDLServiceImpl();
        this.wasaction_opeartionmap = new HashMap();
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
        this.setServiceInterface(new WSDLInterfaceImpl());
        moduleConfigmap = new HashMap();
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
    * @see org.apache.axis2.description.AxisService#addToengagedModules(javax.xml.namespace.QName)
    */

    /**
     * To ebgage a module it is reuired to use this method
     *
     * @param moduleref
     */
    public void engageModule(ModuleDescription moduleref,
                             AxisConfiguration axisConfig) throws AxisFault {
        if (moduleref == null) {
            return;
        }
        Iterator itr_engageModules = engagedModules.iterator();
        while (itr_engageModules.hasNext()) {
            ModuleDescription module = (ModuleDescription) itr_engageModules.next();
            if (module.getName().equals(moduleref.getName())) {
                log.info(moduleref.getName().getLocalPart() +
                        " module has alredy been engaged on the service. " +
                        " Operation terminated !!!");
//                return;
            }
        }
//adding module operations
        addModuleOperations(moduleref, axisConfig);

        Iterator operations = getOperations().values().iterator();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            axisOperation.engageModule(moduleref, axisConfig);
        }
        engagedModules.add(moduleref);
    }

    /**
     * To add a opeartion to a service if a module requird to do so
     *
     * @param module
     */

    public void addModuleOperations(ModuleDescription module,
                                    AxisConfiguration axisConfig) throws AxisFault {
        HashMap map = module.getOperations();
        Collection col = map.values();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            AxisOperation axisOperation = copyOperation((AxisOperation) iterator.next());
            ArrayList wsamappings = axisOperation.getWsamappingList();
            for (int j = 0; j < wsamappings.size(); j++) {
                Parameter paramter = (Parameter) wsamappings.get(j);
                this.addMapping((String) paramter.getValue(), axisOperation);
            }
            //this opration is a control opeartion.
            axisOperation.setControlOperation(true);
            this.addOperation(axisOperation);
        }
    }

    /**
     * To get a copy from module operation
     *
     * @param axisOperation
     * @return
     * @throws AxisFault
     */
    private AxisOperation copyOperation(AxisOperation axisOperation) throws AxisFault {
        AxisOperation operation = AxisOperationFactory.getOperetionDescription(
                axisOperation.getMessageExchangePattern());
        operation.setMessageReceiver(axisOperation.getMessageReceiver());
        operation.setName(axisOperation.getName());
        operation.setStyle(axisOperation.getStyle());
        Iterator parameters = axisOperation.getParameters().iterator();
        while (parameters.hasNext()) {
            Parameter parameter = (Parameter) parameters.next();
            operation.addParameter(parameter);
        }
        operation.setWsamappingList(axisOperation.getWsamappingList());
        operation.setRemainingPhasesInFlow(axisOperation.getRemainingPhasesInFlow());
        operation.setPhasesInFaultFlow(axisOperation.getPhasesInFaultFlow());
        operation.setPhasesOutFaultFlow(axisOperation.getPhasesOutFaultFlow());
        operation.setPhasesOutFlow(axisOperation.getPhasesOutFlow());
        return operation;
    }

    /**
     * Method getEngadgedModules
     *
     * @return Collection
     */
    public Collection getEngagedModules() {
        return engagedModules;
    }

    /**
     * Method getOperation
     *
     * @param operationName
     * @return AxisOperation
     */
    public AxisOperation getOperation(QName operationName) {
        String opStr = operationName.getLocalPart();

        HashMap allOperations = this.getServiceInterface().getAllOperations();
        AxisOperation opeartion = (AxisOperation) allOperations.get(opStr);
        if (opeartion == null) {
            opeartion = (AxisOperation) wasaction_opeartionmap.get(
                    operationName.getLocalPart());
        }
        return opeartion;
    }

    /**
     * To get the WSDL opeartion element in servic einterface
     *
     * @param operationName <code>QName</cde>
     * @return WSDLOperation <code>WSDLOperation</code>
     */
    public WSDLOperation getWSDLOPOperation(QName operationName) {
        String opStr = operationName.getLocalPart();
        return this.getServiceInterface().getOperation(opStr);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.AxisService#addOperation(org.apache.axis2.description.AxisOperation)
    */

    /**
     * Method addOperation
     *
     * @param axisOperation
     */
    public void addOperation(AxisOperation axisOperation) {
        axisOperation.setParent(this);
        Iterator modules = getEngagedModules().iterator();
        while (modules.hasNext()) {
            ModuleDescription module = (ModuleDescription) modules.next();
            AxisServiceGroup parent = getParent();
            AxisConfiguration axisConfig = null;
            if (parent != null) {
                axisConfig = parent.getParent();
            }
            try {
                axisOperation.engageModule(module, axisConfig);
            } catch (AxisFault axisFault) {
                log.info("Trying to engage a module which is already engege:"
                        + module.getName().getLocalPart());
            }
        }
        this.getServiceInterface().setOperation(axisOperation);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.AxisService#setClassLoader(java.lang.ClassLoader)
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
    * @see org.apache.axis2.description.AxisService#getClassLoader()
    */

    /**
     * Method getClassLoader
     *
     * @return ClassLoader
     */
    public ClassLoader getClassLoader() {
        return (ClassLoader) this.getComponentProperty(CLASSLOADER_KEY);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.AxisService#setContextPath(java.lang.String)
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
    * @see org.apache.axis2.description.AxisService#getContextPath()
    */

    /**
     * Method getContextPath
     *
     * @return String
     */
    public String getContextPath() {
        return (String) this.getComponentProperty(CONTEXTPATH_KEY);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.AxisService#setStyle(javax.swing.text.Style)
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
    * @see org.apache.axis2.description.AxisService#getStyle()
    */

    /**
     * Method getStyle
     *
     * @return String
     */
    public String getStyle() {
        return (String) this.getComponentProperty(STYLE_KEY);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.PhasesInclude#getPhases(java.util.ArrayList,
    *      int)
    */

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.ParameterInclude#addParameter(org.apache.axis2.description.Parameter)
    */

    /**
     * Method addParameter
     *
     * @param param
     */
    public void addParameter(Parameter param) throws AxisFault {
        if (param == null) {
            return;
        }

        if (isParameterLocked(param.getName())) {
            throw new AxisFault("Parmter is locked can not overide: " + param.getName());
        } else {
            ParameterIncludeImpl paramInclude =
                    (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
            paramInclude.addParameter(param);
        }
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.ParameterInclude#getParameter(java.lang.String)
    */

    /**
     * Method getParameter
     *
     * @param name
     * @return Parameter
     */
    public Parameter getParameter(String name) {
        ParameterIncludeImpl paramInclude =
                (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
        return paramInclude.getParameter(name);
    }

    public ArrayList getParameters() {
        ParameterIncludeImpl paramInclude =
                (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
        return paramInclude.getParameters();
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.FlowInclude#getInFlow()
    */

    /**
     * Method getInFlow
     *
     * @return Flow
     */
    public Flow getInFlow() {
        return (Flow) this.getComponentProperty(INFLOW_KEY);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.FlowInclude#setInFlow(org.apache.axis2.description.Flow)
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
    * @see org.apache.axis2.description.FlowInclude#getOutFlow()
    */

    /**
     * Method getOutFlow
     *
     * @return Flow
     */
    public Flow getOutFlow() {
        return (Flow) this.getComponentProperty(OUTFLOW_KEY);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.FlowInclude#setOutFlow(org.apache.axis2.description.Flow)
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
    * @see org.apache.axis2.description.FlowInclude#getFaultInFlow()
    */

    /**
     * Method getFaultInFlow
     *
     * @return Flow
     */
    public Flow getFaultInFlow() {
        return (Flow) this.getComponentProperty(IN_FAULTFLOW_KEY);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.FlowInclude#setFaultInFlow(org.apache.axis2.description.Flow)
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
     * @return HashMap
     */
    public HashMap getOperations() {
        return this.getServiceInterface().getOperations();
    }

    /**
     * To get only the publish operations
     */

    public ArrayList getPublishedOperations() {
        Iterator op_itr = getOperations().values().iterator();
        ArrayList operationList = new ArrayList();
        while (op_itr.hasNext()) {
            AxisOperation operation = (AxisOperation) op_itr.next();
            if (!operation.isControlOperation()) {
                operationList.add(operation);
            }
        }
        return operationList;
    }

    /**
     * To get the control operation which are added by module like RM
     */
    public ArrayList getControlOperations() {
        Iterator op_itr = getOperations().values().iterator();
        ArrayList operationList = new ArrayList();
        while (op_itr.hasNext()) {
            AxisOperation operation = (AxisOperation) op_itr.next();
            if (operation.isControlOperation()) {
                operationList.add(operation);
            }
        }
        return operationList;
    }

    public AxisOperation getOperation(String ncName) {
        return (AxisOperation) this.getServiceInterface().getOperations()
                .get(ncName);
    }

    /**
     * This method will return the operation given particular SOAP Action.
     * This method should only be called if there is only one Endpoint is defined
     * for this Service. If more than one Endpoint exists one of them will be picked.
     * If more than one Operation is found with the given
     * SOAP Action; null will be ruturned. If no particular Operation is found with
     * the given SOAP Action; null will be returned.
     *
     * @param soapAction SOAP Action defined for the particular Operation
     * @return A AxisOperation if a unque Operation can be found with the given SOAP Action
     *         otherwise will return null.
     */
    public AxisOperation getOperationBySOAPAction(String soapAction) {
        if (soapAction == null || soapAction.equals("")) {
            return null;
        }
        Iterator iterator = this.getEndpoints().keySet().iterator();
        if (iterator.hasNext()) {
            WSDLEndpoint endpoint = (WSDLEndpoint) this.getEndpoints().get(
                    iterator.next());
            return this.getOperationBySOAPAction(soapAction,
                    endpoint.getName());
        }

        return null;


    }


    /**
     * This method will return the operation given the particular endpoing and the
     * particular SOAP Action. If more than one Operation is found with the given
     * SOAP Action; null will be ruturned. If no particular Operation is found with
     * the given SOAP Action; null will be returned
     *
     * @param endpoint   Particular Enpoint in which the bining is defined with the particular SOAP
     *                   Action.
     * @param soapAction SOAP Action defined for the particular Operation
     * @return A AxisOperation if a unque Operation can be found with the given SOAP Action
     *         otherwise will return null.
     */
    public AxisOperation getOperationBySOAPAction(String soapAction,
                                                  QName endpoint) {
        HashMap bindingOperations = this.getEndpoint(endpoint).getBinding()
                .getBindingOperations();
        Iterator operationKeySetIterator = bindingOperations.keySet().iterator();
        AxisOperation axisOperation = null;
        int count = 0;
        while (operationKeySetIterator.hasNext()) {
            WSDLBindingOperation bindingOperation = (WSDLBindingOperation) bindingOperations.get(
                    operationKeySetIterator.next());
            Iterator extIterator = bindingOperation.getExtensibilityElements()
                    .iterator();
            while (extIterator.hasNext()) {
                WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();
                if (ExtensionConstants.SOAP_11_OPERATION.equals(element.getType()) ||
                        ExtensionConstants.SOAP_12_OPERATION.equals(element.getType())) {
                    if (((SOAPOperation) element).getSoapAction().equals(
                            soapAction)) {
                        WSDLOperation op = bindingOperation.getOperation();
                        if (op instanceof AxisOperation) {
                            axisOperation = (AxisOperation) op;
                            count++;
                        }
                    }
                }
            }
        }
        if (1 == count) {
            return axisOperation;
        }
        return null;
    }


    /**
     * This finds the ServiceContext provided that the incomming message that
     * has have some serviceInstanceID. Currently this will not be added to the
     * EngineContext's ServiceContextMap.
     *
     * @param msgContext
     * @return ServiceContext
     */
    public ServiceContext u(MessageContext msgContext) {
        // TODO : Fix me. Can't look up a service context in the system context
        ServiceContext serviceContext;
        if (null == msgContext.getServiceGroupContextId()) {
            serviceContext =
                    new ServiceContext(this, msgContext.getServiceGroupContext());
            //TODO Once the ServiceContext is bound to an incomming serviceContext ID(like a cookie,reference Property) FIX this
            //			msgContext.getConfigurationContext().registerServiceContext(serviceContext.getServiceContextID(),
            // serviceContext);
        } else {
            serviceContext =
                    msgContext.getConfigurationContext()
                            .getServiceContext(msgContext.getServiceContextID());
        }

        return serviceContext;

    }

    /**
     * To get the description about the service
     * ty67tyuio
     *
     * @return String
     */
    public String getAxisServiceName() {
        return axisServiceName;
    }

    /**
     * Set the description about the service
     *
     * @param axisServiceName
     */
    public void setAxisServiceName(String axisServiceName) {
        this.axisServiceName = axisServiceName;
    }

    public Definition getWSDLDefinition() {
        return definition;
    }

    public void setWSDLDefinition(Definition difDefinition) {
        this.definition = difDefinition;
    }

    public void printWSDL(Writer out, String PortURL) throws AxisFault {
        try {
            Definition wsdlDefinition = this.getWSDLDefinition();
            if (wsdlDefinition != null) {
                Collection services = wsdlDefinition.getServices().values();

                for (Iterator iterator = services.iterator(); iterator.hasNext();) {
                    Service service = (Service) iterator.next();
                    Collection ports = service.getPorts().values();
                    for (Iterator iterator1 = ports.iterator(); iterator1.hasNext();) {
                        Port port = (Port) iterator1.next();
                        service.setQName(this.getName());
                        SOAPAddress soapAddress = new SOAPAddressImpl();
                        soapAddress.setElementType(SOAPConstants.Q_ELEM_SOAP_ADDRESS);
                        soapAddress.setLocationURI(PortURL);
                        port.getExtensibilityElements().clear();
                        port.addExtensibilityElement(soapAddress);
                    }
                }

                WSDLFactory.newInstance().newWSDLWriter().writeWSDL(
                        wsdlDefinition, out);
                out.flush();
            } else {
                WSDLFactory.newInstance().newWSDLWriter().writeWSDL(
                        wsdlDefinition, out);
                out.write("<wsdl>This service does not have a WSDL</wsdl>");
                out.flush();
            }


        } catch (WSDLException e) {
            throw new AxisFault(e);
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    /**
     * This method will set the current time as last update time of the service
     */
    public void setLastupdate() {
        lastupdate = new Date().getTime();
    }

    public long getLastupdate() {
        return lastupdate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public HashMap getEndpoints() {
        return serviceimpl.getEndpoints();
    }

    public void setEndpoints(HashMap endpoints) {
        serviceimpl.setEndpoints(endpoints);
    }

    public void setEndpoint(WSDLEndpoint endpoint) {
        serviceimpl.setEndpoint(endpoint);
    }

    public WSDLEndpoint getEndpoint(QName qName) {
        return serviceimpl.getEndpoint(qName);
    }

    public QName getName() {
        return serviceimpl.getName();
    }

    public void setName(QName name) {
        serviceimpl.setName(name);
    }

    public String getNamespace() {
        return serviceimpl.getNamespace();
    }

    public WSDLInterface getServiceInterface() {
        return serviceimpl.getServiceInterface();
    }

    public void setServiceInterface(WSDLInterface serviceInterface) {
        serviceimpl.setServiceInterface(serviceInterface);
    }

    public HashMap getComponentProperties() {
        return serviceimpl.getComponentProperties();
    }

    public void setComponentProperties(HashMap properties) {
        serviceimpl.setComponentProperties(properties);
    }

    public void setComponentProperty(Object key, Object obj) {
        serviceimpl.setComponentProperty(key, obj);
    }

    public Object getComponentProperty(Object key) {
        return serviceimpl.getComponentProperty(key);
    }

    public void addExtensibilityElement(WSDLExtensibilityElement element) {
        serviceimpl.addExtensibilityElement(element);
    }

    public List getExtensibilityElements() {
        return serviceimpl.getExtensibilityElements();
    }

    public List getExtensibilityAttributes() {
        return serviceimpl.getExtensibilityAttributes();
    }

    public void addExtensibleAttributes(WSDLExtensibilityAttribute attribute) {
        serviceimpl.addExtensibleAttributes(attribute);
    }

    public Map getMetadataBag() {
        return serviceimpl.getMetadataBag();
    }

    public void setMetadataBag(Map map) {
        this.serviceimpl.setMetadataBag(map);
    }

    /**
     * To add the was action paramater into has map so that was action based dispatch can support
     */
    public void addMapping(String mappingKey, AxisOperation axisOperation) {
        wasaction_opeartionmap.put(mappingKey, axisOperation);
    }

    /**
     * To get the parent (which is AxisConfiguration in this case)
     *
     * @return <code>AxisConfiguration</code>
     */
    public AxisServiceGroup getParent() {
        return parent;
    }

    public void setParent(AxisServiceGroup parent) {
        this.parent = parent;
    }

    //to check whether a given paramter is locked
    public boolean isParameterLocked(String paramterName) {
        // checking the locked value of parent
        boolean loscked = false;

        if (getParent() != null) {
            loscked = getParent().getAxisDescription().isParameterLocked(paramterName);
        }
        if (loscked) {
            return true;
        } else {
            Parameter parameter = getParameter(paramterName);
            return parameter != null && parameter.isLocked();
        }
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        ParameterIncludeImpl paramInclude =
                (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
        paramInclude.deserializeParameters(parameterElement);
    }

    /**
     * Adding module configuration , if there is moduleConfig tag in service
     *
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        moduleConfigmap.put(moduleConfiguration.getModuleName(), moduleConfiguration);
    }

    public ModuleConfiguration getModuleConfig(QName moduleName) {
        return (ModuleConfiguration) moduleConfigmap.get(moduleName);
    }


    public void addModuleref(QName moduleref) {
        moduleRefs.add(moduleref);
    }

    public ArrayList getModules() {
        return moduleRefs;
    }


}