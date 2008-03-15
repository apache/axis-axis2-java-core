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

package org.apache.axis2.jaxws.handler;

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.util.LoggingControl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
/*
 * Utility class to perform static utility type of operations on Handlers.
 */
public class HandlerUtils {
    private static Log log = LogFactory.getLog(HandlerUtils.class);

    /**
     * IregisterHandlerHeaders will invoke getHeaders on SOAPHandlers and register these headers
     * with AxisService as Understood headers.
     * @param msgContext
     * @param handlers
     */
    public static List<QName> registerSOAPHandlerHeaders(MessageContext msgContext, List<Handler> handlers){
    	List<QName> understood = new ArrayList<QName>();
        if(msgContext == null){
            return understood;
        }

        for(Handler handler:handlers){
            if(handler instanceof SOAPHandler){
                SOAPHandler soapHandler = (SOAPHandler)handler;
                //Invoking getHeaders.
                if(log.isDebugEnabled()){
                    log.debug("Invoking getHeader() on SOAPHandler");
                }
                Set<QName> headers = soapHandler.getHeaders();
                if(headers!=null){
                    for(QName header:headers){
                        if(!understood.contains(header)){
                            if(log.isDebugEnabled()){
                                log.debug("Adding Header QName" + header + " to uderstoodHeaderQName List");
                            }
                            //Adding this to understood header list.
                            understood.add(header);
                        }
                    }
                }
            }
        }
        return understood;
    }

    /**
     * checkMustUnderstand will validate headers that where delegated by Axis Engine
     * to MessageReceiver for mustUnderstand check.
     * @param msgContext
     * @throws AxisFault
     */
    public static void checkMustUnderstand(MessageContext msgContext, List<QName> understood)throws AxisFault{
        if (msgContext == null || !msgContext.isHeaderPresent()) {
            return;
        }
        SOAPEnvelope envelope = msgContext.getEnvelope();
        if (envelope.getHeader() == null) {
            return;
        }
        
        if(log.isDebugEnabled()){
            log.debug("Reading UnprocessedHeaderNames from Message Context properties");
        }
 
        List<QName> unprocessed = (List)msgContext.getProperty(Constants.UNPROCESSED_HEADER_QNAMES);
        if(unprocessed == null || unprocessed.size() == 0){
            if(log.isDebugEnabled()){
                log.debug("UNPROCESSED_HEADER_QNAMES not found.");
            }
            return;
        }
        //lets go thru each header only if @HandlerChain is present
        if(!canUnderstand(msgContext)){
            QName[] qNames = unprocessed.toArray(new QName[0]);
            String[] headerNames = new String[qNames.length];
            for(int i=0; i<qNames.length; i++){
                headerNames[i] ="{" + qNames[i].getNamespaceURI()+ "}" + qNames[i].getLocalPart();
            }
            QName faultQName = envelope.getVersion().getMustUnderstandFaultCode();
            throw new AxisFault(Messages.getMessage("mustunderstandfailed2", headerNames), faultQName); 
        }

        checkUnprocessed(envelope, unprocessed, understood, msgContext);
        //resetting the FAULTY_HEADER_QNAME to null.
        msgContext.setProperty(Constants.UNPROCESSED_HEADER_QNAMES, null);
    }

    private static void checkUnprocessed(SOAPEnvelope envelope, List<QName> unprocessed, List<QName> understood, MessageContext msgContext) throws AxisFault{
        for (QName headerQName : unprocessed) {           
            if (understood != null && !understood.isEmpty()) {
                if (understood.contains(headerQName)) {
                    if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                        log.debug("MustUnderstand header registered as understood on AxisOperation: " + headerQName);
                    }    
                    continue;
                }
            }
            if (LoggingControl.debugLoggingAllowed && log.isDebugEnabled()) {
                log.debug("MustUnderstand header not processed or registered as understood " + headerQName);
            }
            // Throw a MustUnderstand fault for the current SOAP version
            String prefix = envelope.getNamespace().getPrefix();
            if (!msgContext.isSOAP11()) {
                if (prefix == null || "".equals(prefix)) {
                    prefix = SOAPConstants.SOAP_DEFAULT_NAMESPACE_PREFIX;
                }
                // TODO: should we be using a prefix on the faultcode?  What about
                // the QName object Constants.FAULT_SOAP12_MUSTUNDERSTAND?
                throw new AxisFault(Messages.getMessage("mustunderstandfailed",
                    prefix,
                    headerQName.toString()),
                    SOAP12Constants.FAULT_CODE_MUST_UNDERSTAND);
            } else {
                // TODO: should we be using a prefix on the faultcode?  What about
                // the QName object Constants.FAULT_MUSTUNDERSTAND?
                throw new AxisFault(Messages.getMessage("mustunderstandfailed",
                    prefix,
                    headerQName.toString()),
                    SOAP11Constants.FAULT_CODE_MUST_UNDERSTAND);
            }
        }
    }

    private static boolean canUnderstand(MessageContext msgContext){
        //JAXWSMessageReceiver will only commit to handling must understand if @HandlerChain annotation is present on the
        //Endpoint. This will indicate to AxisEngine that Faulty Header names are understood however the mustUnderstand 
        //Check will be performed in HandlerUtils class after Handlers are injected in application.
        AxisService axisSvc = msgContext.getAxisService();
        if (axisSvc.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER) != null) {
            Parameter param = axisSvc.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER);
            EndpointDescription ed = (EndpointDescription)param.getValue();
            //Lets check if there is a Handler implementation present using Metadata layer.
            //HandlerChain annotation can be present in Service Endpoint or ServiceEndpointInterface.
            // ed.getHandlerChain() looks for HandlerAnnotation at both Endpoint and at SEI.
            if(log.isDebugEnabled()){
                log.debug("Check to see if a jaxws handler is configured.");
            }
            if(ed.getHandlerChain()!=null){
                return true;
            }
            return false;
        }
        else{
            //If we cannot get to ServiceDescription to check for HandlerChain annotation we will return true;
            return true;
        }

    }
}
