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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.dataretrieval.AxisDataLocator;
import org.apache.axis2.dataretrieval.AxisDataLocatorImpl;
import org.apache.axis2.dataretrieval.DRConstants;
import org.apache.axis2.dataretrieval.Data;
import org.apache.axis2.dataretrieval.DataRetrievalException;
import org.apache.axis2.dataretrieval.DataRetrievalRequest;
import org.apache.axis2.dataretrieval.LocatorType;
import org.apache.axis2.dataretrieval.OutputForm;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DefaultObjectSupplier;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.ObjectSupplier;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.modules.Module;
import org.apache.axis2.phaseresolver.PhaseResolver;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.http.server.HttpUtils;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaExternal;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.java2wsdl.AnnotationConstants;
import org.apache.ws.java2wsdl.Java2WSDLConstants;
import org.apache.ws.java2wsdl.SchemaGenerator;
import org.apache.ws.java2wsdl.utils.TypeTable;
import org.codehaus.jam.JAnnotation;
import org.codehaus.jam.JMethod;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class AxisService
 */
public class AxisService extends AxisDescription {

    private Map endpointMap = new HashMap();

    /*This is a map between the QName of the element of a message
    *specified in the WSDL and an Operation.  It enables SOAP Body-based
    *dispatching for doc-literal bindings.
    */
    private Map messageElementQNameToOperationMap = new HashMap();

    private int nsCount = 0;
    private static final Log log = LogFactory.getLog(AxisService.class);
    private URL fileName;

    // Maps httpLocations to corresponding operations. Used to dispatch rest messages.
    private HashMap httpLocationDispatcherMap = null;

    private HashMap operationsAliasesMap = null;
    // Collection of aliases that are invalid for this service because they are duplicated across
    // multiple operations under this service.
    private List invalidOperationsAliases = null;
//    private HashMap operations = new HashMap();

    // to store module ref at deploy time parsing
    private ArrayList moduleRefs = null;

    // to store engaged modules
    private ArrayList engagedModules = null;
    private String serviceDescription;

    // to store the wsdl definition , which is build at the deployment time
    // to keep the time that last update time of the service
    private long lastupdate;
    private HashMap moduleConfigmap;
    private String name;
    private ClassLoader serviceClassLoader;

    //to keep the XMLScheam getting either from WSDL or java2wsdl
    private ArrayList schemaList;
    //private XmlSchema schema;

    //wsdl is there for this service or not (in side META-INF)
    private boolean wsdlFound = false;

    //to store the scope of the service
    private String scope;

    //to store default message receivers
    private HashMap messageReceivers;

    // to set the handler chain available in phase info
    private boolean useDefaultChains = true;

    //to keep the status of the service , since service can stop at the run time
    private boolean active = true;

    private boolean elementFormDefault = true;

    //to keep the service target name space
    private String targetNamespace =
            Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE;
    private String targetNamespacePrefix =
            Java2WSDLConstants.TARGETNAMESPACE_PREFIX;

    // to store the target namespace for the schema
    private String schematargetNamespace;// = Java2WSDLConstants.AXIS2_XSD;
    private String schematargetNamespacePrefix =
            Java2WSDLConstants.SCHEMA_NAMESPACE_PRFIX;

    private boolean enableAllTransports = true;
    private List exposedTransports = new ArrayList();

    //To keep reference to ServiceLifeCycle instance , if the user has
    // specified in services.xml
    private ServiceLifeCycle serviceLifeCycle;


    /**
     * Keeps track whether the schema locations are adjusted
     */
    private boolean schemaLocationsAdjusted = false;

    /**
     * A table that keeps a mapping of unique xsd names (Strings)
     * against the schema objects. This is populated in the first
     * instance the schemas are asked for and then used to serve
     * the subsequent requests
     */
    private Map schemaMappingTable = null;

    /**
     * counter variable for naming the schemas
     */
    private int count = 0;
    /**
     * A custom schema Name prefix. if set this will be used to
     * modify the schema names
     */
    private String customSchemaNamePrefix = null;

    /**
     * A custom schema name suffix. will be attached to the
     * schema file name when the files are uniquely named.
     * A good place to add a file extension if needed
     */
    private String customSchemaNameSuffix = null;
    /////////////////////////////////////////
    // WSDL related stuff ////////////////////
    ////////////////////////////////////////
    private NamespaceMap nameSpacesMap;

    private String soapNsUri;
    private String endpointName;
    private String endpointURL;

    // Flag representing whether WS-Addressing is required to use this service.
    // Reflects the wsaw:UsingAddressing wsdl extension element
    private String wsaddressingFlag = AddressingConstants.ADDRESSING_UNSPECIFIED;
    private boolean clientSide = false;

    //To keep a ref to ObjectSupplier instance
    private ObjectSupplier objectSupplier;

    // package to namespace mapping
    private Map p2nMap;

    private TypeTable typeTable;

    // Data Locators for  WS-Mex Support
    private HashMap dataLocators;
    private HashMap dataLocatorClassNames;
    private AxisDataLocatorImpl defaultDataLocator;
    // Define search sequence for datalocator based on Data Locator types.
    LocatorType[] availableDataLocatorTypes = new LocatorType[]{LocatorType.SERVICE_DIALECT,
            LocatorType.SERVICE_LEVEL, LocatorType.GLOBAL_DIALECT, LocatorType.GLOBAL_LEVEL,
            LocatorType.DEFAULT_AXIS};

    // name of the  binding used : use in codegeneration
    private String bindingName;

    // names list keep to preserve the parameter order
    private List operationsNameList;

    public AxisEndpoint getEndpoint(String key) {
        return (AxisEndpoint) endpointMap.get(key);
    }

    public void addEndpoint(String key, AxisEndpoint axisEndpoint) {
        this.endpointMap.put(key, axisEndpoint);
    }

    private boolean customWsld = false;

    public String getWSAddressingFlag() {
        return wsaddressingFlag;
    }

    public void setWSAddressingFlag(String ar) {
        wsaddressingFlag = ar;
        if (wsaddressingFlag == null) {
            wsaddressingFlag = AddressingConstants.ADDRESSING_UNSPECIFIED;
        }
    }

    public boolean isSchemaLocationsAdjusted() {
        return schemaLocationsAdjusted;
    }

    public void setSchemaLocationsAdjusted(boolean schemaLocationsAdjusted) {
        this.schemaLocationsAdjusted = schemaLocationsAdjusted;
    }

    public Map getSchemaMappingTable() {
        return schemaMappingTable;
    }

    public void setSchemaMappingTable(Map schemaMappingTable) {
        this.schemaMappingTable = schemaMappingTable;
    }

    public String getCustomSchemaNamePrefix() {
        return customSchemaNamePrefix;
    }

    public void setCustomSchemaNamePrefix(String customSchemaNamePrefix) {
        this.customSchemaNamePrefix = customSchemaNamePrefix;
    }

    public String getCustomSchemaNameSuffix() {
        return customSchemaNameSuffix;
    }

    public void setCustomSchemaNameSuffix(String customSchemaNameSuffix) {
        this.customSchemaNameSuffix = customSchemaNameSuffix;
    }

    /**
     * Constructor AxisService.
     */
    public AxisService() {
        super();
        this.operationsAliasesMap = new HashMap();
        this.invalidOperationsAliases = new ArrayList();
        moduleConfigmap = new HashMap();
        //by default service scope is for the request
        scope = Constants.SCOPE_REQUEST;
        httpLocationDispatcherMap = new HashMap();
        messageReceivers = new HashMap();
        moduleRefs = new ArrayList();
        engagedModules = new ArrayList();
        schemaList = new ArrayList();
        serviceClassLoader = (ClassLoader) org.apache.axis2.java.security.AccessController
                .doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
        objectSupplier = new DefaultObjectSupplier();
        dataLocators = new HashMap();
        dataLocatorClassNames = new HashMap();

    }

    /**
     * @return name of the port type
     * @deprecated use AxisService#getEndpointName() instead.
     */
    public String getPortTypeName() {
        return endpointName;
    }

    /**
     * @param portTypeName
     * @deprecated use AxisService#setEndpointName() instead
     */
    public void setPortTypeName(String portTypeName) {
        this.endpointName = portTypeName;
    }

    public String getBindingName() {
        return bindingName;
    }

    public void setBindingName(String bindingName) {
        this.bindingName = bindingName;
    }

    /**
     * get the SOAPVersion
     */
    public String getSoapNsUri() {
        return soapNsUri;
    }

    public void setSoapNsUri(String soapNsUri) {
        this.soapNsUri = soapNsUri;
    }

    /**
     * get the endpointName
     */
    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpoint) {
        this.endpointName = endpoint;
    }

    /**
     * Constructor AxisService.
     */
    public AxisService(String name) {
        this();
        this.name = name;
    }

    public void addMessageReceiver(String mepURI, MessageReceiver messageReceiver) {
        if (WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY.equals(mepURI) ||
                WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_ONLY.equals(mepURI)) {
            messageReceivers
                    .put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY, messageReceiver);
            messageReceivers
                    .put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_ONLY, messageReceiver);
        } else if (WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_ONLY.equals(mepURI) ||
                WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_ONLY.equals(mepURI)) {
            messageReceivers
                    .put(WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_ONLY, messageReceiver);
            messageReceivers
                    .put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_ONLY, messageReceiver);
        } else if (WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT.equals(mepURI) ||
                WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OUT.equals(mepURI)) {
            messageReceivers
                    .put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT, messageReceiver);
            messageReceivers
                    .put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OUT, messageReceiver);
        } else if (WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mepURI) ||
                WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mepURI)) {
            messageReceivers.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT,
                                 messageReceiver);
            messageReceivers.put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OPTIONAL_OUT,
                                 messageReceiver);
        } else if (WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_IN.equals(mepURI) ||
                WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_IN.equals(mepURI)) {
            messageReceivers
                    .put(WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_IN, messageReceiver);
            messageReceivers
                    .put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_IN, messageReceiver);
        } else if (WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mepURI) ||
                WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mepURI)) {
            messageReceivers.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN,
                                 messageReceiver);
            messageReceivers.put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_OPTIONAL_IN,
                                 messageReceiver);
        } else if (WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_OUT_ONLY.equals(mepURI) ||
                WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_OUT_ONLY.equals(mepURI)) {
            messageReceivers.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_OUT_ONLY,
                                 messageReceiver);
            messageReceivers.put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_OUT_ONLY,
                                 messageReceiver);
        } else if (WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY.equals(mepURI) ||
                WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY.equals(mepURI)) {
            messageReceivers.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY,
                                 messageReceiver);
            messageReceivers.put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY,
                                 messageReceiver);
        } else {
            messageReceivers.put(mepURI, messageReceiver);
        }
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
            if (this.getOperation(axisOperation.getName()) == null) {
                ArrayList wsamappings = axisOperation.getWsamappingList();
                if (wsamappings != null) {
                    for (int j = 0, size = wsamappings.size(); j < size; j++) {
                        String mapping = (String) wsamappings.get(j);
                        mapActionToOperation(mapping, axisOperation);
                    }
                }
                // this operation is a control operation.
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
                    // notifying module for service engagement
                    moduleImpl.engageNotify(axisOperation);
                }
                axisOperation.engageModule(module, axisConfig);
            } catch (AxisFault axisFault) {
                log.info(Messages.getMessage(
                        "modulealredyengagetoservice", module.getName().getLocalPart()));
            }
        }
        if (axisOperation.getMessageReceiver() == null) {
            axisOperation.setMessageReceiver(
                    loadDefaultMessageReceiver(axisOperation.getMessageExchangePattern(), this));
        }
        if (axisOperation.getInputAction() == null) {
            axisOperation.setSoapAction("urn:" + axisOperation.getName().getLocalPart());
        }
        addChild(axisOperation);

        String operationName = axisOperation.getName().getLocalPart();

        /*
           Some times name of the operation can be different from the name of the first child of the SOAPBody.
           This will put the correct mapping associating that name with  the operation. This will be useful especially for
           the SOAPBodyBasedDispatcher
         */

        Iterator axisMessageIter = axisOperation.getChildren();

        while (axisMessageIter.hasNext()) {
            AxisMessage axisMessage = (AxisMessage) axisMessageIter.next();
            String messageName = axisMessage.getName();
            if (messageName != null && !messageName.equals(operationName)) {
                mapActionToOperation(messageName, axisOperation);
            }
        }

        mapActionToOperation(operationName, axisOperation);

        String action = axisOperation.getInputAction();
        if (action.length() > 0) {
            mapActionToOperation(action, axisOperation);
        }

        ArrayList wsamappings = axisOperation.getWsamappingList();
        if (wsamappings != null) {
            for (int j = 0, size = wsamappings.size(); j < size; j++) {
                String mapping = (String) wsamappings.get(j);
                mapActionToOperation(mapping, axisOperation);
            }
        }

        if (axisOperation.getMessageReceiver() == null) {
            axisOperation.setMessageReceiver(
                    loadDefaultMessageReceiver(
                            axisOperation.getMessageExchangePattern(), this));
        }
    }


    private MessageReceiver loadDefaultMessageReceiver(String mepURL, AxisService service) {
        MessageReceiver messageReceiver;
        if (mepURL == null) {
            mepURL = WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT;
        }
        if (service != null) {
            messageReceiver = service.getMessageReceiver(mepURL);
            if (messageReceiver != null) {
                return messageReceiver;
            }
        }
        if (getParent() != null && getParent().getParent() != null) {
            return ((AxisConfiguration) getParent().getParent()).getMessageReceiver(mepURL);
        }
        return null;
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
                AxisOperationFactory
                        .getOperationDescription(axisOperation.getMessageExchangePattern());

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

        operation.setOutputAction(axisOperation.getOutputAction());
        String[] faultActionNames = axisOperation.getFaultActionNames();
        for (int i = 0; i < faultActionNames.length; i++) {
            operation.addFaultAction(faultActionNames[i],
                                     axisOperation.getFaultAction(faultActionNames[i]));
        }

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
     * @param axisModule
     */
    public void engageModule(AxisModule axisModule, AxisConfiguration axisConfig)
            throws AxisFault {
        if (axisModule == null) {
            throw new AxisFault(Messages.getMessage("modulenf"));
        }
        Iterator itr_engageModules = engagedModules.iterator();
        boolean isEngagable;
        QName moduleName = axisModule.getName();
        while (itr_engageModules.hasNext()) {
            AxisModule module = (AxisModule) itr_engageModules.next();
            QName modu = module.getName();
            isEngagable = org.apache.axis2.util.Utils.checkVersion(moduleName, modu);
            if (!isEngagable) {
                return;
            }
        }

        Module moduleImpl = axisModule.getModule();
        if (moduleImpl != null) {
            // notyfying module for service engagement
            moduleImpl.engageNotify(this);
        }
        // adding module operations
        addModuleOperations(axisModule, axisConfig);

        Iterator operations = getOperations();

        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            axisOperation.engageModule(axisModule, axisConfig);
        }
        engagedModules.add(axisModule);
    }

    /**
     * Maps an action (a SOAPAction or WSA action) to the given operation. This is used by
     * dispatching (both SOAPAction- and WSAddressing- based dispatching) to figure out which
     * operation a given message is for.  Some notes on restrictions of "action"
     * - A null or empty action will be ignored
     * - An action that is a duplicate and references an idential operation is allowed
     * - An acton that is a duplicate and references a different operation is NOT allowed.  In this
     * case, the action for the original operation is removed from the alias table, thus removing
     * the ability to route based on this action.  This is necessary to prevent mis-directing
     * incoming message to the wrong operation based on SOAPAction.
     *
     * @param action        the action key
     * @param axisOperation the operation to map to
     */
    public void mapActionToOperation(String action, AxisOperation axisOperation) {
        if (action == null || "".equals(action)) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "mapActionToOperation: A null or empty action cannot be used to map to an operation.");
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("mapActionToOperation: Mapping Action to Operation: action: " + action +
                    "; operation: " + axisOperation);
        }

        // First check if this action has already been flagged as invalid because it is a duplicate.
        if (invalidOperationsAliases.contains(action)) {
            // This SOAPAction has already been determined to be invalid; log a message
            // and do not add it to the operation alias map.
            if (log.isDebugEnabled()) {
                log.debug("mapActionToOperation: The action: " + action +
                        " can not be used for operation: "
                        + axisOperation + " with operation name: " + axisOperation.getName()
                        + " because that SOAPAction is not unique for this service.");
            }
            return;
        }

        // Check if the action is currently mapping to an operation.
        AxisOperation currentlyMappedOperation = getOperationByAction(action);
        if (currentlyMappedOperation != null) {
            if (currentlyMappedOperation == axisOperation) {
                // This maps to the same operation, then it is already in the alias table, so
                // just silently ignore this mapping request.
                if (log.isDebugEnabled()) {
                    log.debug(
                            "mapActionToOperation: This operation is already mapped to this action: " +
                                    action + "; AxisOperation: "
                                    + currentlyMappedOperation + " named: " +
                                    currentlyMappedOperation.getName());
                }
            } else {
                // This action is already mapped, but it is to a different operation.  Remove
                // the action mapping from the alias table and add it to the list of invalid mappings
                operationsAliasesMap.remove(action);
                invalidOperationsAliases.add(action);
                if (log.isDebugEnabled()) {
                    log.debug(
                            "mapActionToOperation: The action is already mapped to a different operation.  The mapping of the action to any operations will be removed.  Action: "
                                    + action + "; original operation: " + currentlyMappedOperation
                                    + " named " + currentlyMappedOperation.getName()
                                    + "; new operation: " + axisOperation
                                    + " named " + axisOperation.getName());
                }
            }
            return;
        } else {
            operationsAliasesMap.put(action, axisOperation);
        }
    }

    /**
     * Maps an constant string in the whttp:location to the given operation. This is used by
     * RequestURIOperationDispatcher based dispatching to figure out which operation it is that a
     * given message is for.
     *
     * @param string        the constant drawn from whttp:location
     * @param axisOperation the operation to map to
     */
    public void addHttpLocationDispatcherString(String string, AxisOperation axisOperation) {
        httpLocationDispatcherMap.put(string, axisOperation);
    }

    public void printSchema(OutputStream out) throws AxisFault {
        for (int i = 0; i < schemaList.size(); i++) {
            XmlSchema schema = addNameSpaces(i);
            schema.write(out);
        }
    }

    public XmlSchema getSchema(int index) {
        return addNameSpaces(index);
    }

    private XmlSchema addNameSpaces(int i) {
        XmlSchema schema = (XmlSchema) schemaList.get(i);
        NamespaceMap map = (NamespaceMap) nameSpacesMap.clone();
        NamespacePrefixList namespaceContext = schema.getNamespaceContext();
        String prefixes[] = namespaceContext.getDeclaredPrefixes();
        for (int j = 0; j < prefixes.length; j++) {
            String prefix = prefixes[j];
            map.add(prefix, namespaceContext.getNamespaceURI(prefix));
        }
        schema.setNamespaceContext(map);
        return schema;
    }

    public AxisConfiguration getAxisConfiguration() {
        if (getParent() != null) {
            return (AxisConfiguration) getParent().getParent();
        }
        return null;
    }

    /**
     * @param out
     * @param requestIP
     * @param servicePath
     * @throws AxisFault
     */
    public void printWSDL(OutputStream out, String requestIP, String servicePath) throws AxisFault {
        if (isUseUserWSDL()) {
            Parameter wsld4jdefinition = getParameter(WSDLConstants.WSDL_4_J_DEFINITION);
            if (wsld4jdefinition != null) {
                try {
                    Definition definition = (Definition) wsld4jdefinition.getValue();
                    if (isModifyUserWSDLPortAddress()) {
                        setPortAddress(definition);
                    }
                    WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
                    writer.writeWSDL(definition, out);
                } catch (WSDLException e) {
                    throw new AxisFault(e);
                }
            } else {
                printWSDLError(out);
            }
        } else {
            String[] eprArray = getEPRs(requestIP);
            getWSDL(out, eprArray, servicePath);
        }
    }

    public String[] getEPRs() throws AxisFault {
        String requestIP;
        try {
            requestIP = HttpUtils.getIpAddress();
        } catch (SocketException e) {
            throw new AxisFault("Cannot get local IP address", e);
        }
        return getEPRs(requestIP);
    }

    private String[] getEPRs(String requestIP) {
        AxisConfiguration axisConfig = getAxisConfiguration();
        if (axisConfig == null) {
            return null;
        }
        ArrayList eprList = new ArrayList();
        if (enableAllTransports) {
            Iterator transports = axisConfig.getTransportsIn().values().iterator();
            while (transports.hasNext()) {
                TransportInDescription transportIn = (TransportInDescription) transports.next();
                TransportListener listener = transportIn.getReceiver();
                if (listener != null) {
                    try {
                        EndpointReference[] eprsForService =
                                listener.getEPRsForService(this.name, requestIP);
                        if (eprsForService != null) {
                            for (int i = 0; i < eprsForService.length; i++) {
                                EndpointReference endpointReference = eprsForService[i];
                                String address = endpointReference.getAddress();
                                if (address != null) {
                                    eprList.add(address);
                                }
                            }
                        }
                    } catch (AxisFault axisFault) {
                        log.warn(axisFault.getMessage());
                    }
                }
            }
        } else {
            List trs = this.exposedTransports;
            for (int i = 0; i < trs.size(); i++) {
                String trsName = (String) trs.get(i);
                TransportInDescription transportIn = axisConfig.getTransportIn(
                        new QName(trsName));
                if (transportIn != null) {
                    TransportListener listener = transportIn.getReceiver();
                    if (listener != null) {
                        try {
                            EndpointReference[] eprsForService =
                                    listener.getEPRsForService(this.name, requestIP);
                            if (eprsForService != null) {
                                for (int j = 0; j < eprsForService.length; j++) {
                                    EndpointReference endpointReference = eprsForService[j];
                                    String address = endpointReference.getAddress();
                                    if (address != null) {
                                        eprList.add(address);
                                    }
                                }
                            }
                        } catch (AxisFault axisFault) {
                            log.warn(axisFault.getMessage());
                        }
                    }
                }
            }
        }
        return (String[]) eprList.toArray(new String[eprList.size()]);
    }

    /**
     * @param out
     * @param requestIP
     * @throws AxisFault
     * @deprecated try to use the method which takes three arguments
     */
    public void printWSDL(OutputStream out, String requestIP) throws AxisFault {
        printWSDL(out, requestIP, "services");
    }

    /**
     * Print the WSDL with a default URL. This will be called only during codegen time.
     *
     * @param out
     * @throws AxisFault
     */
    public void printWSDL(OutputStream out) throws AxisFault {
        if (isUseUserWSDL()) {
            Parameter wsld4jdefinition = getParameter(WSDLConstants.WSDL_4_J_DEFINITION);
            if (wsld4jdefinition != null) {
                try {
                    Definition definition = (Definition) wsld4jdefinition.getValue();
                    if (isModifyUserWSDLPortAddress()) {
                        setPortAddress(definition);
                    }
                    WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
                    writer.writeWSDL(definition, out);
                } catch (WSDLException e) {
                    throw new AxisFault(e);
                }
            } else {
                printWSDLError(out);
            }
        } else {
            setWsdlFound(true);
            //pick the endpointName and take it as the epr for the WSDL
            getWSDL(out, new String[]{this.endpointName}, "services");
        }
    }

    private void setPortAddress(Definition definition) throws AxisFault {
        setPortAddress(definition, null);
    }

    private void setPortAddress(Definition definition, String requestIP) throws AxisFault {
        Iterator serviceItr = definition.getServices().values().iterator();
        while (serviceItr.hasNext()) {
            Service serviceElement = (Service) serviceItr.next();
            Iterator portItr = serviceElement.getPorts().values().iterator();
            while (portItr.hasNext()) {
                Port port = (Port) portItr.next();
                List list = port.getExtensibilityElements();
                for (int i = 0; i < list.size(); i++) {
                    Object extensibilityEle = list.get(i);
                    if (extensibilityEle instanceof SOAPAddress) {
                        if (requestIP == null) {
                            ((SOAPAddress) extensibilityEle).setLocationURI(getEPRs()[0]);
                        } else {
                            ((SOAPAddress) extensibilityEle).setLocationURI(getEPRs(requestIP)[0]);
                        }
                    }
                }
            }
        }
    }

    private void getWSDL(OutputStream out, String[] serviceURL,
                         String servicePath) throws AxisFault {
        // Retrieve WSDL using the same data retrieval path for GetMetadata request.
        DataRetrievalRequest request = new DataRetrievalRequest();
        request.putDialect(DRConstants.SPEC.DIALECT_TYPE_WSDL);
        request.putOutputForm(OutputForm.INLINE_FORM);

        MessageContext context = new MessageContext();
        context.setAxisService(this);
        context.setTo(new EndpointReference(serviceURL[0]));

        Data[] result = getData(request, context);
        OMElement wsdlElement;
        if (result != null && result[0] != null) {
            wsdlElement = (OMElement) (result[0].getData());
            try {
                wsdlElement.serialize(out);
                out.flush();
                out.close();
            } catch (Exception e) {
                throw new AxisFault(e);
            }
        }
    }

    private void printWSDLError(OutputStream out) throws AxisFault {
        try {
            String wsdlntfound = "<error>" +
                    "<description>Unable to generate WSDL for this service</description>" +
                    "<reason>If you wish Axis2 to automatically generate the WSDL, then please use one of the RPC message " +
                    "receivers for the service(s)/operation(s) in services.xml. If you have added a custom WSDL in the " +
                    "META-INF directory, then please make sure that the name of the service in services.xml " +
                    "(/serviceGroup/service/@name) is the same as in the " +
                    "custom wsdl's service name (/wsdl:definitions/wsdl:service/@name). </reason>" +
                    "</error>";
            out.write(wsdlntfound.getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new AxisFault(e);
        }
    }

    //WSDL 2.0
    public void printWSDL2(OutputStream out) throws AxisFault {
        if (isUseUserWSDL()) {
            Parameter wsld4jdefinition = getParameter(WSDLConstants.WSDL_4_J_DEFINITION);
            if (wsld4jdefinition != null) {
                try {
                    String error = "<error>" +
                            "<description>Unable to showtwo will WSDL for this service</description>" +
                            "<reason>WSDL 2.0 document is to be shown. But we do not support WSDL 2.0" +
                            "serialization yet.</reason>" +
                            "</error>";
                    out.write(error.getBytes());
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    throw new AxisFault(e);
                }
            } else {
                printWSDLError(out);
            }
        } else {
            setWsdlFound(true);
            //pick the endpointName and take it as the epr for the WSDL
            getWSDL2(out, new String[]{this.endpointName});
        }
    }

    public void printWSDL2(OutputStream out,
                           String requestIP,
                           String servicePath) throws AxisFault {
        getWSDL2(out, getEPRs());
    }

    private void getWSDL2(OutputStream out, String[] serviceURL) throws AxisFault {
        AxisService2WSDL2 axisService2WSDL2 = new AxisService2WSDL2(this, serviceURL);
        try {
            OMElement wsdlElement = axisService2WSDL2.generateOM();
            wsdlElement.serialize(out);
            out.flush();
            out.close();
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

    public URL getFileName() {
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
            axisOperation = (AxisOperation) operationsAliasesMap.get(
                    operationName.getLocalPart());
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
        if ((soapAction == null) || soapAction.length() == 0) {
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

    /**
     * Sets the description about the service which is specified in services.xml
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

    public void setFileName(URL fileName) {
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

    public ArrayList getSchema() {
        return schemaList;
    }

    public void addSchema(XmlSchema schema) {
        if (schema != null) {
            schemaList.add(schema);
            if (schema.getTargetNamespace() != null) {
                addSchemaNameSpace(schema);
            }
        }
    }

    public void addSchema(Collection schemas) {
        Iterator iterator = schemas.iterator();
        while (iterator.hasNext()) {
            XmlSchema schema = (XmlSchema) iterator.next();
            schemaList.add(schema);
            addSchemaNameSpace(schema);
        }
    }

    public boolean isWsdlFound() {
        return wsdlFound;
    }

    public void setWsdlFound(boolean wsdlFound) {
        this.wsdlFound = wsdlFound;
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
        return this.name;
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
        XmlSchemaElement element;
        for (int i = 0; i < schemaList.size(); i++) {
            XmlSchema schema = (XmlSchema) schemaList.get(i);
            if (schema != null) {
                element = schema.getElementByName(elementQName);
                if (element != null) {
                    return element;
                }
            }
        }
        return null;
    }

    public boolean isEnableAllTransports() {
        return enableAllTransports;
    }

    /**
     * To eneble service to be expose in all the transport
     *
     * @param enableAllTransports
     */
    public void setEnableAllTransports(boolean enableAllTransports) {
        this.enableAllTransports = enableAllTransports;
    }

    public List getExposedTransports() {
        return this.exposedTransports;
    }

    public void setExposedTransports(List transports) {
        enableAllTransports = false;
        this.exposedTransports = transports;
    }

    public void addExposedTransport(String transport) {
        enableAllTransports = false;
        if (!this.exposedTransports.contains(transport)) {
            this.exposedTransports.add(transport);
        }
    }

    public void removeExposedTransport(String transport) {
        enableAllTransports = false;
        this.exposedTransports.remove(transport);
    }

    public boolean isExposedTransport(String transport) {
        return exposedTransports.contains(transport);
    }

    public void disengageModule(AxisModule module) {
        AxisConfiguration axisConfig = getAxisConfiguration();
        if (axisConfig != null) {
            PhaseResolver phaseResolver = new PhaseResolver(axisConfig);
            if (axisConfig.isEngaged(module.getName())) {
                removeModuleOperations(module);
                Iterator operations = getChildren();
                while (operations.hasNext()) {
                    AxisOperation axisOperation = (AxisOperation) operations.next();
                    phaseResolver.disengageModuleFromOperationChain(module, axisOperation);
                    axisOperation.removeFromEngagedModuleList(module);
                }
            } else {
                if (isEngaged(module.getName())) {
                    phaseResolver.disengageModuleFromGlobalChains(module);
                    removeModuleOperations(module);
                    Iterator operations = getChildren();
                    while (operations.hasNext()) {
                        AxisOperation axisOperation = (AxisOperation) operations.next();
                        phaseResolver.disengageModuleFromOperationChain(module, axisOperation);
                        axisOperation.removeFromEngagedModuleList(module);
                    }
                }
            }
        }
        engagedModules.remove(module);
    }

    /**
     * To remove module operations added at the time of engagement
     */
    private void removeModuleOperations(AxisModule module) {
        HashMap moduleOerations = module.getOperations();
        if (moduleOerations != null) {
            Iterator moduleOperations_itr = moduleOerations.values().iterator();
            while (moduleOperations_itr.hasNext()) {
                AxisOperation operation = (AxisOperation) moduleOperations_itr.next();
                removeOperation(operation.getName());
            }
        }
    }

    public boolean isEngaged(QName moduleName) {
        AxisModule module = getAxisConfiguration().getModule(moduleName);
        if (module == null) {
            return false;
        }
        Iterator engagedModuleItr = engagedModules.iterator();
        while (engagedModuleItr.hasNext()) {
            AxisModule axisModule = (AxisModule) engagedModuleItr.next();
            if (axisModule.getName().getLocalPart().equals(module.getName().getLocalPart())) {
                return true;
            }
        }
        return false;
    }

    //#######################################################################################
    //                    APIs to create AxisService

    //

    /**
     * To create a AxisService for a given WSDL and the created client is most suitable for client side
     * invocation not for server side invocation. Since all the soap action and wsa action is added to
     * operations
     *
     * @param wsdlURL         location of the WSDL
     * @param wsdlServiceName name of the service to be invoke , if it is null then the first one will
     *                        be selected if there are more than one
     * @param portName        name of the port , if there are more than one , if it is null then the
     *                        first one in the  iterator will be selected
     * @param options         Service client options, to set the target EPR
     * @return AxisService , the created service will be return
     */
    public static AxisService createClientSideAxisService(URL wsdlURL,
                                                          QName wsdlServiceName,
                                                          String portName,
                                                          Options options) throws AxisFault {
        try {
            InputStream in = wsdlURL.openConnection().getInputStream();
            Document doc = XMLUtils.newDocument(in);
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            reader.setFeature("javax.wsdl.importDocuments", true);
            Definition wsdlDefinition = reader.readWSDL(null, doc);
            return createClientSideAxisService(wsdlDefinition, wsdlServiceName, portName, options);
        } catch (IOException e) {
            log.error(e);
            throw new AxisFault("IOException : " + e.getMessage());
        } catch (ParserConfigurationException e) {
            log.error(e);
            throw new AxisFault("ParserConfigurationException : " + e.getMessage());
        } catch (SAXException e) {
            log.error(e);
            throw new AxisFault("SAXException : " + e.getMessage());
        } catch (WSDLException e) {
            log.error(e);
            throw new AxisFault("WSDLException : " + e.getMessage());
        }
    }

    public static AxisService createClientSideAxisService(Definition wsdlDefinition,
                                                          QName wsdlServiceName,
                                                          String portName,
                                                          Options options) throws AxisFault {
        WSDL11ToAxisServiceBuilder serviceBuilder =
                new WSDL11ToAxisServiceBuilder(wsdlDefinition, wsdlServiceName, portName);
        serviceBuilder.setServerSide(false);
        AxisService axisService = serviceBuilder.populateService();
        options.setTo(new EndpointReference(axisService.getEndpointName()));
        options.setSoapVersionURI(axisService.getSoapNsUri());
        return axisService;
    }

    /**
     * To create an AxisService using given service impl class name
     * first generate schema corresponding to the given java class , next for each methods AxisOperation
     * will be created.
     * <p/>
     * Note : Inorder to work this properly RPCMessageReceiver should be available in the class path
     * otherewise operation can not continue
     *
     * @param implClass
     * @param axisConfig
     * @return return created AxisSrevice
     */
    public static AxisService createService(String implClass,
                                            AxisConfiguration axisConfig,
                                            Class messageReceiverClass) throws AxisFault {
        return createService(implClass, axisConfig, messageReceiverClass, null, null);
    }

    /**
     * messageReceiverClassMap will hold the MessageReceivers for given meps. Key will be the
     * mep and value will be the instance of the MessageReceiver class.
     * Ex:
     * Map mrMap = new HashMap();
     * mrMap.put("http://www.w3.org/2004/08/wsdl/in-only",
     * RPCInOnlyMessageReceiver.class.newInstance());
     * mrMap.put("http://www.w3.org/2004/08/wsdl/in-out",
     * RPCMessageReceiver.class.newInstance());
     *
     * @param implClass
     * @param axisConfiguration
     * @param messageReceiverClassMap
     * @param targetNamespace
     * @param schemaNamespace
     * @throws AxisFault
     */
    public static AxisService createService(String implClass,
                                            AxisConfiguration axisConfiguration,
                                            Map messageReceiverClassMap,
                                            String targetNamespace,
                                            String schemaNamespace,
                                            ClassLoader loader) throws AxisFault {
        Parameter parameter = new Parameter(Constants.SERVICE_CLASS, implClass);
        OMElement paraElement = Utils.getParameter(Constants.SERVICE_CLASS, implClass, false);
        parameter.setParameterElement(paraElement);
        AxisService axisService = new AxisService();
        axisService.setUseDefaultChains(false);
        axisService.addParameter(parameter);

        if (schemaNamespace == null) {
            schemaNamespace = axisService.getSchematargetNamespace();
        }

        int index = implClass.lastIndexOf(".");
        String serviceName;
        if (index > 0) {
            serviceName = implClass.substring(index + 1, implClass.length());
        } else {
            serviceName = implClass;
        }

        axisService.setName(serviceName);
        axisService.setClassLoader(loader);

        ClassLoader serviceClassLoader = axisService.getClassLoader();
        SchemaGenerator schemaGenerator;
        ArrayList excludeOpeartion = new ArrayList();


        NamespaceMap map = new NamespaceMap();
        map.put(Java2WSDLConstants.AXIS2_NAMESPACE_PREFIX,
                Java2WSDLConstants.AXIS2_XSD);
        map.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX,
                Java2WSDLConstants.URI_2001_SCHEMA_XSD);
        axisService.setNameSpacesMap(map);


        try {
            schemaGenerator = new SchemaGenerator(serviceClassLoader,
                                                  implClass, schemaNamespace,
                                                  axisService.getSchematargetNamespacePrefix());
            schemaGenerator.setElementFormDefault(Java2WSDLConstants.FORM_DEFAULT_UNQUALIFIED);
            axisService.setElementFormDefault(false);
            excludeOpeartion.add("init");
            excludeOpeartion.add("setOperationContext");
            excludeOpeartion.add("destroy");
            excludeOpeartion.add("startUp");
            schemaGenerator.setExcludeMethods(excludeOpeartion);
            axisService.addSchema(schemaGenerator.generateSchema());
            axisService.setSchematargetNamespace(schemaGenerator.getSchemaTargetNameSpace());
            axisService.setTypeTable(schemaGenerator.getTypeTable());
            if (targetNamespace == null) {
                targetNamespace = schemaGenerator.getSchemaTargetNameSpace();
            }
            if (targetNamespace != null && !"".equals(targetNamespace)) {
                axisService.setTargetNamespace(targetNamespace);
            }
        } catch (Exception e) {
            throw new AxisFault(e);
        }

        JMethod[] method = schemaGenerator.getMethods();
        TypeTable table = schemaGenerator.getTypeTable();

        PhasesInfo pinfo = axisConfiguration.getPhasesInfo();

        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];
            JAnnotation methodAnnon = jmethod.getAnnotation(AnnotationConstants.WEB_METHOD);
            if (methodAnnon != null) {
                if (methodAnnon.getValue(AnnotationConstants.EXCLUDE).asBoolean()) {
                    continue;
                }
            }
            if (!jmethod.isPublic()) {
                // no need to expose , private and protected methods
                continue;
            } else if (excludeOpeartion.contains(jmethod.getSimpleName())) {
                continue;
            }
            AxisOperation operation = Utils.getAxisOperationforJmethod(jmethod, table);
            String mep = operation.getMessageExchangePattern();
            MessageReceiver mr;
            if (messageReceiverClassMap != null) {

                if (messageReceiverClassMap.get(mep) != null) {
                    Object obj = messageReceiverClassMap.get(mep);
                    if (obj instanceof MessageReceiver) {
                        mr = (MessageReceiver) obj;
                        operation.setMessageReceiver(mr);
                    } else {
                        log.error(
                                "Object is not an instance of MessageReceiver, thus, default MessageReceiver has been set");
                        mr = axisConfiguration
                                .getMessageReceiver(operation.getMessageExchangePattern());
                        operation.setMessageReceiver(mr);
                    }
                } else {
                    log.error(
                            "Required MessageReceiver couldn't be found, thus, default MessageReceiver has been used");
                    mr = axisConfiguration
                            .getMessageReceiver(operation.getMessageExchangePattern());
                    operation.setMessageReceiver(mr);
                }
            } else {
                log.error(
                        "MessageRecevierClassMap couldn't be found, thus, default MessageReceiver has been used");
                mr = axisConfiguration.getMessageReceiver(operation.getMessageExchangePattern());
                operation.setMessageReceiver(mr);
            }
            pinfo.setOperationPhases(operation);
            axisService.addOperation(operation);
        }
        return axisService;

    }


    public static AxisService createService(String implClass,
                                            AxisConfiguration axisConfiguration,
                                            Map messageReceiverClassMap,
                                            String targetNamespace,
                                            String schemaNamespace) throws AxisFault {
        return createService(implClass,
                             axisConfiguration,
                             messageReceiverClassMap,
                             targetNamespace,
                             schemaNamespace,
                             axisConfiguration.getServiceClassLoader());


    }

    /**
     * To create a service for a given Java class with user defined schema and target
     * namespaces. This method should be used iff, the operations in the service class is homogeneous.
     *
     * @param implClass            : full name of the class
     * @param axisConfig           : current AxisConfgiuration
     * @param messageReceiverClass : Message receiver that you want to use
     * @param targetNameSpace      : Service namespace
     * @param schemaNameSpace      : Schema namespace
     * @throws AxisFault
     */

    public static AxisService createService(String implClass,
                                            AxisConfiguration axisConfig,
                                            Class messageReceiverClass,
                                            String targetNameSpace,
                                            String schemaNameSpace) throws AxisFault {
        Parameter parameter = new Parameter(Constants.SERVICE_CLASS, implClass);
        OMElement paraElement = Utils.getParameter(Constants.SERVICE_CLASS, implClass, false);
        parameter.setParameterElement(paraElement);
        AxisService axisService = new AxisService();
        axisService.setUseDefaultChains(false);
        axisService.addParameter(parameter);

        if (schemaNameSpace == null) {
            schemaNameSpace = axisService.getSchematargetNamespace();
        }

        int index = implClass.lastIndexOf(".");
        String serviceName;
        if (index > 0) {
            serviceName = implClass.substring(index + 1, implClass.length());
        } else {
            serviceName = implClass;
        }

        axisService.setName(serviceName);
        axisService.setClassLoader(axisConfig.getServiceClassLoader());

        ClassLoader serviceClassLoader = axisService.getClassLoader();
        SchemaGenerator schemaGenerator;
        ArrayList excludeOpeartion = new ArrayList();


        NamespaceMap map = new NamespaceMap();
        map.put(Java2WSDLConstants.AXIS2_NAMESPACE_PREFIX,
                Java2WSDLConstants.AXIS2_XSD);
        map.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX,
                Java2WSDLConstants.URI_2001_SCHEMA_XSD);
        axisService.setNameSpacesMap(map);


        try {
            schemaGenerator = new SchemaGenerator(serviceClassLoader,
                                                  implClass, schemaNameSpace,
                                                  axisService.getSchematargetNamespacePrefix());
            schemaGenerator.setElementFormDefault(Java2WSDLConstants.FORM_DEFAULT_UNQUALIFIED);
            axisService.setElementFormDefault(false);
            excludeOpeartion.add("init");
            excludeOpeartion.add("setOperationContext");
            excludeOpeartion.add("destroy");
            excludeOpeartion.add("startUp");
            schemaGenerator.setExcludeMethods(excludeOpeartion);
            axisService.addSchema(schemaGenerator.generateSchema());
            axisService.setSchematargetNamespace(schemaGenerator.getSchemaTargetNameSpace());
            axisService.setTypeTable(schemaGenerator.getTypeTable());
            if (targetNameSpace == null) {
                targetNameSpace = schemaGenerator.getSchemaTargetNameSpace();
            }
            if (targetNameSpace != null && !"".equals(targetNameSpace)) {
                axisService.setTargetNamespace(targetNameSpace);
            }
        } catch (Exception e) {
            throw new AxisFault(e);
        }

        JMethod[] method = schemaGenerator.getMethods();
        TypeTable table = schemaGenerator.getTypeTable();

        PhasesInfo pinfo = axisConfig.getPhasesInfo();

        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];
            JAnnotation methodAnnon = jmethod.getAnnotation(AnnotationConstants.WEB_METHOD);
            if (methodAnnon != null) {
                if (methodAnnon.getValue(AnnotationConstants.EXCLUDE).asBoolean()) {
                    continue;
                }
            }
            if (!jmethod.isPublic()) {
                // no need to expose , private and protected methods
                continue;
            } else if (excludeOpeartion.contains(jmethod.getSimpleName())) {
                continue;
            }
            AxisOperation operation = Utils.getAxisOperationforJmethod(jmethod, table);

            // loading message receivers
            try {
                MessageReceiver messageReceiver =
                        (MessageReceiver) messageReceiverClass.newInstance();
                operation.setMessageReceiver(messageReceiver);
            } catch (IllegalAccessException e) {
                throw new AxisFault(
                        "IllegalAccessException occurred during message receiver loading"
                                + e.getMessage());
            } catch (InstantiationException e) {
                throw new AxisFault(
                        "InstantiationException occurred during message receiver loading"
                                + e.getMessage());
            }
            pinfo.setOperationPhases(operation);
            axisService.addOperation(operation);
        }
        return axisService;

    }

    public static AxisService createService(String implClass,
                                            AxisConfiguration axisConfig) throws AxisFault {
        Class clazz;
        try {
            clazz = Loader.loadClass("org.apache.axis2.rpc.receivers.RPCMessageReceiver");
        } catch (ClassNotFoundException e) {
            throw new AxisFault("ClassNotFoundException occured during message receiver loading"
                    + e.getMessage());
        }

        return createService(implClass, axisConfig, clazz);

    }

    public void removeOperation(QName opName) {
        AxisOperation operation = getOperation(opName);
        if (operation != null) {
            removeChild(opName);
            ArrayList mappingList = operation.getWsamappingList();
            if (mappingList != null) {
                for (int i = 0; i < mappingList.size(); i++) {
                    String actionMapping = (String) mappingList.get(i);
                    operationsAliasesMap.remove(actionMapping);
                }
            }
            operationsAliasesMap.remove(operation.getName().getLocalPart());
        }
    }

    public Map getNameSpacesMap() {
        return nameSpacesMap;
    }

    public void setNameSpacesMap(NamespaceMap nameSpacesMap) {
        this.nameSpacesMap = nameSpacesMap;
    }

    private void addSchemaNameSpace(XmlSchema schema) {
        String targetNameSpace = schema.getTargetNamespace();
        String prefix = schema.getNamespaceContext().getPrefix(targetNameSpace);

        boolean found = false;
        if (nameSpacesMap != null && nameSpacesMap.size() > 0) {
            Iterator itr = nameSpacesMap.values().iterator();
            Set keys = nameSpacesMap.keySet();
            while (itr.hasNext()) {
                String value = (String) itr.next();
                if (value.equals(targetNameSpace) && keys.contains(prefix)) {
                    found = true;
                }
            }
        }
        if (nameSpacesMap == null) {
            nameSpacesMap = new NamespaceMap();
        }
        if (!found) {
            nameSpacesMap.put("ns" + nsCount, targetNameSpace);
            nsCount++;
        }
    }

    /**
     * runs the schema mappings if it has not been run previously
     * it is best that this logic be in the axis service since one can
     * call the axis service to populate the schema mappings
     */
    public void populateSchemaMappings() {

        //populate the axis service with the necessary schema references
        ArrayList schema = this.schemaList;
        if (!this.schemaLocationsAdjusted) {
            Hashtable nameTable = new Hashtable();
            //calculate unique names for the schemas
            calcualteSchemaNames(schema, nameTable);
            //adjust the schema locations as per the calculated names
            adjustSchemaNames(schema, nameTable);
            //reverse the nametable so that there is a mapping from the
            //name to the schemaObject
            setSchemaMappingTable(swapMappingTable(nameTable));
            setSchemaLocationsAdjusted(true);
        }
    }

    /**
     * run 1 -calcualte unique names
     *
     * @param schemas
     */
    private void calcualteSchemaNames(List schemas, Hashtable nameTable) {
        //first traversal - fill the hashtable
        for (int i = 0; i < schemas.size(); i++) {
            XmlSchema schema = (XmlSchema) schemas.get(i);
            XmlSchemaObjectCollection includes = schema.getIncludes();

            for (int j = 0; j < includes.getCount(); j++) {
                Object item = includes.getItem(j);
                XmlSchema s;
                if (item instanceof XmlSchemaExternal) {
                    XmlSchemaExternal externalSchema = (XmlSchemaExternal) item;
                    s = externalSchema.getSchema();
                    if (s != null && nameTable.get(s) == null) {
                        //insert the name into the table
                        insertIntoNameTable(nameTable, s);
                        //recursively call the same procedure
                        calcualteSchemaNames(Arrays.asList(
                                new XmlSchema[]{s}),
                                             nameTable);
                    }
                }
            }
        }
    }

    /**
     * A quick private sub routine to insert the names
     *
     * @param nameTable
     * @param s
     */
    private void insertIntoNameTable(Hashtable nameTable, XmlSchema s) {
        nameTable.put(s,
                      ("xsd" + count++)
                              + (customSchemaNameSuffix != null ?
                              customSchemaNameSuffix :
                              ""));
    }

    /**
     * Run 2  - adjust the names
     */
    private void adjustSchemaNames(List schemas, Hashtable nameTable) {
        Hashtable importedSchemas = new Hashtable();
        //process the schemas in the main schema list
        for (int i = 0; i < schemas.size(); i++) {
            adjustSchemaName((XmlSchema) schemas.get(i), nameTable, importedSchemas);
        }
        //process all the rest in the name table
        Enumeration nameTableKeys = nameTable.keys();
        while (nameTableKeys.hasMoreElements()) {
            adjustSchemaName((XmlSchema) nameTableKeys.nextElement(), nameTable, importedSchemas);

        }
    }

    /**
     * Adjust a single schema
     *
     * @param parentSchema
     * @param nameTable
     */
    private void adjustSchemaName(XmlSchema parentSchema, Hashtable nameTable,
                                  Hashtable importedScheams) {
        XmlSchemaObjectCollection includes = parentSchema.getIncludes();
        for (int j = 0; j < includes.getCount(); j++) {
            Object item = includes.getItem(j);
            if (item instanceof XmlSchemaExternal) {
                XmlSchemaExternal xmlSchemaExternal = (XmlSchemaExternal) item;
                XmlSchema s = xmlSchemaExternal.getSchema();
                adjustSchemaLocation(s, xmlSchemaExternal, nameTable, importedScheams);
            }
        }

    }

    /**
     * Adjusts a given schema location
     *
     * @param s
     * @param xmlSchemaExternal
     * @param nameTable
     */
    private void adjustSchemaLocation(XmlSchema s, XmlSchemaExternal xmlSchemaExternal,
                                      Hashtable nameTable, Hashtable importedScheams) {
        if (s != null) {
            String schemaLocation = xmlSchemaExternal.getSchemaLocation();
            if (importedScheams.get(schemaLocation) != null) {
                xmlSchemaExternal.setSchemaLocation(
                        (String) importedScheams.get(xmlSchemaExternal.getSchemaLocation()));
            } else {
                String newscheamlocation = customSchemaNamePrefix == null ?
                        //use the default mode
                        (getName() +
                                "?xsd=" +
                                nameTable.get(s)) :
                        //custom prefix is present - add the custom prefix
                        (customSchemaNamePrefix +
                                nameTable.get(s));
                xmlSchemaExternal.setSchemaLocation(
                        newscheamlocation);
                importedScheams.put(schemaLocation, newscheamlocation);
            }

        }
    }

    /**
     * Swap the key,value pairs
     *
     * @param originalTable
     */
    private Map swapMappingTable(Map originalTable) {
        HashMap swappedTable = new HashMap(originalTable.size());
        Iterator keys = originalTable.keySet().iterator();
        Object key;
        while (keys.hasNext()) {
            key = keys.next();
            swappedTable.put(originalTable.get(key), key);
        }

        return swappedTable;
    }

    public boolean isClientSide() {
        return clientSide;
    }

    public void setClientSide(boolean clientSide) {
        this.clientSide = clientSide;
    }

    public boolean isElementFormDefault() {
        return elementFormDefault;
    }

    public void setElementFormDefault(boolean elementFormDefault) {
        this.elementFormDefault = elementFormDefault;
    }

    /**
     * User can set a parameter in services.xml saying he want to show the original wsdl
     * that he put into META-INF once someone ask for ?wsdl
     * so if you want to use your own wsdl then add following parameter into
     * services.xml
     * <parameter name="useOriginalwsdl">true</parameter>
     */
    public boolean isUseUserWSDL() {
        Parameter parameter = getParameter("useOriginalwsdl");
        if (parameter != null) {
            String value = (String) parameter.getValue();
            if ("true".equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * By default the port address in user WSDLs is modified, set
     * the following parameter to override this behaviour
     * <parameter name="modifyUserWSDLPortAddress">false</parameter>
     */
    public boolean isModifyUserWSDLPortAddress() {
        Parameter parameter = getParameter("modifyUserWSDLPortAddress");
        if (parameter != null) {
            String value = (String) parameter.getValue();
            if ("false".equals(value)) {
                return false;
            }
        }
        return true;
    }

    public ServiceLifeCycle getServiceLifeCycle() {
        return serviceLifeCycle;
    }

    public void setServiceLifeCycle(ServiceLifeCycle serviceLifeCycle) {
        this.serviceLifeCycle = serviceLifeCycle;
    }

    public Map getP2nMap() {
        return p2nMap;
    }

    public void setP2nMap(Map p2nMap) {
        this.p2nMap = p2nMap;
    }

    public ObjectSupplier getObjectSupplier() {
        return objectSupplier;
    }

    public void setObjectSupplier(ObjectSupplier objectSupplier) {
        this.objectSupplier = objectSupplier;
    }

    public TypeTable getTypeTable() {
        return typeTable;
    }

    public void setTypeTable(TypeTable typeTable) {
        this.typeTable = typeTable;
    }


    /**
     * Find a data locator from the available data locators (both configured and default ones) to retrieve Metadata or data
     * specified in the request.
     *
     * @param request    an {@link DataRetrievalRequest} object
     * @param msgContext message context
     * @return array of {@link Data} object for the request.
     * @throws AxisFault
     */

    public Data[] getData(DataRetrievalRequest request,
                          MessageContext msgContext) throws AxisFault {

        Data[] data = null;
        String dialect = request.getDialect();
        AxisDataLocator dataLocator = null;
        int nextDataLocatorIndex = 0;
        int totalLocators = availableDataLocatorTypes.length;
        for (int i = 0; i < totalLocators; i++) {
            dataLocator = getDataLocator(availableDataLocatorTypes[i], dialect);
            if (dataLocator != null) {
                nextDataLocatorIndex = i + 1;
                break;
            }
        }

        data = dataLocator.getData(request, msgContext);
        // Null means Data Locator not understood request. Automatically find
        // Data Locator in the hierarchy to process the request.
        if (data == null) {
            if (nextDataLocatorIndex < totalLocators) {
                data = bubbleupDataLocators(nextDataLocatorIndex, request,
                                            msgContext);
            }

        }
        return data;
    }

    /*
    * To search the next Data Locator from the available Data Locators that understood
    * the data retrieval request.
    */
    private Data[] bubbleupDataLocators(int nextIndex,
                                        DataRetrievalRequest request, MessageContext msgContext)
            throws AxisFault {
        Data[] data = null;
        if (nextIndex < availableDataLocatorTypes.length) {
            AxisDataLocator dataLocator = getDataLocator(
                    availableDataLocatorTypes[nextIndex], request.getDialect());
            nextIndex++;
            if (dataLocator != null) {
                data = dataLocator.getData(request, msgContext);
                if (data == null) {
                    data = bubbleupDataLocators(nextIndex, request, msgContext);
                } else {
                    return data;
                }

            } else {
                data = bubbleupDataLocators(nextIndex, request, msgContext);
            }


        }
        return data;
    }

    /**
     * Save data Locator configured at service level for this Axis Service
     *
     * @param dialect-             an absolute URI represents the Dialect i.e. WSDL, Policy, Schema or
     *                             "ServiceLevel" for non-dialect service level data locator.
     * @param dataLocatorClassName - class name of the Data Locator configured to support data retrieval
     *                             for the specified dialect.
     */
    public void addDataLocatorClassNames(String dialect, String dataLocatorClassName) {
        dataLocatorClassNames.put(dialect, dataLocatorClassName);
    }


    /*
    * Get data locator instance based on the LocatorType and dialect.
    */
    private AxisDataLocator getDataLocator(LocatorType locatorType, String dialect)
            throws AxisFault {
        AxisDataLocator locator = null;
        if (locatorType == LocatorType.SERVICE_DIALECT) {
            locator = getServiceDataLocator(dialect);
        } else if (locatorType == LocatorType.SERVICE_LEVEL) {
            locator = getServiceDataLocator(DRConstants.SERVICE_LEVEL);
        } else if (locatorType == LocatorType.GLOBAL_DIALECT) {
            locator = getGlobalDataLocator(dialect);
        } else if (locatorType == LocatorType.GLOBAL_LEVEL) {
            locator = getGlobalDataLocator(DRConstants.GLOBAL_LEVEL);
        } else if (locatorType == LocatorType.DEFAULT_AXIS) {
            locator = getDefaultDataLocator();
        } else {
            locator = getDefaultDataLocator();
        }


        return locator;
    }

    // Return default Axis2 Data Locator
    private AxisDataLocator getDefaultDataLocator() throws DataRetrievalException {

        if (defaultDataLocator == null) {
            defaultDataLocator = new AxisDataLocatorImpl(this);
        }

        defaultDataLocator.loadServiceData();

        return defaultDataLocator;
    }


    /*
    * Checks if service level data locator configured for specified dialect.
    * Returns an instance of the data locator if exists, and null otherwise.
    */
    private AxisDataLocator getServiceDataLocator(String dialect)
            throws AxisFault {
        AxisDataLocator locator = null;
        locator = (AxisDataLocator) dataLocators.get(dialect);
        if (locator == null) {
            String className = (String) dataLocatorClassNames.get(dialect);
            if (className != null) {
                locator = loadDataLocator(className);
                dataLocators.put(dialect, locator);
            }

        }

        return locator;

    }

    /*
    * Checks if global level data locator configured for specified dialect.
    * @param dialect- an absolute URI represents the Dialect i.e. WSDL, Policy, Schema or
    *                 "GlobalLevel" for non-dialect Global level data locator.
    * Returns an instance of the data locator if exists, and null otherwise.
    */

    public AxisDataLocator getGlobalDataLocator(String dialect)
            throws AxisFault {
        AxisConfiguration axisConfig = getAxisConfiguration();
        AxisDataLocator locator = null;
        if (axisConfig != null) {
            locator = axisConfig.getDataLocator(dialect);
            if (locator == null) {
                String className = axisConfig.getDataLocatorClassName(dialect);
                if (className != null) {
                    locator = loadDataLocator(className);
                    axisConfig.addDataLocator(dialect, locator);
                }
            }
        }

        return locator;

    }


    protected AxisDataLocator loadDataLocator(String className)
            throws AxisFault {

        AxisDataLocator locator = null;

        try {
            Class dataLocator;
            dataLocator = Class.forName(className, true, serviceClassLoader);
            locator = (AxisDataLocator) dataLocator.newInstance();
        } catch (ClassNotFoundException e) {
            throw new AxisFault(e);
        } catch (IllegalAccessException e) {
            throw new AxisFault(e);
        } catch (InstantiationException e) {
            throw new AxisFault(e);

        }

        return locator;
    }

    /**
     * Set the map of WSDL message element QNames to AxisOperations for this
     * service.  This map is used during SOAP Body-based routing for
     * document/literal bare services to match the first child element of the
     * SOAP Body element to an operation.  (Routing for RPC and
     * document/literal wrapped services occurs via the operationsAliasesMap.)
     * <p/>
     * From section 4.7.6 of the WS-I BP 1.1:
     * the "operation signature" is "the fully qualified name of the child
     * element of SOAP body of the SOAP input message described by an operation
     * in a WSDL binding," and thus this map must be from a QName to an
     * operation.
     *
     * @param messageElementQNameToOperationMap
     *         The map from WSDL message
     *         element QNames to
     *         AxisOperations.
     */
    public void setMessageElementQNameToOperationMap(Map messageElementQNameToOperationMap) {
        this.messageElementQNameToOperationMap = messageElementQNameToOperationMap;
    }

    /**
     * Look up an AxisOperation for this service based off of an element QName
     * from a WSDL message element.
     *
     * @param messageElementQName The QName to search for.
     * @return The AxisOperation registered to the QName or null if no match
     *         was found.
     * @see #setMessageElementQNameToOperationMap(Map)
     */
    public AxisOperation getOperationByMessageElementQName(QName messageElementQName) {
        return (AxisOperation) messageElementQNameToOperationMap.get(messageElementQName);
    }

    /**
     * Add an entry to the map between element QNames in WSDL messages and
     * AxisOperations for this service.
     *
     * @param messageElementQName The QName of the element on the input message
     *                            that maps to the given operation.
     * @param operation           The AxisOperation to be mapped to.
     * @see #setMessageElementQNameToOperationMap(Map)
     */
    public void addMessageElementQNameToOperationMapping(QName messageElementQName,
                                                         AxisOperation operation) {
        messageElementQNameToOperationMap.put(messageElementQName, operation);
    }

    public String getEndpointURL() {
        return endpointURL;
    }

    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    public Map getEndpoints() {
        return endpointMap;
    }

    public boolean isCustomWsld() {
        return customWsld;
    }

    public void setCustomWsld(boolean customWsld) {
        this.customWsld = customWsld;
    }

    public List getOperationsNameList() {
        return operationsNameList;
    }

    public void setOperationsNameList(List operationsNameList) {
        this.operationsNameList = operationsNameList;
    }
}
