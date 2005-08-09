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
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.wsdl.*;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPOperation;
import org.apache.wsdl.impl.WSDLInterfaceImpl;
import org.apache.wsdl.impl.WSDLServiceImpl;

import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Class ServiceDescription
 */
public class ServiceDescription
        //    extends WSDLServiceImpl
        implements WSDLService ,
        ParameterInclude,
        FlowInclude,
        DescriptionConstants {

    private Definition difDefinition = null;
    //to store the wsdl definition , which is build at the deployment time

    //to keep the time that last update time of the service
    private long lastupdate ;
    /**
     * TODO this should be in the WSDLInterface, yet we want it to have in the
     * the Services, so we put this here for M1 until we foud better way to do
     * that
     */
//    protected final HashMap operationsMap = new HashMap();

    private String serviceDescription = "Not Specified";

    private String fileName = "";

    private WSDLServiceImpl serviceimpl = null;

    /**
     * Constructor ServiceDescription
     */

    public ServiceDescription(WSDLServiceImpl serviceimpl){
        this.serviceimpl = serviceimpl;
        this.setComponentProperty(MODULEREF_KEY, new ArrayList());
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
        this.setServiceInterface(new WSDLInterfaceImpl());
    }

    public ServiceDescription() {
        this.serviceimpl = new WSDLServiceImpl();
        this.setComponentProperty(MODULEREF_KEY, new ArrayList());
        this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
        this.setServiceInterface(new WSDLInterfaceImpl());
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
    * @see org.apache.axis2.description.ServiceDescription#addModule(javax.xml.namespace.QName)
    */

    /**
     * To ebgage a module it is reuired to use this method
     *
     * @param moduleref
     * @throws AxisFault
     */
    public void engageModule(ModuleDescription moduleref , AxisConfiguration axisConfig) throws AxisFault {
        if (moduleref == null) {
            return;
        }
        if (moduleref != null) {
            Collection collectionModule = (Collection) this.getComponentProperty(
                    MODULEREF_KEY);
            for (Iterator iterator = collectionModule.iterator();
                 iterator.hasNext();) {
                ModuleDescription modu = (ModuleDescription) iterator.next();
                if (modu.getName().equals(moduleref.getName())) {
                    throw new AxisFault(moduleref.getName().getLocalPart() +
                            " module has alredy engaged to the seevice" +
                            "  operation terminated !!!");
                }

            }
        }
        new PhaseResolver(axisConfig).engageModuleToService(this, moduleref);
        Collection collectionModule = (Collection) this.getComponentProperty(
                MODULEREF_KEY);
        collectionModule.add(moduleref);
    }

    /**
     * To add a opeartion to a service if a module requird to do so
     *
     * @param module
     */
    public void addModuleOperations(ModuleDescription module ,AxisConfiguration axisConfig) {
        HashMap map = module.getOperations();
        Collection col = map.values();
        PhaseResolver pr = new PhaseResolver(axisConfig, this);
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            OperationDescription operation = (OperationDescription) iterator.next();
            this.addOperation(operation);
        }
    }

    public void addToEngagModuleList(ModuleDescription moduleName) {
        Collection collectionModule = (Collection) this.getComponentProperty(
                MODULEREF_KEY);
        for (Iterator iterator = collectionModule.iterator();
             iterator.hasNext();) {
            ModuleDescription moduleDescription = (ModuleDescription) iterator.next();
            if (moduleName.getName().equals(moduleDescription.getName())) {
                return;
            }
        }
        collectionModule.add(moduleName);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.ServiceDescription#getEngadgedModules()
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
        String opStr = operationName.getLocalPart();

        HashMap allOperations = this.getServiceInterface().getAllOperations();
        return (OperationDescription) allOperations.get(opStr);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.ServiceDescription#addOperation(org.apache.axis2.description.OperationDescription)
    */

    /**
     * Method addOperation
     *
     * @param operation
     */
    public void addOperation(OperationDescription operation) {

        this.getServiceInterface().setOperation(operation);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.ServiceDescription#setClassLoader(java.lang.ClassLoader)
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
    * @see org.apache.axis2.description.ServiceDescription#getClassLoader()
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
    * @see org.apache.axis2.description.ServiceDescription#setContextPath(java.lang.String)
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
    * @see org.apache.axis2.description.ServiceDescription#getContextPath()
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
    * @see org.apache.axis2.description.ServiceDescription#setStyle(javax.swing.text.Style)
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
    * @see org.apache.axis2.description.ServiceDescription#getStyle()
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
    * @see org.apache.axis2.description.ParameterInclude#getParameter(java.lang.String)
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

    public ArrayList getParameters() {
        ParameterIncludeImpl paramInclude =
                (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
        return  paramInclude.getParameters();
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.axis2.description.FlowInclude#getInFlow()
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
     * @return
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
     * @return
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
     * @return
     */
    public HashMap getOperations() {
        return this.getServiceInterface().getOperations();
    }

    public OperationDescription getOperation(String ncName) {
        return (OperationDescription) this.getServiceInterface().getOperations()
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
     * @return A OperationDescription if a unque Operation can be found with the given SOAP Action
     *         otherwise will return null.
     */
    public OperationDescription getOperationBySOAPAction(String soapAction) {
        if(soapAction == null || soapAction.equals("")){
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
     * @return A OperationDescription if a unque Operation can be found with the given SOAP Action
     *         otherwise will return null.
     */
    public OperationDescription getOperationBySOAPAction(String soapAction,
                                                         QName endpoint) {
        HashMap bindingOperations = this.getEndpoint(endpoint).getBinding()
                .getBindingOperations();
        Iterator operationKeySetIterator = bindingOperations.keySet().iterator();
        OperationDescription operation = null;
        int count = 0;
        while (operationKeySetIterator.hasNext()) {
            WSDLBindingOperation bindingOperation = (WSDLBindingOperation) bindingOperations.get(
                    operationKeySetIterator.next());
            Iterator extIterator = bindingOperation.getExtensibilityElements()
                    .iterator();
            while (extIterator.hasNext()) {
                WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();
                if (element.getType().equals(ExtensionConstants.SOAP_OPERATION)) {
                    if (((SOAPOperation) element).getSoapAction().equals(
                            soapAction)) {
                        operation =
                                (OperationDescription) bindingOperation.getOperation();
                        count++;
                    }
                }
            }
        }
        if (1 == count) {
            return operation;
        }
        return null;
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
            serviceContext =
                    new ServiceContext(this, msgContext.getSystemContext());
            //TODO Once the ServiceContext is bound to an incomming serviceContext ID(like a cookie,reference Property) FIX this
            //			msgContext.getSystemContext().registerServiceContext(serviceContext.getServiceInstanceID(),
            // serviceContext);
        } else {
            serviceContext =
                    (ServiceContext) msgContext.getSystemContext()
                    .getServiceContext(msgContext.getServiceInstanceID());
        }

        return serviceContext;

    }

    /**
     * To get the description about the service
     *
     * @return
     */
    public String getServiceDescription() {
        return serviceDescription;
    }

    /**
     * Set the description about the service
     *
     * @param serviceDescription
     */
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public Definition getWSDLDefinition() {
        return difDefinition;
    }

    public void setWSDLDefinition(Definition difDefinition) {
        this.difDefinition = difDefinition;
    }

    public void printWSDL(Writer out, String PortURL) throws AxisFault {
        try {
            Definition wsdlDefinition = this.getWSDLDefinition();
            if (wsdlDefinition != null) {
                Iterator sreviceitr = wsdlDefinition.getServices().keySet()
                        .iterator();
                while (sreviceitr.hasNext()) {
                    wsdlDefinition.removeService((QName) sreviceitr.next());
                }

                //  wsdlDefinition.removeService(this.getName());

                Service service = wsdlDefinition.createService();
                service.setQName(this.getName());

                Port port = wsdlDefinition.createPort();
                SOAPAddress soapAddress = new SOAPAddressImpl();
                soapAddress.setElementType(SOAPConstants.Q_ELEM_SOAP_ADDRESS);
                soapAddress.setLocationURI(PortURL);
                port.addExtensibilityElement(soapAddress);
                port.setName(this.getName().getLocalPart() + "Port");

                Map bindingsMap = wsdlDefinition.getBindings();
                Collection bind_col = bindingsMap.values();
                for (Iterator iterator = bind_col.iterator();
                     iterator.hasNext();) {
                    Binding binding = (Binding) iterator.next();
                    port.setBinding(binding);
                    break;
                }
                service.addPort(port);

                wsdlDefinition.addService(service);
                WSDLFactory.newInstance().newWSDLWriter().writeWSDL(
                        wsdlDefinition, out);
                out.flush();
            } else {
                WSDLFactory.newInstance().newWSDLWriter().writeWSDL(
                        wsdlDefinition, out);
                out.write("<wsdl>WSDL is NOT found</wsdl>");
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
    public void setLastupdate(){
        lastupdate = new Date().getTime();
    }

    public long getLastupdate(){
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

    public void setEndpoint(WSDLEndpoint endpoint){
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
        serviceimpl.setComponentProperty(key,obj);
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
}