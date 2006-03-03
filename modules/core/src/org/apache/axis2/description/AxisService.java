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

import com.ibm.wsdl.PortImpl;
import com.ibm.wsdl.extensions.soap.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.modules.Module;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.wsdl.builder.SchemaGenerator;
import org.apache.axis2.wsdl.writer.WOMWriter;
import org.apache.axis2.wsdl.writer.WOMWriterFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;

import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.util.*;

/**
 * Class AxisService
 */
public class AxisService extends AxisDescription {

    private Definition definition = null;
    private Log log = LogFactory.getLog(getClass());
    private String fileName = "";

    private HashMap operationsAliasesMap = null;
//    private HashMap operations = new HashMap();

    // to store module ref at deploy time parsing
    private ArrayList moduleRefs = new ArrayList();

    // to store engaged mdodules
    private ArrayList engagedModules = new ArrayList();
    private String serviceDescription;

    // to store the wsdl definition , which is build at the deployment time
    // to keep the time that last update time of the service
    private long lastupdate;
    private HashMap moduleConfigmap;
    private String name;
    private ClassLoader serviceClassLoader;

    //to keep the XMLScheam getting either from WSDL or java2wsdl
    private XmlSchema schema;

    //wsdl is there for this service or not (in side META-INF)
    private boolean wsdlfound = false;

    //to store the scope of the service
    private String scope;

    //to store default message receivers
    private HashMap messageReceivers;

// to set the handler chain available in phase info
    private boolean useDefaultChains = true;

    //to keep the status of the service , since service can stop at the run time
    private boolean active = true;

    //to keep the service target name space
    private String targetNamespace = SchemaGenerator.TARGET_NAMESPACE;
    private String targetNamespacePrefix = SchemaGenerator.TARGET_NAMESPACE_PREFIX;

    // to store the target namespace for the schema
    private String schematargetNamespace = SchemaGenerator.SCHEMA_TARGET_NAMESPACE;
    private String schematargetNamespacePrefix = SchemaGenerator.SCHEMA_NAMESPACE_PRFIX;

    private boolean enableAllTransport = true;
    private String [] exposeTransports;

    /**
     * Constructor AxisService.
     */
    public AxisService() {
        this.operationsAliasesMap = new HashMap();
        moduleConfigmap = new HashMap();
        //by dafault service scope is for the request
        scope = Constants.SCOPE_REQUEST;
        messageReceivers = new HashMap();
    }

    /**
     * Constructor AxisService.
     */
    public AxisService(String name) {
        this();
        this.name = name;
    }

    public void addMessageReceiver(String mepURL, MessageReceiver messageReceiver) {
        messageReceivers.put(mepURL, messageReceiver);
    }

    public MessageReceiver getMessageReceiver(String mepURL) {
        return (MessageReceiver) messageReceivers.get(mepURL);
    }

    /**
     * Adds module configuration , if there is moduleConfig tag in service.
     *
     * @param moduleConfiguration
     */
    public void addModuleConfig(ModuleConfiguration moduleConfiguration) {
        moduleConfigmap.put(moduleConfiguration.getModuleName(), moduleConfiguration);
    }

    /**
     * Adds an operation to a service if a module is required to do so.
     *
     * @param module
     */
    public void addModuleOperations(AxisModule module, AxisConfiguration axisConfig)
            throws AxisFault {
        HashMap map = module.getOperations();
        Collection col = map.values();

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            AxisOperation axisOperation = copyOperation((AxisOperation) iterator.next());
            ArrayList wsamappings = axisOperation.getWsamappingList();

            if (wsamappings != null) {
                for (int j = 0; j < wsamappings.size(); j++) {
                    String mapping = (String) wsamappings.get(j);
                    this.mapActionToOperation(mapping, axisOperation);
                }
            }
            if (this.getOperation(axisOperation.getName()) == null) {
                // this opration is a control operation.
                axisOperation.setControlOperation(true);
                this.addOperation(axisOperation);
            }
        }
    }

    public void addModuleref(QName moduleref) {
        moduleRefs.add(moduleref);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.AxisService#addOperation(org.apache.axis2.description.AxisOperation)
     */

    /**
     * Method addOperation.
     *
     * @param axisOperation
     */
    public void addOperation(AxisOperation axisOperation) {
        axisOperation.setParent(this);

        Iterator modules = getEngagedModules().iterator();

        while (modules.hasNext()) {
            AxisModule module = (AxisModule) modules.next();
            AxisServiceGroup parent = (AxisServiceGroup) getParent();
            AxisConfiguration axisConfig = null;

            if (parent != null) {
                axisConfig = (AxisConfiguration) parent.getParent();
            }

            try {
                Module moduleImpl = module.getModule();
                if (moduleImpl != null) {
                    // notyfying module for service engagement
                    moduleImpl.engageNotify(axisOperation);
                }
                axisOperation.engageModule(module, axisConfig);
            } catch (AxisFault axisFault) {
                log.info("Trying to engage a module which is already engege:"
                        + module.getName().getLocalPart());
            }
        }

//        operations.put(axisOperation.getName(), axisOperation);
        addChild(axisOperation);
        operationsAliasesMap.put(axisOperation.getName().getLocalPart(), axisOperation);
    }

    /**
     * Gets a copy from module operation.
     *
     * @param axisOperation
     * @return Returns AxisOperation.
     * @throws AxisFault
     */
    private AxisOperation copyOperation(AxisOperation axisOperation) throws AxisFault {
        AxisOperation operation =
                AxisOperationFactory.getOperationDescription(axisOperation.getMessageExchangePattern());

        operation.setMessageReceiver(axisOperation.getMessageReceiver());
        operation.setName(axisOperation.getName());

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

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.AxisService#addToengagedModules(javax.xml.namespace.QName)
     */

    /**
     * Engages a module. It is required to use this method.
     *
     * @param moduleref
     */
    public void engageModule(AxisModule moduleref, AxisConfiguration axisConfig)
            throws AxisFault {
        if (moduleref == null) {
            return;
        }

        boolean needToadd = true;
        Iterator itr_engageModules = engagedModules.iterator();

        while (itr_engageModules.hasNext()) {
            AxisModule module = (AxisModule) itr_engageModules.next();

            if (module.getName().equals(moduleref.getName())) {
                log.debug(moduleref.getName().getLocalPart()
                        + " module has already been engaged on the service. "
                        + " Operation terminated !!!");
                needToadd = false;
            }
        }

        Module moduleImpl = moduleref.getModule();
        if (moduleImpl != null) {
            // notyfying module for service engagement
            moduleImpl.engageNotify(this);
        }
        // adding module operations
        addModuleOperations(moduleref, axisConfig);

        Iterator operations = getOperations();

        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (moduleImpl != null) {
                // notyfying module for service engagement
                moduleImpl.engageNotify(axisOperation);
            }
            axisOperation.engageModule(moduleref, axisConfig);
        }

        if (needToadd) {
            engagedModules.add(moduleref);
        }
    }

    /**
     * Maps an action (aka WSA action) to the given operation. This is used by
     * addressing based dispatching to figure out which operation it is that a
     * given message is for.
     *
     * @param action        the action key
     * @param axisOperation the operation to map to
     */
    public void mapActionToOperation(String action, AxisOperation axisOperation) {
        operationsAliasesMap.put(action, axisOperation);
    }


    public void printSchema(OutputStream out) throws AxisFault {
        schema.write(out);
    }

    public void printPolicy(OutputStream out) throws AxisFault {
        PolicyUtil.writePolicy(getPolicyInclude(), out);
    }

    public void printPolicy(OutputStream out, String operationName) throws AxisFault {
        AxisOperation axisOperation = getOperation(new QName(operationName));
        if (axisOperation == null) {
            throw new AxisFault("invalid operation : " + operationName);
        }
        PolicyUtil.writePolicy(axisOperation.getPolicyInclude(), out);
    }

    private AxisConfiguration getAxisConfiguration() {
        if (getParent() != null) return (AxisConfiguration) getParent().getParent();
        return null;
    }

    public void printWSDL(OutputStream out, String requestIP) throws AxisFault {
        ArrayList eprList = new ArrayList();
        AxisConfiguration axisConfig = getAxisConfiguration();
        if (enableAllTransport) {
            Iterator transports = axisConfig.getTransportsIn().values().iterator();
            while (transports.hasNext()) {
                TransportInDescription transportIn = (TransportInDescription) transports.next();
                TransportListener listener = transportIn.getReceiver();
                if (listener != null) {
                    try {
                        if (listener.getEPRForService(getName(), requestIP) != null) {
                            String address = listener.getEPRForService(getName(), requestIP).getAddress();
                            if (address != null) {
                                eprList.add(address);
                            }
                        }
                    } catch (AxisFault axisFault) {
                        log.info(axisFault.getMessage());
                    }
                }
            }
        } else {
            String trs [] = getExposeTransports();
            for (int i = 0; i < trs.length; i++) {
                String trsName = trs[i];
                TransportInDescription transportIn = axisConfig.getTransportIn(new QName(trsName));
                if (transportIn != null) {
                    TransportListener listener = transportIn.getReceiver();
                    if (listener != null) {
                        try {
                            if (listener.getEPRForService(getName(), requestIP) != null) {
                                String address = listener.getEPRForService(getName(), requestIP).getAddress();
                                if (address != null) {
                                    eprList.add(address);
                                }
                            }
                        } catch (AxisFault axisFault) {
                            log.info(axisFault.getMessage());
                        }
                    }
                }
            }
        }
        String eprArray [] = (String[]) eprList.toArray(new String[eprList.size()]);
        if (getWSDLDefinition() != null) {
            printUsingWSDLDefinition(out, eprArray);
        } else {
            printUsingWOM(out, eprArray);
        }
    }

    private void printUsingWSDLDefinition(OutputStream out, String [] serviceURL) throws AxisFault {
        try {
            Definition wsdlDefinition = getWSDLDefinition();
            Iterator itr_bindings = wsdlDefinition.getBindings().values().iterator();
            Binding binding = null;
            while (itr_bindings.hasNext()) {
                binding = (Binding) itr_bindings.next();
                binding.getExtensibilityElements().clear();
                javax.wsdl.extensions.soap.SOAPBinding soapBinding = new SOAPBindingImpl();
                soapBinding.setStyle("document");
                soapBinding.setTransportURI(Constants.URI_SOAP11_HTTP);
                binding.addExtensibilityElement(soapBinding);

                Iterator bin_ops = binding.getBindingOperations().iterator();
                while (bin_ops.hasNext()) {
                    BindingOperation bindingOperation = (BindingOperation) bin_ops.next();
                    bindingOperation.getExtensibilityElements().clear();
                    javax.wsdl.extensions.soap.SOAPOperation soapOperation = new SOAPOperationImpl();
                    soapOperation.setStyle("document");
                    soapOperation.setSoapActionURI(bindingOperation.getName());
                    bindingOperation.addExtensibilityElement(soapOperation);

                    BindingInput input = bindingOperation.getBindingInput();
                    if (input != null) {
                        input.getExtensibilityElements().clear();
                        javax.wsdl.extensions.soap.SOAPBody soapBody = new SOAPBodyImpl();
                        soapBody.setUse("literal");
                        soapBody.setNamespaceURI(getTargetNamespace());
                        input.addExtensibilityElement(soapBody);
                    }
                    BindingOutput output = bindingOperation.getBindingOutput();
                    if (output != null) {
                        output.getExtensibilityElements().clear();
                        javax.wsdl.extensions.soap.SOAPBody soapBody = new SOAPBodyImpl();
                        soapBody.setUse("literal");
                        soapBody.setNamespaceURI(getTargetNamespace());
                        output.addExtensibilityElement(soapBody);
                    }
                }
            }

            Collection services = wsdlDefinition.getServices().values();

            for (Iterator iterator = services.iterator(); iterator.hasNext();) {
                Service service = (Service) iterator.next();
                service.getPorts().values().clear();

                for (int i = 0; i < serviceURL.length; i++) {
                    String url = serviceURL[i];
                    Port port = new PortImpl();
                    SOAPAddress soapAddress = new SOAPAddressImpl();

                    soapAddress.setElementType(SOAPConstants.Q_ELEM_SOAP_ADDRESS);
                    soapAddress.setLocationURI(url);
                    port.addExtensibilityElement(soapAddress);
                    port.setName(getName() + "Port" + i);
                    port.setBinding(binding);
                    service.addPort(port);
                }
            }

            WSDLFactory.newInstance().newWSDLWriter().writeWSDL(wsdlDefinition, out);
            out.flush();
        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

    private void printUsingWOM(OutputStream out, String [] serviceURL) throws AxisFault {
        AxisService2WOM axisService2WOM = new AxisService2WOM(getSchema(),
                this,
                targetNamespace,
                targetNamespacePrefix,
                serviceURL);
        try {
            WSDLDescription desc = axisService2WOM.generateWOM();

            // populate it with policy information ..
            PolicyUtil.populatePolicy(desc, this);

            WOMWriter womWriter = WOMWriterFactory.createWriter(WSDLConstants.WSDL_1_1);
            womWriter.setdefaultWSDLPrefix("wsdl");
            womWriter.writeWOM(desc, out);

        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

    /**
     * Gets the description about the service which is specified in services.xml.
     *
     * @return Returns String.
     */
    public String getServiceDescription() {
        return serviceDescription;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.AxisService#getClassLoader()
     */

    /**
     * Method getClassLoader.
     *
     * @return Returns ClassLoader.
     */
    public ClassLoader getClassLoader() {
        return this.serviceClassLoader;
    }

    /**
     * Gets the control operation which are added by module like RM.
     */
    public ArrayList getControlOperations() {
        Iterator op_itr = getOperations();
        ArrayList operationList = new ArrayList();

        while (op_itr.hasNext()) {
            AxisOperation operation = (AxisOperation) op_itr.next();

            if (operation.isControlOperation()) {
                operationList.add(operation);
            }
        }

        return operationList;
    }

    /**
     * Method getEngagedModules.
     *
     * @return Returns Collection.
     */
    public Collection getEngagedModules() {
        return engagedModules;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLastupdate() {
        return lastupdate;
    }

    public ModuleConfiguration getModuleConfig(QName moduleName) {
        return (ModuleConfiguration) moduleConfigmap.get(moduleName);
    }

    public ArrayList getModules() {
        return moduleRefs;
    }

    public String getName() {
        return name;
    }

    /**
     * Method getOperation.
     *
     * @param operationName
     * @return Returns AxisOperation.
     */
    public AxisOperation getOperation(QName operationName) {
//        AxisOperation axisOperation = (AxisOperation) operations.get(operationName);
        AxisOperation axisOperation = (AxisOperation) getChild(operationName);

        if (axisOperation == null) {
            axisOperation = (AxisOperation) operationsAliasesMap.get(operationName.getLocalPart());
        }

        return axisOperation;
    }

    /**
     * Returns the AxisOperation which has been mapped to the given action.
     *
     * @param action the action key
     * @return Returns the corresponding AxisOperation or null if it isn't found.
     */
    public AxisOperation getOperationByAction(String action) {
        return (AxisOperation) operationsAliasesMap.get(action);
    }

    /**
     * Returns the operation given a SOAP Action. This
     * method should be called if only one Endpoint is defined for
     * this Service. If more than one Endpoint exists, one of them will be
     * picked. If more than one Operation is found with the given SOAP Action;
     * null will be returned. If no particular Operation is found with the given
     * SOAP Action; null will be returned.
     *
     * @param soapAction SOAP Action defined for the particular Operation
     * @return Returns an AxisOperation if a unique Operation can be found with the given
     *         SOAP Action otherwise will return null.
     */
    public AxisOperation getOperationBySOAPAction(String soapAction) {
        if ((soapAction == null) || soapAction.equals("")) {
            return null;
        }

//        AxisOperation operation = (AxisOperation) operations.get(new QName(soapAction));
        AxisOperation operation = (AxisOperation) getChild(new QName(soapAction));

        if (operation != null) {
            return operation;
        }

        operation = (AxisOperation) operationsAliasesMap.get(soapAction);

        return operation;
    }

    /**
     * Method getOperations.
     *
     * @return Returns HashMap
     */
    public Iterator getOperations() {
        return getChildren();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.ParameterInclude#getParameter(java.lang.String)
     */

    /**
     * Gets only the published operations.
     */
    public ArrayList getPublishedOperations() {
        Iterator op_itr = getOperations();
        ArrayList operationList = new ArrayList();

        while (op_itr.hasNext()) {
            AxisOperation operation = (AxisOperation) op_itr.next();

            if (!operation.isControlOperation()) {
                operationList.add(operation);
            }
        }

        return operationList;
    }

    public Definition getWSDLDefinition() {
        return definition;
    }

    /**
     * Sets the description about the service whish is specified in services.xml
     *
     * @param serviceDescription
     */
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.axis2.description.AxisService#setClassLoader(java.lang.ClassLoader)
     */

    /**
     * Method setClassLoader.
     *
     * @param classLoader
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.serviceClassLoader = classLoader;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Sets the current time as last update time of the service.
     */
    public void setLastupdate() {
        lastupdate = new Date().getTime();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWSDLDefinition(Definition difDefinition) {
        this.definition = difDefinition;
    }

    public XmlSchema getSchema() {
        return schema;
    }

    public void setSchema(XmlSchema schema) {
        //todo : need to support multiple schemas
        this.schema = schema;
    }

    public boolean isWsdlfound() {
        return wsdlfound;
    }

    public void setWsdlfound(boolean wsdlfound) {
        this.wsdlfound = wsdlfound;
    }

    public String getScope() {
        return scope;
    }

    /**
     * @param scope - Available scopes :
     *              Constants.SCOPE_APPLICATION
     *              Constants.SCOPE_TRANSPORT_SESSION
     *              Constants.SCOPE_SOAP_SESSION
     *              Constants.SCOPE_REQUEST.equals
     */
    public void setScope(String scope) {
        if (Constants.SCOPE_APPLICATION.equals(scope) ||
                Constants.SCOPE_TRANSPORT_SESSION.equals(scope) ||
                Constants.SCOPE_SOAP_SESSION.equals(scope) ||
                Constants.SCOPE_REQUEST.equals(scope)) {
            this.scope = scope;
        }
    }

    public boolean isUseDefaultChains() {
        return useDefaultChains;
    }

    public void setUseDefaultChains(boolean useDefaultChains) {
        this.useDefaultChains = useDefaultChains;
    }

    public Object getKey() {
        return getName();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSchematargetNamespace() {
        return schematargetNamespace;
    }

    public void setSchematargetNamespace(String schematargetNamespace) {
        this.schematargetNamespace = schematargetNamespace;
    }

    public String getSchematargetNamespacePrefix() {
        return schematargetNamespacePrefix;
    }

    public void setSchematargetNamespacePrefix(String schematargetNamespacePrefix) {
        this.schematargetNamespacePrefix = schematargetNamespacePrefix;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public String getTargetNamespacePrefix() {
        return targetNamespacePrefix;
    }

    public void setTargetNamespacePrefix(String targetNamespacePrefix) {
        this.targetNamespacePrefix = targetNamespacePrefix;
    }

    public XmlSchemaElement getSchemaElement(QName elementQName) {
        if (schema != null) {
            return schema.getElementByName(elementQName);
        }
        return null;
    }

    public boolean isEnableAllTransport() {
        return enableAllTransport;
    }

    public String[] getExposeTransports() {
        return exposeTransports;
    }

    public void setExposeTransports(String[] exposeTransports) {
        if (exposeTransports.length > 0) {
            enableAllTransport = false;
            this.exposeTransports = exposeTransports;
        }
    }
}
