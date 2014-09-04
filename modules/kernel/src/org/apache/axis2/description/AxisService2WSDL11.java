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

package org.apache.axis2.description;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.ExternalPolicySerializer;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PolicyLocator;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.Utils;
import org.apache.axis2.util.WSDLSerializationUtil;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyReference;
import org.apache.neethi.PolicyRegistry;
import org.apache.neethi.PolicyRegistryImpl;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class AxisService2WSDL11 implements Java2WSDLConstants {

	protected AxisService axisService;

	protected String serviceName;

    private String targetNamespace;

	private OMElement definition;

	private OMNamespace soap;

	private OMNamespace soap12;

	private OMNamespace http;

	private OMNamespace mime;

	private OMNamespace tns;

	private OMNamespace wsdl;

	private OMNamespace wsaw;

	private String style = DOCUMENT;

	private String use = LITERAL;

	private Map<String, Policy> policiesInDefinitions;

	private ExternalPolicySerializer serializer;

	private HashMap messagesMap;
	
	private boolean checkIfEndPointActive = true;
	
	public AxisService2WSDL11() { }

    public AxisService2WSDL11(AxisService service) throws Exception {
        this.axisService = service;
        this.serviceName = service.getName();
        init();
	}

    /**
     * Sets whether to make a check if endpoint is active before adding the endpoint 
     * to the WSDL.  By default an endpoint is not added if a transport for the endpoint
     * is not found. 
     * 
     * @param flag true=check if endpoint is active before adding endpoint.
     *             false=add endpoint independent of whether endpoint is active. 
     */
    public void setCheckIfEndPointActive(boolean flag) {
        checkIfEndPointActive = flag;
    }

	protected void init() throws AxisFault {
/*
		// the EPR list of AxisService contains REST EPRs as well. Those REST
		// EPRs will be used to generated HTTPBinding
		// and rest of the EPRs will be used to generate SOAP 1.1 and 1.2
		// bindings. Let's first initialize those set of
		// EPRs now to be used later, especially when we generate the WSDL.
        String[] serviceEndpointURLs = axisService.getEPRs();
		if (serviceEndpointURLs == null) {
			Map endpointMap = axisService.getEndpoints();
			if (endpointMap.size() > 0) {
				Iterator endpointItr = endpointMap.values().iterator();
				if (endpointItr.hasNext()) {
					AxisEndpoint endpoint = (AxisEndpoint) endpointItr.next();
					serviceEndpointURLs = new String[] { endpoint
							.getEndpointURL() };
				}

			} else {
				serviceEndpointURLs = new String[] { axisService
						.getEndpointName() };
			}
		}
*/
		this.targetNamespace = axisService.getTargetNamespace();

		serializer = new ExternalPolicySerializer();
		// CHECKME check whether service.getAxisConfiguration() return null ???

		AxisConfiguration configuration = axisService.getAxisConfiguration();
		if (configuration != null) {
			serializer.setAssertionsToFilter(configuration
					.getLocalPolicyAssertions());
		}
	}

	public AxisService2WSDL11(AxisService service, String serviceName)
			throws Exception {
		this.axisService = service;
		this.serviceName = serviceName;
		init();
	}

    /**
     * Build the OM structure of the WSDL document
     *
     * @return an OMElement containing a WSDL document
     * @throws Exception
     */
    public OMElement generateOM() throws Exception {

		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement definition = generateDefinition(fac);
		      

		// adding documentation element
		// <documentation>&lt;b&gt;NEW!&lt;/b&gt; This method accepts an ISBN
		// string and returns &lt;b&gt;Amazon.co.uk&lt;/b&gt; Sales Rank for
		// that book.</documentation>
		OMElement documentation = generateDocumentation(fac);
		if(documentation != null){
		    definition.addChild(documentation);
		}
		
		OMElement types = generateTypes(fac);
        if(types != null){
            definition.addChild(types);
        }	
        
        List<OMElement> messages = generateMessages(fac);
        for( OMElement message : messages){
            if(message != null){
                definition.addChild(message);
            }
        }
        
        OMElement portType = generatePortType(fac);
        definition.addChild(portType);
        
        if(!isDisableSOAP11()){
            definition.addChild(portType);            
        }

		generateService(fac, definition, isDisableREST(), isDisableSOAP12() , isDisableSOAP11());
		addPoliciesToDefinitionElement(policiesInDefinitions.values().iterator(), definition);

		return definition;
	}

	protected List<OMElement> generateMessages(OMFactory fac) {
		HashSet faultMessageNames = new HashSet();
		List<OMElement> messageList = new ArrayList<OMElement>();
		messagesMap = new HashMap();

		Iterator operations = axisService.getOperations();
		while (operations.hasNext()) {
			AxisOperation axisOperation = (AxisOperation) operations.next();
			if (axisOperation.isControlOperation()) {
				continue;
			}
			String MEP = axisOperation.getMessageExchangePattern();
			if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage inaxisMessage =
                        axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
				if (inaxisMessage != null) {
					messageList.add(writeMessage(inaxisMessage, fac));
					messageList.add(generateHeaderMessages(inaxisMessage, fac));
				}
			}

			if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisMessage outAxisMessage = axisOperation
						.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				if (outAxisMessage != null) {
				    messageList.add(writeMessage(outAxisMessage, fac));
				    messageList.add(generateHeaderMessages(outAxisMessage, fac));
				}
			}

			// generate fault Messages
			ArrayList<AxisMessage> faultyMessages = this.extractWSDL11FaultMessages(axisOperation);
			if (faultyMessages != null) {
                for (AxisMessage faultyMessage : faultyMessages) {
                    String name = faultyMessage.getName();
                    if (faultMessageNames.add(name)) {
                        messageList.add(writeMessage(faultyMessage, fac));
                        messageList.add(generateHeaderMessages(faultyMessage, fac));
                    }
                }
			}
		}
		return messageList;
	}
	
	/**
	 * Checks if the given MEP is supported for containing fault messages.
	 */
	private boolean isWSDL11FaultyMessagesValidForMEP(String mepURI) {
		return !(WSDL2Constants.MEP_URI_IN_ONLY.equals(mepURI) || 
				WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(mepURI));
	}
	
	/**
	 * Return the fault messages only if it's not an in-only operation.
	 * For WSDL11 generation, the fault messages must be not present in the operation,
	 * as mentioned in <link>http://www.w3.org/TR/wsdl#_one-way</link>.
	 */
	private ArrayList<AxisMessage> extractWSDL11FaultMessages(AxisOperation axisOperation) {
		String mepURI = axisOperation.getMessageExchangePattern();
		if (this.isWSDL11FaultyMessagesValidForMEP(mepURI)) {
			return axisOperation.getFaultMessages();
		} else {
			return new ArrayList<AxisMessage>();
		}
	}
	
	/**
	 * @see AxisService2WSDL11#extractWSDL11FaultMessages(AxisOperation)
	 */
	private ArrayList<AxisBindingMessage> extractWSDL11FaultMessages(
			AxisBindingOperation axisBindingOperation) {
		String mepURI = axisBindingOperation.getAxisOperation().getMessageExchangePattern();
		if (this.isWSDL11FaultyMessagesValidForMEP(mepURI)) {
			return axisBindingOperation.getFaults();
		} else {
			return new ArrayList<AxisBindingMessage>();
		}
	}

	private OMElement generateHeaderMessages(AxisMessage axismessage, OMFactory fac) {
		ArrayList extList = axismessage.getSoapHeaders();
        for (Object anExtList : extList) {
            SOAPHeaderMessage header = (SOAPHeaderMessage)anExtList;
            OMElement messageElement = fac.createOMElement(MESSAGE_LOCAL_NAME, wsdl);
            messageElement.addAttribute(ATTRIBUTE_NAME, header.getMessage().getLocalPart(), null);
            OMElement messagePart = fac.createOMElement(PART_ATTRIBUTE_NAME, wsdl);
            messageElement.addChild(messagePart);
            messagePart.addAttribute(ATTRIBUTE_NAME, header.part(), null);
            if (header.getElement() == null) {
                throw new RuntimeException(ELEMENT_ATTRIBUTE_NAME
                                           + " is null for " + header.getMessage());
            }
            messagePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                                     WSDLSerializationUtil.getPrefix(header.getElement()
                                             .getNamespaceURI(), axisService.getNamespaceMap())
                                     + ":" + header.getElement().getLocalPart(), null);
            return messageElement;
        }
        return null;
	}

	private OMElement writeMessage(AxisMessage axismessage, OMFactory fac) {
		if (axismessage.getName() != null && messagesMap.get(axismessage.getName()) == null) {
			messagesMap.put(axismessage.getName(), axismessage);
			QName schemaElementName = axismessage.getElementQName();
			OMElement messageElement = fac.createOMElement(MESSAGE_LOCAL_NAME, wsdl);
			messageElement.addAttribute(ATTRIBUTE_NAME, axismessage.getName(), null);			
			if (schemaElementName != null) {
				OMElement messagePart = fac.createOMElement(PART_ATTRIBUTE_NAME, wsdl);
				messageElement.addChild(messagePart);
				if (axismessage.getMessagePartName() != null) {
					messagePart.addAttribute(ATTRIBUTE_NAME,
                                             axismessage.getMessagePartName(),
                                             null);
				} else {
					messagePart.addAttribute(ATTRIBUTE_NAME, axismessage.getPartName(), null);
				}
				messagePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
						WSDLSerializationUtil.getPrefix(schemaElementName.getNamespaceURI(),
                                                        axisService.getNamespaceMap())
								+ ":" + schemaElementName.getLocalPart(), null);
			}
			return messageElement; 
		}
        return null;

	}

	/**
	 * Builds the &lt;portType&gt; element in the passed WSDL definition. When
	 * this returns successfully, there will be a new child element under
	 * definitons for the portType.
	 * 
	 * @param fac
	 *            the active OMFactory
	 * @param defintions
	 *            the WSDL &lt;definitions&gt; element
	 * @return 
	 * @throws Exception
	 *             if there's a problem
	 */
	protected OMElement generatePortType(OMFactory fac)
			throws Exception {
		OMElement portType = fac.createOMElement(PORT_TYPE_LOCAL_NAME, wsdl);

        String portTypeName = serviceName + PORT_TYPE_SUFFIX;

        Parameter param = this.axisService.getParameter(Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG);
        if (param != null){
            portTypeName = (String) param.getValue();
        }

		portType.addAttribute(ATTRIBUTE_NAME, portTypeName, null);

		addPolicyAsExtAttribute(axisService, portType, fac);

		for (Iterator operations = axisService.getOperations(); operations
				.hasNext();) {
			AxisOperation axisOperation = (AxisOperation) operations.next();
			if (axisOperation.isControlOperation()
					|| axisOperation.getName() == null) {
				continue;
			}
			String operationName = axisOperation.getName().getLocalPart();
			OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
					wsdl);
			WSDLSerializationUtil.addWSDLDocumentationElement(axisOperation,
					operation, fac, wsdl);
			portType.addChild(operation);
			operation.addAttribute(ATTRIBUTE_NAME, operationName, null);
			addPolicyAsExtAttribute(axisOperation, operation,fac);

			String MEP = axisOperation.getMessageExchangePattern();
			if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisMessage inaxisMessage = axisOperation
						.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
				if (inaxisMessage != null) {
					OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME,
							wsdl);
					WSDLSerializationUtil.addWSDLDocumentationElement(
							inaxisMessage, input, fac, wsdl);
					input.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
							+ ":" + inaxisMessage.getName(), null);
					addPolicyAsExtAttribute(inaxisMessage, input,fac);

					WSDLSerializationUtil.addWSAWActionAttribute(input,
							axisOperation.getInputAction(), wsaw);
					operation.addChild(input);
				}
			}

			if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisMessage outAxisMessage = axisOperation
						.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				if (outAxisMessage != null) {
					OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME,
							wsdl);
					WSDLSerializationUtil.addWSDLDocumentationElement(
							outAxisMessage, output, fac, wsdl);
					output.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
							+ ":" + outAxisMessage.getName(), null);
					addPolicyAsExtAttribute(outAxisMessage, output, fac);
					WSDLSerializationUtil.addWSAWActionAttribute(output,
							axisOperation.getOutputAction(), wsaw);
					operation.addChild(output);
				}
			}

			// generate fault Messages
			ArrayList<AxisMessage> faultMessages = this.extractWSDL11FaultMessages(axisOperation);
			if (faultMessages != null) {
                for (AxisMessage faultMessage : faultMessages) {
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME, wsdl);
                    WSDLSerializationUtil.addWSDLDocumentationElement(faultMessage,
                                                                      fault,
                                                                      fac,
                                                                      wsdl);
                    fault.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
                                                           + ":" + faultMessage.getName(), null);
                    fault.addAttribute(ATTRIBUTE_NAME, faultMessage.getName(), null);
                    WSDLSerializationUtil.addWSAWActionAttribute(fault,
                                                                 axisOperation.getFaultAction(
                                                                		 faultMessage.getName()),
                                                                 wsaw);
                    // TODO add policies for fault messages
                    operation.addChild(fault);
                }
			}

		}
		return portType;
	}

	/**
	 * Generate the WSDL &lt;service&gt; element
	 * 
	 * @param fac
	 *            the active OMFactory
	 * @param defintions
	 *            the WSDL &lt;definitions&gt; element under which to put the
	 *            service
	 * @param disableREST
	 *            if false, generate REST binding, if true, don't
	 * @param disableSOAP12
	 *            if false, generate SOAP 1.2 binding, if true, don't
	 * @return 
	 * @throws Exception
	 *             if there's a problem
	 */
	public OMElement generateService(OMFactory fac, OMElement defintions, boolean disableREST,
                                boolean disableSOAP12, boolean disableSOAP11)
            throws Exception {
		OMElement service = fac.createOMElement(SERVICE_LOCAL_NAME, wsdl);
		defintions.addChild(service);
		service.addAttribute(ATTRIBUTE_NAME, serviceName, null);

        if (!disableSOAP11) {
			generateSoap11Port(fac, defintions, service);
		}

        if (!disableSOAP12) {
			// generateSOAP12Ports(fac, service);
			generateSoap12Port(fac, defintions, service);
		}


        addPolicyAsExtElement(PolicyInclude.SERVICE_POLICY, axisService.getPolicySubject(),
                              service);
		// addPolicyAsExtElement(PolicyInclude.AXIS_SERVICE_POLICY, axisService.
		// getPolicyInclude(), service);

		if (!disableREST) {
			// generateHTTPPorts(fac, service);
			generateHttpPort(fac, definition, service);
		}
		return service;
	}

	private void writeSoapHeaders(AxisMessage inaxisMessage, OMFactory fac,
			OMElement input, OMNamespace soapNameSpace) throws Exception {
		ArrayList extElementList;
		extElementList = inaxisMessage.getSoapHeaders();
		if (extElementList != null) {
			Iterator elements = extElementList.iterator();
			while (elements.hasNext()) {
				SOAPHeaderMessage soapheader = (SOAPHeaderMessage) elements
						.next();
				addSOAPHeader(fac, input, soapheader, soapNameSpace);
			}
		}
	}

	private void addExtensionElement(OMFactory fac, OMElement element,
			String name, String att1Name, String att1Value, String att2Name,
			String att2Value, OMNamespace soapNameSpace) {
		OMElement soapbinding = fac.createOMElement(name, soapNameSpace);
		element.addChild(soapbinding);
		soapbinding.addAttribute(att1Name, att1Value, null);
		if (att2Name != null) {
			soapbinding.addAttribute(att2Name, att2Value, null);
		}
	}

	private void setDefinitionElement(OMElement defintion) {
		this.definition = defintion;
	}

	private void addSOAPHeader(OMFactory fac, OMElement element,
			SOAPHeaderMessage header, OMNamespace soapNameSpace) {
		OMElement extElement = fac.createOMElement("header", soapNameSpace);
		element.addChild(extElement);
		String use = header.getUse();
		if (use != null) {
			extElement.addAttribute("use", use, null);
		}
		if (header.part() != null) {
			extElement.addAttribute("part", header.part(), null);
		}
		if (header.getMessage() != null) {
			extElement.addAttribute("message", WSDLSerializationUtil.getPrefix(
					targetNamespace, axisService.getNamespaceMap())
					+ ":" + header.getMessage().getLocalPart(), null);
		}
	}

	private void addPolicyAsExtElement(int type, PolicySubject policySubject,
			OMElement parentElement) throws Exception {
	    Collection<PolicyComponent> elementList = policySubject.getAttachedPolicyComponents();

        for (Object policyElement : elementList) {
            if (policyElement instanceof Policy) {
                OMElement child = PolicyUtil.getPolicyComponentAsOMElement(
                        (PolicyComponent)policyElement, serializer);

//                OMNode firstChildElem = parentElement.getFirstElement();

                // if (firstChildElem == null) {
                // parentElement.addChild(child);
                // } else {
                // firstChildElem.insertSiblingBefore(child);
                // }
                // there is a problem with the OM insertSiblingBefore element
                // with
                // drops the already exists elements.
                // since there is no any techical problem of adding policy
                // elements after other
                // children temporaliy fix this as it is.
                // one OM fix this issue we can revert this change.
                parentElement.addChild(child);

            } else if (policyElement instanceof PolicyReference) {
                OMElement child = PolicyUtil
                        .getPolicyComponentAsOMElement((PolicyComponent)policyElement);
                OMElement firstChildElem = parentElement.getFirstElement();

                if (firstChildElem == null) {
                    parentElement.addChild(child);
                } else {
                    firstChildElem.insertSiblingBefore(child);
                }

                PolicyRegistry reg = new PolicyRegistryImpl();
                String key = ((PolicyReference)policyElement).getURI();

                if (key.startsWith("#")) {
                    key = key.substring(key.indexOf("#") + 1);
                }

                Policy p = reg.lookup(key);

                if (p == null) {
                    throw new Exception("Policy not found for uri : " + key);
                }

                addPolicyToDefinitionElement(key, p);
            }
        }
	}

	protected void addPoliciesToDefinitionElement(Iterator iterator,
			OMElement definitionElement) throws Exception {
		Policy policy;
		OMElement policyElement;
		OMNode firstChild;

		for (; iterator.hasNext();) {
			policy = (Policy) iterator.next();
			policyElement = PolicyUtil.getPolicyComponentAsOMElement(policy,
					serializer);
			firstChild = definition.getFirstOMChild();
			if (firstChild != null) {
				firstChild.insertSiblingBefore(policyElement);
			} else {
				definitionElement.addChild(policyElement);
			}
		}
	}

	private void addPolicyToDefinitionElement(String key, Policy policy) {
		policiesInDefinitions.put(key, policy);
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getUse() {
		return use;
	}

	public void setUse(String use) {
		this.use = use;
	}

	private void generateSoap11Port(OMFactory fac, OMElement definition,
			OMElement service) throws Exception {
		Iterator iterator = axisService.getEndpoints().values().iterator();
		AxisEndpoint axisEndpoint;
		AxisBinding axisBinding;
		for (; iterator.hasNext();) {
			axisEndpoint = (AxisEndpoint) iterator.next();
			/*
			 * Some transports might not be active at runtime.
			 */
			if (checkIfEndPointActive && !axisEndpoint.isActive()) {
				continue;
			}
			axisBinding = axisEndpoint.getBinding();
			String type = axisBinding.getType();
			if (Java2WSDLConstants.TRANSPORT_URI.equals(type)
					|| WSDL2Constants.URI_WSDL2_SOAP.equals(type)) {
				String version = (String) axisBinding
						.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
				if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(version)) {
					OMElement port = fac.createOMElement(PORT, wsdl);					
					port.addAttribute(ATTRIBUTE_NAME, axisEndpoint.getName(),
							null);
					QName qname = axisBinding.getName();
					port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
							+ qname.getLocalPart(), null);
					String endpointURL = getEndpointURL(axisEndpoint);
					WSDLSerializationUtil.addExtensionElement(fac, port,
							SOAP_ADDRESS, LOCATION, (endpointURL == null) ? ""
									: endpointURL, soap);
					generateEPRElement(axisEndpoint, fac, port, endpointURL);
					addPolicyAsExtElement(axisEndpoint, port);
					service.addChild(modifyPort(port));
					if (isAlreadyAdded(axisBinding, definition)) {
                        return;
                    }
					OMElement binding = generateSoap11Binding(fac, axisEndpoint
							.getBinding());
					OMElement serviceElement = definition.getFirstChildWithName(new QName(
			                wsdl.getNamespaceURI(), SERVICE_LOCAL_NAME));					
			        serviceElement.insertSiblingBefore(modifyBinding(binding));
					
				}
			}
		}
	}

	private void generateSoap12Port(OMFactory fac, OMElement definition,
			OMElement service) throws Exception {

		// /////////////////// FIXME //////////////////////////////////////////
		Iterator iterator = axisService.getEndpoints().values().iterator();
		AxisEndpoint axisEndpoint;
		AxisBinding axisBinding;
		for (; iterator.hasNext();) {
			axisEndpoint = (AxisEndpoint) iterator.next();
			/*
			 * 
			 */
			if (checkIfEndPointActive && !axisEndpoint.isActive()) {
				continue;
			}
			axisBinding = axisEndpoint.getBinding();
			String type = axisBinding.getType();
			if (Java2WSDLConstants.TRANSPORT_URI.equals(type)
					|| WSDL2Constants.URI_WSDL2_SOAP.equals(type)) {
				String version = (String) axisBinding
						.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
				if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(version)) {

					OMElement port = fac.createOMElement(PORT, wsdl);					
					port.addAttribute(ATTRIBUTE_NAME, axisEndpoint.getName(),
							null);
					QName qname = axisBinding.getName();
					port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
							+ qname.getLocalPart(), null);
					String endpointURL = getEndpointURL(axisEndpoint);
					WSDLSerializationUtil.addExtensionElement(fac, port,
							SOAP_ADDRESS, LOCATION, (endpointURL == null) ? ""
									: endpointURL, soap12);
					generateEPRElement(axisEndpoint, fac, port, endpointURL);
					addPolicyAsExtElement(axisEndpoint, port);
					service.addChild(modifyPort(port));
					if (isAlreadyAdded(axisBinding, definition)) {
			            return;
			        }
					OMElement binding = generateSoap12Binding(fac, definition, axisEndpoint
							.getBinding());
					service.insertSiblingBefore(modifyBinding(binding));
				}
			}
		}
	}

	private void generateHttpPort(OMFactory fac, OMElement definition,
			OMElement service) throws Exception {

		Iterator iterator = axisService.getEndpoints().values().iterator();
		AxisEndpoint axisEndpoint;
		AxisBinding axisBinding;
		for (; iterator.hasNext();) {
			axisEndpoint = (AxisEndpoint) iterator.next();
			/*
			 * 
			 */
			if (checkIfEndPointActive && !axisEndpoint.isActive()) {
				continue;
			}
			axisBinding = axisEndpoint.getBinding();
			String type = axisBinding.getType();
			if (WSDL2Constants.URI_WSDL2_HTTP.equals(type)) {
				OMElement port = fac.createOMElement(PORT, wsdl);				
				port.addAttribute(ATTRIBUTE_NAME, axisEndpoint.getName(), null);
				QName qname = axisBinding.getName();
				port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
						+ qname.getLocalPart(), null);
				OMElement extElement = fac.createOMElement("address", http);
				String endpointURL = getEndpointURL(axisEndpoint);
				extElement.addAttribute("location", (endpointURL == null) ? ""
						: endpointURL, null);
				port.addChild(extElement);

				addPolicyAsExtElement(axisEndpoint, port);
				service.addChild(modifyPort(port));				
				if (isAlreadyAdded(axisBinding, definition)) {
		            return;
		        }
				OMElement binding = generateHttpBinding(fac, definition, axisEndpoint.getBinding());
				service.insertSiblingBefore(modifyBinding(binding));
				
			}
		}
	}

	protected OMElement generateSoap11Binding(OMFactory fac, AxisBinding axisBinding) throws Exception {
		
		OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);	
		QName qname = axisBinding.getName();
		binding.addAttribute(ATTRIBUTE_NAME, qname.getLocalPart(), null);

        String portTypeName = serviceName + PORT_TYPE_SUFFIX;

        Parameter param = this.axisService.getParameter(Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG);
        if (param != null){
            portTypeName = (String) param.getValue();
        }

		binding.addAttribute("type", tns.getPrefix() + ":" + portTypeName, null);

		// Adding ext elements
		addPolicyAsExtElement(axisBinding, binding);
		addExtensionElement(fac, binding, BINDING_LOCAL_NAME, TRANSPORT,
				TRANSPORT_URI, STYLE, style, soap);

		// /////////////////////////////////////////////////////////////////////
		// Add WS-Addressing UsingAddressing element if appropriate
		// SHOULD be on the binding element per the specification
		if (AddressingHelper
				.getAddressingRequirementParemeterValue(axisService).equals(
						AddressingConstants.ADDRESSING_OPTIONAL)) {
			WSDLSerializationUtil.addExtensionElement(fac, binding,
					AddressingConstants.USING_ADDRESSING,
					DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "false", wsaw);
		} else if (AddressingHelper.getAddressingRequirementParemeterValue(
				axisService).equals(AddressingConstants.ADDRESSING_REQUIRED)) {
			WSDLSerializationUtil.addExtensionElement(fac, binding,
					AddressingConstants.USING_ADDRESSING,
					DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true", wsaw);
		}
		// //////////////////////////////////////////////////////////////////////

		for (Iterator axisBindingOperations = axisBinding.getChildren(); axisBindingOperations
				.hasNext();) {
			AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisBindingOperations
					.next();
			AxisOperation axisOperation = axisBindingOperation
					.getAxisOperation();
			if (axisOperation.isControlOperation()
					|| axisOperation.getName() == null) {
				continue;
			}
			String opeartionName = axisOperation.getName().getLocalPart();
			OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
					wsdl);
			binding.addChild(operation);
			String soapAction = axisOperation.getSoapAction();
			if (soapAction == null) {
				soapAction = "";
			}
			addPolicyAsExtElement(axisBindingOperation, operation);
			addExtensionElement(fac, operation, OPERATION_LOCAL_NAME,
					SOAP_ACTION, soapAction, STYLE, style, soap);

			// addPolicyAsExtElement(PolicyInclude.BINDING_OPERATION_POLICY,
			// axisOperation.getPolicyInclude(), operation);

			String MEP = axisOperation.getMessageExchangePattern();

			if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisBindingMessage axisBindingInMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
				if (axisBindingInMessage != null) {
					AxisMessage inaxisMessage = axisBindingInMessage
							.getAxisMessage();

					if (inaxisMessage != null) {
						operation.addAttribute(ATTRIBUTE_NAME, opeartionName,
								null);
						OMElement input = fac.createOMElement(
								IN_PUT_LOCAL_NAME, wsdl);
						addPolicyAsExtElement(axisBindingInMessage, input);
						addExtensionElement(fac, input, SOAP_BODY, SOAP_USE,
								use, null, targetNamespace, soap);
						// addPolicyAsExtElement(PolicyInclude.BINDING_INPUT_POLICY,
						// inaxisMessage.getPolicyInclude(), input);
						operation.addChild(input);
						writeSoapHeaders(inaxisMessage, fac, input, soap12);
					}
				}
			}

			if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {

				AxisBindingMessage axisBindingOutMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				if (axisBindingOutMessage != null) {
					AxisMessage outAxisMessage = axisBindingOutMessage
							.getAxisMessage();
					if (outAxisMessage != null) {
						OMElement output = fac.createOMElement(
								OUT_PUT_LOCAL_NAME, wsdl);
						addPolicyAsExtElement(axisBindingOutMessage, output);
						addExtensionElement(fac, output, SOAP_BODY, SOAP_USE,
								use, null, targetNamespace, soap);
						// addPolicyAsExtElement(PolicyInclude.BINDING_OUTPUT_POLICY,
						// outAxisMessage.getPolicyInclude(), output);
						operation.addChild(output);
						writeSoapHeaders(outAxisMessage, fac, output, soap12);
					}
				}
			}

			// generate fault Messages
			ArrayList faultyMessages = this.extractWSDL11FaultMessages(axisBindingOperation);
			if (faultyMessages != null) {
                for (Object faultyMessage1 : faultyMessages) {
                    AxisBindingMessage bindingFaultyMessage = (AxisBindingMessage)faultyMessage1;
                    if (bindingFaultyMessage != null) {
                        AxisMessage faultyMessage = bindingFaultyMessage.getAxisMessage();

                        OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME, wsdl);

                        addPolicyAsExtElement(bindingFaultyMessage, fault);
                        addExtensionElement(fac, fault, FAULT_LOCAL_NAME,
                                            SOAP_USE, use, ATTRIBUTE_NAME,
                                            faultyMessage.getName(), soap);
                        fault.addAttribute(ATTRIBUTE_NAME, faultyMessage.getName(), null);
                        // add policies for fault messages
                        operation.addChild(fault);
                        writeSoapHeaders(faultyMessage, fac, fault, soap);
                    }
                }
			}
		}
        return binding;
	}

	private OMElement generateSoap12Binding(OMFactory fac, OMElement definitions,
			AxisBinding axisBinding) throws Exception {
		
		OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
		OMElement serviceElement = definitions.getFirstChildWithName(new QName(
				wsdl.getNamespaceURI(), SERVICE_LOCAL_NAME));		

		QName qname = axisBinding.getName();
		binding.addAttribute(ATTRIBUTE_NAME, qname.getLocalPart(), null);

        String portTypeName = serviceName + PORT_TYPE_SUFFIX;

        Parameter param = this.axisService.getParameter(Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG);
        if (param != null){
            portTypeName = (String) param.getValue();
        }

		binding.addAttribute("type", tns.getPrefix() + ":" + portTypeName, null);

		// Adding ext elements
		addPolicyAsExtElement(axisBinding, binding);
		addExtensionElement(fac, binding, BINDING_LOCAL_NAME, TRANSPORT,
				TRANSPORT_URI, STYLE, style, soap12);

		// /////////////////////////////////////////////////////////////////////
		// Add WS-Addressing UsingAddressing element if appropriate
		// SHOULD be on the binding element per the specification
		if (AddressingHelper
				.getAddressingRequirementParemeterValue(axisService).equals(
						AddressingConstants.ADDRESSING_OPTIONAL)) {
			WSDLSerializationUtil.addExtensionElement(fac, binding,
					AddressingConstants.USING_ADDRESSING,
					DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "false", wsaw);
		} else if (AddressingHelper.getAddressingRequirementParemeterValue(
				axisService).equals(AddressingConstants.ADDRESSING_REQUIRED)) {
			WSDLSerializationUtil.addExtensionElement(fac, binding,
					AddressingConstants.USING_ADDRESSING,
					DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true", wsaw);
		}
		// //////////////////////////////////////////////////////////////////////

		for (Iterator axisBindingOperations = axisBinding.getChildren(); axisBindingOperations
				.hasNext();) {
			AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisBindingOperations
					.next();
			AxisOperation axisOperation = axisBindingOperation
					.getAxisOperation();
			if (axisOperation.isControlOperation()
					|| axisOperation.getName() == null) {
				continue;
			}
			String opeartionName = axisOperation.getName().getLocalPart();
			OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
					wsdl);
			binding.addChild(operation);
			String soapAction = axisOperation.getSoapAction();
			if (soapAction == null) {
				soapAction = "";
			}
			addPolicyAsExtElement(axisBindingOperation, operation);
			addExtensionElement(fac, operation, OPERATION_LOCAL_NAME,
					SOAP_ACTION, soapAction, STYLE, style, soap12);

			// addPolicyAsExtElement(PolicyInclude.BINDING_OPERATION_POLICY,
			// axisOperation.getPolicyInclude(), operation);

			String MEP = axisOperation.getMessageExchangePattern();

			if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisBindingMessage axisBindingInMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
				if (axisBindingInMessage != null) {
					AxisMessage inaxisMessage = axisBindingInMessage
							.getAxisMessage();

					if (inaxisMessage != null) {
						operation.addAttribute(ATTRIBUTE_NAME, opeartionName,
								null);
						OMElement input = fac.createOMElement(
								IN_PUT_LOCAL_NAME, wsdl);
						addPolicyAsExtElement(axisBindingInMessage, input);
						addExtensionElement(fac, input, SOAP_BODY, SOAP_USE,
								use, null, targetNamespace, soap12);
						operation.addChild(input);
						writeSoapHeaders(inaxisMessage, fac, input, soap12);
					}
				}
			}

			if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {

				AxisBindingMessage axisBindingOutMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				if (axisBindingOutMessage != null) {
					AxisMessage outAxisMessage = axisBindingOutMessage
							.getAxisMessage();
					if (outAxisMessage != null) {
						OMElement output = fac.createOMElement(
								OUT_PUT_LOCAL_NAME, wsdl);
						addPolicyAsExtElement(axisBindingOutMessage, output);
						addExtensionElement(fac, output, SOAP_BODY, SOAP_USE,
								use, null, targetNamespace, soap12);
						// addPolicyAsExtElement(PolicyInclude.BINDING_OUTPUT_POLICY,
						// outAxisMessage.getPolicyInclude(), output);
						operation.addChild(output);
						writeSoapHeaders(outAxisMessage, fac, output, soap12);
					}
				}
			}

			// generate fault Messages
			ArrayList faultyMessages = this.extractWSDL11FaultMessages(axisBindingOperation);
			if (faultyMessages != null) {
                for (Object faultyMessage1 : faultyMessages) {
                    AxisBindingMessage bindingFaultyMessage = (AxisBindingMessage)faultyMessage1;
                    if (bindingFaultyMessage != null) {
                        AxisMessage faultyMessage = bindingFaultyMessage
                                .getAxisMessage();
                        OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME,
                                                              wsdl);
                        addPolicyAsExtElement(bindingFaultyMessage, fault);
                        addExtensionElement(fac, fault, FAULT_LOCAL_NAME,
                                            SOAP_USE, use, ATTRIBUTE_NAME, faultyMessage
                                .getName(), soap12);
                        fault.addAttribute(ATTRIBUTE_NAME, faultyMessage
                                .getName(), null);
                        // add policies for fault messages
                        operation.addChild(fault);
                        writeSoapHeaders(faultyMessage, fac, fault, soap12);
                    }
                }
			}
		}
        return binding;
	}

	private OMElement generateHttpBinding(OMFactory fac, OMElement definitions,
			AxisBinding axisBinding) throws Exception {
		
		OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
		OMElement serviceElement = definitions.getFirstChildWithName(new QName(
				wsdl.getNamespaceURI(), SERVICE_LOCAL_NAME));
		

		QName qname = axisBinding.getName();
		binding.addAttribute(ATTRIBUTE_NAME, qname.getLocalPart(), null);

        String portTypeName = serviceName + PORT_TYPE_SUFFIX;

        Parameter param = this.axisService.getParameter(Java2WSDLConstants.PORT_TYPE_NAME_OPTION_LONG);
        if (param != null){
            portTypeName = (String) param.getValue();
        }

		binding.addAttribute("type", tns.getPrefix() + ":" + portTypeName, null);

		OMElement httpBinding = fac.createOMElement("binding", http);
		binding.addChild(httpBinding);
		httpBinding.addAttribute("verb", "POST", null);

		for (Iterator axisBindingOperations = axisBinding.getChildren(); axisBindingOperations
				.hasNext();) {
			AxisBindingOperation axisBindingOperation = (AxisBindingOperation) axisBindingOperations
					.next();
			AxisOperation axisOperation = axisBindingOperation
					.getAxisOperation();
			if (axisOperation.isControlOperation()
					|| axisOperation.getName() == null) {
				continue;
			}
			String opeartionName = axisOperation.getName().getLocalPart();
			OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
					wsdl);
			binding.addChild(operation);

			OMElement httpOperation = fac.createOMElement("operation", http);
			operation.addChild(httpOperation);
            String location = (String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION);
            location = location.replace('{','(');
            location = location.replace('}',')');
            httpOperation.addAttribute("location", location, null);

			String MEP = axisOperation.getMessageExchangePattern();

			if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {
				AxisBindingMessage axisBindingInMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
				if (axisBindingInMessage != null) {
					AxisMessage inaxisMessage = axisBindingInMessage
							.getAxisMessage();

					if (inaxisMessage != null) {
						operation.addAttribute(ATTRIBUTE_NAME, opeartionName,
								null);
						OMElement input = fac.createOMElement(
								IN_PUT_LOCAL_NAME, wsdl);
						OMElement inputelement = fac.createOMElement("content",
								mime);
						input.addChild(inputelement);
                        String inputType=(String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION);
                        inputelement.addAttribute("type", (inputType!=null? inputType:"application/xml"), null);
						inputelement.addAttribute("part", inaxisMessage.getPartName(), null);
						operation.addChild(input);
					}
				}
			}

			if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
					|| WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
					|| WSDL2Constants.MEP_URI_IN_OUT.equals(MEP)) {

				AxisBindingMessage axisBindingOutMessage = (AxisBindingMessage) axisBindingOperation
						.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
				if (axisBindingOutMessage != null) {
					AxisMessage outAxisMessage = axisBindingOutMessage
							.getAxisMessage();
					if (outAxisMessage != null) {
						OMElement output = fac.createOMElement(
								OUT_PUT_LOCAL_NAME, wsdl);
						OMElement outElement = fac.createOMElement("content",
								mime);
                        String outputType=(String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION);
                        outElement.addAttribute("type", (outputType!=null? outputType:"application/xml"), null);
						outElement.addAttribute("part", outAxisMessage.getPartName(), null);
						output.addChild(outElement);
						operation.addChild(output);
					}
				}
			}

            // generate fault Messages
            ArrayList faultyMessages = axisBindingOperation.getFaults();
            if (faultyMessages != null) {
                for (Object faultyMessage1 : faultyMessages) {
                    AxisBindingMessage bindingFaultyMessage = (AxisBindingMessage) faultyMessage1;
                    if (bindingFaultyMessage != null) {
                        AxisMessage faultyMessage = bindingFaultyMessage
                                .getAxisMessage();
                        OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME,
                                wsdl);
                        fault.addAttribute(ATTRIBUTE_NAME, faultyMessage
                                .getName(), null);
                        // add policies for fault messages
                        operation.addChild(fault);
                    }
                }
            }
		}
        return binding;
	}

	private void addPolicyAsExtElement(AxisDescription axisDescription,
			OMElement wsdlElement) throws Exception {
		PolicySubject policySubject = axisDescription.getPolicySubject();
		Collection attachPolicyComponents = policySubject
				.getAttachedPolicyComponents();

        for (Object policyElement : attachPolicyComponents) {
            if (policyElement instanceof Policy) {
                PolicyReference policyReference =
                        PolicyUtil.createPolicyReference((Policy)policyElement);
                OMElement policyRefElement =
                        PolicyUtil.getPolicyComponentAsOMElement(policyReference, serializer);

                OMNode firstChildElem = wsdlElement.getFirstElement();
                if (firstChildElem == null) {
                    wsdlElement.addChild(policyRefElement);
                } else {
                    firstChildElem.insertSiblingBefore(policyRefElement);
                }
                String key = policyReference.getURI();
                if (key.startsWith("#")) {
                    key = key.substring(key.indexOf("#") + 1);
                }
                addPolicyToDefinitionElement(key, (Policy)policyElement);

            } else if (policyElement instanceof PolicyReference) {
                OMElement child =
                        PolicyUtil.getPolicyComponentAsOMElement((PolicyComponent)policyElement,
                                                                 serializer);
                OMElement firstChildElem = wsdlElement.getFirstElement();

                if (firstChildElem == null) {
                    wsdlElement.addChild(child);
                } else {
                    firstChildElem.insertSiblingBefore(child);
                }

                String key = ((PolicyReference)policyElement).getURI();
                if (key.startsWith("#")) {
                    key = key.substring(key.indexOf("#") + 1);
                }

                PolicyLocator locator = new PolicyLocator(axisService);
                Policy p = locator.lookup(key);

                if (p != null) {
                    addPolicyToDefinitionElement(key, p);

                }
            }
        }
	}

	private void addPolicyAsExtAttribute(AxisDescription axisDescription,
			OMElement element, OMFactory factory) throws Exception {

		PolicySubject policySubject = axisDescription.getPolicySubject();
		ArrayList policyURIs = new ArrayList();

        for (Object policyElement : policySubject.getAttachedPolicyComponents()) {
            String key;

            if (policyElement instanceof Policy) {
                Policy p = (Policy)policyElement;

                if (p.getId() != null) {
                    key = "#" + p.getId();
                } else if (p.getName() != null) {
                    key = p.getName();
                } else {
                    throw new RuntimeException(
                            "Can't add the Policy as an extensibility attribute since it doesn't have a id or a name attribute");
                }

                policyURIs.add(key);
                addPolicyToDefinitionElement(key, p);
            } else {
                String uri = ((PolicyReference)policyElement).getURI();
                PolicyLocator locator = new PolicyLocator(axisService);
                if (uri.startsWith("#")) {
                    key = uri.substring(uri.indexOf('#') + 1);
                } else {
                    key = uri;
                }

                Policy p = locator.lookup(key);

                if (p == null) {
                    throw new RuntimeException("Cannot resolve " + uri
                                               + " to a Policy");
                }
                policyURIs.add(uri);
                addPolicyToDefinitionElement(key, p);
            }
        }

		if (!policyURIs.isEmpty()) {
			String value = null;

			/*
			 * We need to create a String that is like 'uri1 uri2 .." to set as
			 * the value of the wsp:PolicyURIs attribute.
			 */
            for (Object policyURI : policyURIs) {
                String uri = (String)policyURI;
                value = (value == null) ? uri : value + " " + uri;
            }

			OMNamespace ns = factory.createOMNamespace(
					org.apache.neethi.Constants.URI_POLICY_NS,
					org.apache.neethi.Constants.ATTR_WSP);
			OMAttribute URIs = factory.createOMAttribute("PolicyURIs", ns,
					value);
			element.addAttribute(URIs);
		}
	}

	private boolean isAlreadyAdded(AxisBinding axisBinding,
			OMElement definitionElement) {
		QName bindingName = axisBinding.getName();
		QName name = new QName("name");
		for (Iterator iterator = definitionElement
				.getChildrenWithName(new QName(wsdl.getNamespaceURI(),
						BINDING_LOCAL_NAME)); iterator.hasNext();) {
			OMElement element = (OMElement) iterator.next();
			String value = element.getAttributeValue(name);
			if (bindingName.getLocalPart().equals(value)) {
				return true;
			}
		}
		return false;
	}
	
	private String getEndpointURL(AxisEndpoint axisEndpoint) {
		Parameter modifyAddressParam = axisService.getParameter("modifyUserWSDLPortAddress");
            String endpointURL = axisEndpoint.getEndpointURL();
            if (modifyAddressParam != null &&
                    !Boolean.parseBoolean((String)modifyAddressParam.getValue())) {
                return endpointURL;
            }

            String hostIP;
    
            // First check the hostname parameter 
            hostIP = Utils.getHostname(axisService.getAxisConfiguration());
        
            //If it is not set extract the hostIP from the URL
            if (hostIP == null) {
                hostIP = WSDLSerializationUtil.extractHostIP(axisService.getEndpointURL());
            }
        
            //TODO This is to prevent problems when JAVA2WSDL tool is used where there is no
            //Axis server running. calculateEndpointURL fails in this scenario, refer to 
            // SimpleHTTPServer#getEPRsForService()
  
            if (hostIP != null) {
                return axisEndpoint.calculateEndpointURL(hostIP);
            } else {
                return endpointURL;
            }
	}
	
    /**
     * Generate a &lt;wsid:Identity&gt; element according to the <a
     * href="http://www.oasis-open.org/committees/download.php/29516/ws-addressingandidentity.doc"
     * >WS-AddressingAndIdentity specification</a> and add it as a child of the given
     * <code>epr</code> &lt;wsa:EndpointReference&gt; element.
     * <p>
     * If none of the <code>identityParameter</code> and <code>x509CertIdentityParameter</code>
     * configures a valid value, this method will skip creating and adding an identity element.
     * </p>
     * 
     * @param fac
     *            A factory to use for creating OMElements.
     * @param epr
     *            The endpoint reference element to add the generated identity element to. Must not
     *            be <code>null</code>.
     * @param identity
     *            An optional &lt;wsid:Identity&gt; OMElement to clone and use, instead of
     *            generating a new one.
     * @param x509CertIdentityParameter
     *            An optional parameter that may contain a &lt;ds:X509Certificate&gt; String literal
     *            value to set as key info in the created identity element.
     */
    private void generateIdentityElement(OMFactory fac, OMElement epr, OMElement identity, Parameter x509CertIdentityParameter) {
        if (identity != null) {
            identity = identity.cloneOMElement();
            epr.addChild(identity);
        }
            
        if (x509CertIdentityParameter != null && x509CertIdentityParameter.getValue() != null) {
            if (identity == null) {
                identity = fac.createOMElement(AddressingConstants.QNAME_IDENTITY);
                epr.addChild(identity);
            }
            
            OMElement keyInfo = identity.getFirstChildWithName(AddressingConstants.QNAME_IDENTITY_KEY_INFO);
            if (keyInfo == null) {
                keyInfo = fac.createOMElement(AddressingConstants.QNAME_IDENTITY_KEY_INFO);
                identity.addChild(keyInfo);
            }
            
            OMElement x509Data = keyInfo.getFirstChildWithName(AddressingConstants.QNAME_IDENTITY_X509_DATA);
            if (x509Data == null) {
                x509Data = fac.createOMElement(AddressingConstants.QNAME_IDENTITY_X509_DATA);
                keyInfo.addChild(x509Data);
            }
            
            OMElement x509cert = x509Data.getFirstChildWithName(AddressingConstants.QNAME_IDENTITY_X509_CERT);
            if (x509cert == null) {
                x509cert = fac.createOMElement(AddressingConstants.QNAME_IDENTITY_X509_CERT);
                x509Data.addChild(x509cert);
            }
            
            String x509CertValue = (String) x509CertIdentityParameter.getValue();
            x509cert.setText(x509CertValue);
        }
	}
	
	
	/*
	 * Generate the EndpointReference element
	 * <wsa:EndpointReference>
         *    <wsa:Address>
         *        http://some.service.epr/
         *     </wsa:Address>
         * </wsa:EndpointReference>
	 * 
	 */
	private void generateEPRElement(AxisEndpoint endpoint, OMFactory fac, OMElement port, String endpointURL){
	    //an optional String parameter that contains x509 certificate information 
	    Parameter x509CertIdentityParameter = axisService.getParameter(AddressingConstants.IDENTITY_PARAMETER);
	    
	    //an optional OMElement parameter that represents an <wsid:Identity> element
	    OMElement identityElement = AddressingHelper.getAddressingIdentityParameterValue(endpoint);

	    if ((x509CertIdentityParameter == null || x509CertIdentityParameter.getValue() == null) && identityElement == null) {
	        //none of these is configured, for backward compatibility do not generate anything and return
	        return;
	    }
	    
	    OMElement wsaEpr = fac.createOMElement(AddressingConstants.Final.WSA_ENDPOINT_REFERENCE);
	    
	    OMElement address = fac.createOMElement(AddressingConstants.Final.WSA_ADDRESS);
	    address.setText((endpointURL == null) ? "": endpointURL);
	    
	    wsaEpr.addChild(address);
	    
	    // This will generate the identity element if the service parameter is set
	    generateIdentityElement(fac, wsaEpr, identityElement, x509CertIdentityParameter);
	    
	    port.addChild(wsaEpr);   
	    
	}
	
	protected OMElement generateDocumentation(OMFactory fac){       
        return WSDLSerializationUtil.generateDocumentationElement(axisService, fac, wsdl);
    }
    
    protected OMElement generateTypes(OMFactory fac){
        
        OMElement wsdlTypes = fac.createOMElement("types", wsdl);      

        // populate the schema mappings
        axisService.populateSchemaMappings();

        ArrayList schemas = axisService.getSchema();
        for (int i = 0; i < schemas.size(); i++) {
            StringWriter writer = new StringWriter();

            // XmlSchema schema = (XmlSchema) schemas.get(i);
            XmlSchema schema = axisService.getSchema(i);

            String targetNamespace = schema.getTargetNamespace();
            if (!Constants.NS_URI_XML.equals(targetNamespace)) {
                schema.write(writer);
                String schemaString = writer.toString();
                if (!"".equals(schemaString)) {
                    try {
                        wsdlTypes.addChild(XMLUtils.toOM(new StringReader(schemaString)));
                    } catch (XMLStreamException e) {                       
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }
        return wsdlTypes;
    }
    
    
    protected OMElement generateDefinition(OMFactory fac){
        
        wsdl = fac.createOMNamespace(WSDL_NAMESPACE, DEFAULT_WSDL_NAMESPACE_PREFIX);
        OMElement ele = fac.createOMElement("definitions", wsdl);
        setDefinitionElement(ele);

        policiesInDefinitions = new HashMap<String, Policy>();

        Map namespaceMap = axisService.getNamespaceMap();
        if (namespaceMap == null)
            namespaceMap = new HashMap();

        WSDLSerializationUtil.populateNamespaces(ele, namespaceMap);
        soap = ele.declareNamespace(URI_WSDL11_SOAP, SOAP11_PREFIX);
        soap12 = ele.declareNamespace(URI_WSDL12_SOAP, SOAP12_PREFIX);
        http = ele.declareNamespace(HTTP_NAMESPACE, HTTP_PREFIX);
        mime = ele.declareNamespace(MIME_NAMESPACE, MIME_PREFIX);
        wsaw = ele.declareNamespace(AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
        String prefix = WSDLSerializationUtil.getPrefix(axisService.getTargetNamespace(),
                                                        namespaceMap);
        if (prefix == null || "".equals(prefix)) {
            if (axisService.getTargetNamespacePrefix() != null) {
                prefix = axisService.getTargetNamespacePrefix();
            } else {
                prefix = DEFAULT_TARGET_NAMESPACE_PREFIX;
            }
        }

        namespaceMap.put(prefix, axisService.getTargetNamespace());
        tns = ele.declareNamespace(axisService.getTargetNamespace(), prefix);
        
        definition.addAttribute("targetNamespace", axisService.getTargetNamespace(), null);
        
        return ele; 
    }
    
    protected boolean isDisableREST() {
        // axis2.xml indicated no HTTP binding?
        boolean disableREST = false;
        Parameter disableRESTParameter = axisService
                .getParameter(org.apache.axis2.Constants.Configuration.DISABLE_REST);
        if (disableRESTParameter != null
                && JavaUtils.isTrueExplicitly(disableRESTParameter.getValue())) {
            disableREST = true;
        }
        return disableREST;
    }

    protected boolean isDisableSOAP11() {
        boolean disableSOAP11 = false;
        Parameter disableSOAP11Parameter = axisService
                .getParameter(org.apache.axis2.Constants.Configuration.DISABLE_SOAP11);
        if (disableSOAP11Parameter != null
                && JavaUtils.isTrueExplicitly(disableSOAP11Parameter.getValue())) {
            disableSOAP11 = true;
        }
        return disableSOAP11;
    }

    protected boolean isDisableSOAP12() {
        // axis2.xml indicated no SOAP 1.2 binding?
        boolean disableSOAP12 = false;
        Parameter disableSOAP12Parameter = axisService
                .getParameter(org.apache.axis2.Constants.Configuration.DISABLE_SOAP12);
        if (disableSOAP12Parameter != null
                && JavaUtils.isTrueExplicitly(disableSOAP12Parameter.getValue())) {
            disableSOAP12 = true;
        }
        return disableSOAP12;
    }
    
    protected Map<String, Policy> getPoliciesInDefinitions() {
        return policiesInDefinitions;        
    }    
    protected OMElement modifyPort(OMElement port){
        return port;
    }
    protected OMElement modifyBinding(OMElement binding){
        return binding;
    }
}
