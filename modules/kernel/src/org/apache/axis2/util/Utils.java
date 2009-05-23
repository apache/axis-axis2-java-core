/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.util;

import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PhaseRule;
import org.apache.axis2.description.Version;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisError;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map;
import java.net.SocketException;
import java.net.NetworkInterface;
import java.net.InetAddress;

public class Utils {
    private static final Log log = LogFactory.getLog(Utils.class);

    public static void addHandler(Flow flow, Handler handler, String phaseName) {
        HandlerDescription handlerDesc = new HandlerDescription(handler.getName());
        PhaseRule rule = new PhaseRule(phaseName);

        handlerDesc.setRules(rule);
        handler.init(handlerDesc);
        handlerDesc.setHandler(handler);
        flow.addHandler(handlerDesc);
    }

    /**
     * @see org.apache.axis2.util.MessageContextBuilder:createOutMessageContext()
     * @deprecated (post1.1branch)
     */
    public static MessageContext createOutMessageContext(MessageContext inMessageContext)
            throws AxisFault {
        return MessageContextBuilder.createOutMessageContext(inMessageContext);
    }

    public static AxisService createSimpleService(QName serviceName, String className, QName opName)
            throws AxisFault {
        return createSimpleService(serviceName, new RawXMLINOutMessageReceiver(), className,
                                   opName);
    }

    public static AxisService createSimpleServiceforClient(QName serviceName, String className,
                                                           QName opName)
            throws AxisFault {
        return createSimpleServiceforClient(serviceName, new RawXMLINOutMessageReceiver(),
                                            className,
                                            opName);
    }


    public static AxisService createSimpleInOnlyService(QName serviceName,
                                                        MessageReceiver messageReceiver,
                                                        QName opName)
            throws AxisFault {
        AxisService service = new AxisService(serviceName.getLocalPart());
        service.setClassLoader(getContextClassLoader_DoPriv());

        AxisOperation axisOp = new InOnlyAxisOperation(opName);

        axisOp.setMessageReceiver(messageReceiver);
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/" + opName.getLocalPart(),
                                     axisOp);

        return service;
    }

    private static ClassLoader getContextClassLoader_DoPriv() {
        return (ClassLoader) org.apache.axis2.java.security.AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                }
        );
    }


    public static AxisService createSimpleService(QName serviceName,
                                                  MessageReceiver messageReceiver, String className,
                                                  QName opName)
            throws AxisFault {
        AxisService service = new AxisService(serviceName.getLocalPart());

        service.setClassLoader(getContextClassLoader_DoPriv());
        service.addParameter(new Parameter(Constants.SERVICE_CLASS, className));

        AxisOperation axisOp = new InOutAxisOperation(opName);

        axisOp.setMessageReceiver(messageReceiver);
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/" + opName.getLocalPart(),
                                     axisOp);

        return service;
    }

    public static AxisService createSimpleServiceforClient(QName serviceName,
                                                           MessageReceiver messageReceiver,
                                                           String className,
                                                           QName opName)
            throws AxisFault {
        AxisService service = new AxisService(serviceName.getLocalPart());

        service.setClassLoader(getContextClassLoader_DoPriv());
        service.addParameter(new Parameter(Constants.SERVICE_CLASS, className));

        AxisOperation axisOp = new OutInAxisOperation(opName);

        axisOp.setMessageReceiver(messageReceiver);
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);

        return service;
    }

    public static ServiceContext fillContextInformation(AxisService axisService,
                                                        ConfigurationContext configurationContext)
            throws AxisFault {

        // 2. if null, create new opCtxt
        // fill the service group context and service context info
        return fillServiceContextAndServiceGroupContext(axisService, configurationContext);
    }

    private static ServiceContext fillServiceContextAndServiceGroupContext(AxisService axisService,
                                                                           ConfigurationContext configurationContext)
            throws AxisFault {
        String serviceGroupContextId = UUIDGenerator.getUUID();
        ServiceGroupContext serviceGroupContext =
                configurationContext.createServiceGroupContext(axisService.getAxisServiceGroup());

        serviceGroupContext.setId(serviceGroupContextId);
        configurationContext.addServiceGroupContextIntoSoapSessionTable(serviceGroupContext);
        return serviceGroupContext.getServiceContext(axisService);
    }

    /**
     * Break a full path into pieces
     *
     * @return an array where element [0] always contains the service, and element 1, if not null, contains
     *         the path after the first element. all ? parameters are discarded.
     */
    public static String[] parseRequestURLForServiceAndOperation(String path, String servicePath) {
        if (log.isDebugEnabled()) {
            log.debug("parseRequestURLForServiceAndOperation : [" + path + "][" + servicePath + "]");
        }
        if (path == null) {
            return null;
        }
        String[] values = new String[2];

        // TODO. This is kind of brittle. Any service with the name /services would cause fun.
        int index = path.lastIndexOf(servicePath);
        String service;

        if (-1 != index) {
            int serviceStart = index + servicePath.length();

            if (path.length() > serviceStart + 1) {
                service = path.substring(serviceStart + 1);

                int queryIndex = service.indexOf('?');

                if (queryIndex > 0) {
                    service = service.substring(0, queryIndex);
                }

                int operationIndex = service.indexOf('/');

                if (operationIndex > 0) {
                    values[0] = service.substring(0, operationIndex);
                    values[1] = service.substring(operationIndex + 1);
                    operationIndex = values[1].lastIndexOf('/');
                    if (operationIndex > 0) {
                        values[1] = values[1].substring(operationIndex + 1);
                    }
                } else {
                    values[0] = service;
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unable to parse request URL [" + path + "][" + servicePath + "]");
            }
        }

        return values;
    }

    public static ConfigurationContext getNewConfigurationContext(String repositry)
            throws Exception {
        final File file = new File(repositry);
        boolean exists = exists(file);
        if (!exists) {
            throw new Exception("repository directory " + file.getAbsolutePath()
                                + " does not exists");
        }
        File axis2xml = new File(file, "axis.xml");
        String axis2xmlString = null;
        if (exists(axis2xml)) {
            axis2xmlString = axis2xml.getName();
        }
        String path = (String) org.apache.axis2.java.security.AccessController.doPrivileged(
                new PrivilegedAction<String>() {
                    public String run() {
                        return file.getAbsolutePath();
                    }
                }
        );
        return ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(path, axis2xmlString);
    }

    private static boolean exists(final File file) {
        Boolean exists = (Boolean) org.apache.axis2.java.security.AccessController.doPrivileged(
                new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        return new Boolean(file.exists());
                    }
                }
        );
        return exists.booleanValue();
    }

    public static String getParameterValue(Parameter param) {
        if (param == null) {
            return null;
        } else {
            return (String) param.getValue();
        }
    }

    public static String getModuleName(String moduleName, String moduleVersion) {
        if (moduleVersion != null && moduleVersion.length() != 0) {
            moduleName = moduleName + "-" + moduleVersion;
        } 
        return moduleName;
    }

    /**
     * - if he trying to engage the same module then method will returen false
     * - else it will return true
     *
     */
    public static boolean checkVersion(Version module1version,
                                       Version module2version) throws AxisFault {
        if ((module1version !=null && !module1version.equals(module2version)) ||
                module2version !=null && !module2version.equals(module1version)) {
            throw new AxisFault("trying to engage two different module versions " +
                    module1version + " : " + module2version);
        }
        return true;
    }

    public static void calculateDefaultModuleVersion(HashMap modules,
                                                     AxisConfiguration axisConfig) {
        Iterator allModules = modules.values().iterator();
        Map<String,Version> defaultModules = new HashMap<String,Version>();
        while (allModules.hasNext()) {
            AxisModule axisModule = (AxisModule) allModules.next();
            String name = axisModule.getName();
            Version currentDefaultVersion = defaultModules.get(name);
            Version version = axisModule.getVersion();
            if (currentDefaultVersion == null ||
                    (version != null && version.compareTo(currentDefaultVersion) > 0)) {
                defaultModules.put(name, version);
            }
        }
        Iterator def_mod_itr = defaultModules.keySet().iterator();
        while (def_mod_itr.hasNext()) {
            String moduleName = (String) def_mod_itr.next();
            Version version = defaultModules.get(moduleName);
            axisConfig.addDefaultModuleVersion(moduleName, version == null ? null : version.toString());
        }
    }

    /**
     * Check if a MessageContext property is true.
     *
     * @param messageContext the MessageContext
     * @param propertyName   the property name
     * @return true if the property is Boolean.TRUE, "true", 1, etc. or false otherwise
     * @deprecated please use MessageContext.isTrue(propertyName) instead
     */
    public static boolean isExplicitlyTrue(MessageContext messageContext, String propertyName) {
        Object flag = messageContext.getProperty(propertyName);
        return JavaUtils.isTrueExplicitly(flag);
    }

    /**
     * Maps the String URI of the Message exchange pattern to a integer.
     * Further, in the first lookup, it will cache the looked
     * up value so that the subsequent method calls are extremely efficient.
     */
    public static int getAxisSpecifMEPConstant(String messageExchangePattern) {


        int mepConstant = WSDLConstants.MEP_CONSTANT_INVALID;

        if (WSDL2Constants.MEP_URI_IN_OUT.equals(messageExchangePattern) ||
            WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT.equals(messageExchangePattern) ||
            WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OUT.equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_IN_OUT;
        } else if (
                WSDL2Constants.MEP_URI_IN_ONLY.equals(messageExchangePattern) ||
                WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY.equals(messageExchangePattern) ||
                WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_ONLY
                        .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_IN_ONLY;
        } else if (WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                .equals(messageExchangePattern) ||
                                                WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT
                                                        .equals(messageExchangePattern) ||
                                                                                        WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OPTIONAL_OUT
                                                                                                .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_IN_OPTIONAL_OUT;
        } else if (WSDL2Constants.MEP_URI_OUT_IN.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_IN.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_IN
                           .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_OUT_IN;
        } else if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_ONLY
                           .equals(messageExchangePattern) ||
                                                           WSDLConstants.WSDL20_2004_Constants
                                                                   .MEP_URI_OUT_ONLY.equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_OUT_ONLY;
        } else if (WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN
                           .equals(messageExchangePattern) ||
                                                           WSDLConstants.WSDL20_2004_Constants.MEP_URI_OUT_OPTIONAL_IN
                                                                   .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_OUT_OPTIONAL_IN;
        } else if (WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY
                           .equals(messageExchangePattern) ||
                                                           WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY
                                                                   .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY;
        } else if (WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(messageExchangePattern) ||
                   WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_OUT_ONLY
                           .equals(messageExchangePattern) ||
                                                           WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_OUT_ONLY
                                                                   .equals(messageExchangePattern)) {
            mepConstant = WSDLConstants.MEP_CONSTANT_ROBUST_OUT_ONLY;
        }

        if (mepConstant == WSDLConstants.MEP_CONSTANT_INVALID) {
            throw new AxisError(Messages.getMessage("mepmappingerror"));
        }


        return mepConstant;
    }

    /**
     * Get an AxisFault object to represent the SOAPFault in the SOAPEnvelope attached
     * to the provided MessageContext. This first check for an already extracted AxisFault
     * and otherwise does a simple extract.
     * <p/>
     * MUST NOT be passed a MessageContext which does not contain a SOAPFault
     *
     * @param messageContext
     * @return
     */
    public static AxisFault getInboundFaultFromMessageContext(MessageContext messageContext) {
        // Get the fault if it's already been extracted by a handler
        AxisFault result = (AxisFault) messageContext.getProperty(Constants.INBOUND_FAULT_OVERRIDE);
        // Else, extract it from the SOAPBody
        if (result == null) {
            SOAPEnvelope envelope = messageContext.getEnvelope();
            SOAPFault soapFault;
            SOAPBody soapBody;
            if (envelope != null && (soapBody = envelope.getBody()) != null) {
                if ((soapFault = soapBody.getFault()) != null) {
                    return new AxisFault(soapFault, messageContext);
                }
                // If its a REST response the content is not a SOAP envelop and hence we will
                // Have use the soap body as the exception
                if (messageContext.isDoingREST() && soapBody.getFirstElement() != null) {
                    return new AxisFault(soapBody.getFirstElement().toString());
                }
            }
            // Not going to be able to
            throw new IllegalArgumentException(
                    "The MessageContext does not have an associated SOAPFault.");
        }
        return result;
    }
    
    /**
     * This method will provide the logic needed to retrieve an Object's classloader
     * in a Java 2 Security compliant manner.
     */
    public static ClassLoader getObjectClassLoader(final Object object) {
        if(object == null) {
            return null;
        }
        else {
            return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return object.getClass().getClassLoader();
                }
            });
        }
    }
    
    public static int getMtomThreshold(MessageContext msgCtxt){
    	Integer value = null;         
        if(!msgCtxt.isServerSide()){                
	        value = (Integer)msgCtxt.getProperty(Constants.Configuration.MTOM_THRESHOLD);	        
        }else{
        	Parameter param = msgCtxt.getParameter(Constants.Configuration.MTOM_THRESHOLD);
        	if(param!=null){
        		value = (Integer)param.getValue();       		        		
        	}        	        	
        }
        int threshold = (value!=null)?value.intValue():0;
        if(log.isDebugEnabled()){
        	log.debug("MTOM optimized Threshold value ="+threshold);
        }
        return threshold;
    }
     /**
     * Returns the ip address to be used for the replyto epr
     * CAUTION:
     * This will go through all the available network interfaces and will try to return an ip address.
     * First this will try to get the first IP which is not loopback address (127.0.0.1). If none is found
     * then this will return this will return 127.0.0.1.
     * This will <b>not<b> consider IPv6 addresses.
     * <p/>
     * TODO:
     * - Improve this logic to genaralize it a bit more
     * - Obtain the ip to be used here from the Call API
     *
     * @return Returns String.
     * @throws java.net.SocketException
      */
    public static String getIpAddress() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        String address = "127.0.0.1";

        while (e.hasMoreElements()) {
            NetworkInterface netface = (NetworkInterface) e.nextElement();
            Enumeration addresses = netface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress ip = (InetAddress) addresses.nextElement();
                if (!ip.isLoopbackAddress() && isIP(ip.getHostAddress())) {
                    return ip.getHostAddress();
                }
            }
        }

        return address;
    }

    /**
     * First check whether the hostname parameter is there in AxisConfiguration (axis2.xml) ,
     * if it is there then this will retun that as the host name , o.w will return the IP address.
     */
    public static String getIpAddress(AxisConfiguration axisConfiguration) throws SocketException {
        if(axisConfiguration!=null){
            Parameter param = axisConfiguration.getParameter(TransportListener.HOST_ADDRESS);
            if (param != null) {
                String  hostAddress = ((String) param.getValue()).trim();
                if(hostAddress!=null){
                    return hostAddress;
                }
            }
        }
        return getIpAddress();
    }
    
    /**
     * First check whether the hostname parameter is there in AxisConfiguration (axis2.xml) ,
     * if it is there then this will return that as the host name , o.w will return the IP address.
     * @param axisConfiguration
     * @return hostname 
     */
    public static String getHostname(AxisConfiguration axisConfiguration) {
        if(axisConfiguration!=null){
            Parameter param = axisConfiguration.getParameter(TransportListener.HOST_ADDRESS);
            if (param != null) {
                String  hostAddress = ((String) param.getValue()).trim();
                if(hostAddress!=null){
                    return hostAddress;
                }
            }
        }      
        return null;
    }

    private static boolean isIP(String hostAddress) {
        return hostAddress.split("[.]").length == 4;
    }

    /**
     * Get the scheme part from a URI (or URL).
     * 
     * @param uri the URI
     * @return the scheme of the URI
     */
    public static String getURIScheme(String uri) {
        int index = uri.indexOf(':');
        return index > 0 ? uri.substring(0, index) : null;
    }
}
