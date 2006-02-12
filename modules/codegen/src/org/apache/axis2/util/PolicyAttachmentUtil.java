/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis2.util;

import com.ibm.wsdl.util.xml.DOM2Writer;
import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.ws.policy.Assertion;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyConstants;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyReader;
import org.apache.ws.policy.util.PolicyRegistry;
import org.apache.ws.policy.util.SchemaRegistry;
import org.apache.wsdl.Component;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityAttribute;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.PolicyExtensibilityElement;
import org.apache.wsdl.extensions.Schema;
import org.w3c.dom.Element;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This util class which implements WSPolicyAttachment sepcification (September
 * 2004).
 * 
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *  
 */
public class PolicyAttachmentUtil {

    private WSDLDescription wsdlDescription = null;

    //private HashMap loadedPolicies = new HashMap();
    private PolicyRegistry reg = new PolicyRegistry();

    private SchemaRegistry schemaRegistry = new SchemaRegistry();

    public PolicyAttachmentUtil() {
    }

    public PolicyAttachmentUtil(WSDLDescription wsdlDescription) {
        this.wsdlDescription = wsdlDescription;

        populatePolicyRegistry();
        populateSchemaRegistry();

    }

    public PolicyAttachmentUtil(InputStream wsdlInputStream) {
        try {
            WSDLVersionWrapper build = WOMBuilderFactory.getBuilder(
                    WSDLConstants.WSDL_1_1).build(wsdlInputStream);
            wsdlDescription = build.getDescription();

            populatePolicyRegistry();
            populateSchemaRegistry();

        } catch (WSDLException e) {
            throw new IllegalArgumentException("error : " + e.getMessage());
        }
    }

    public void setWSDLDescription(WSDLDescription wsdlDescription) {
        this.wsdlDescription = wsdlDescription;
        reg = new PolicyRegistry();

        populatePolicyRegistry();
        populateSchemaRegistry();
    }

    public WSDLDescription getWSDLDescription() {
        try {
            return getDescription();

        } catch (IllegalStateException ex) {
            return null;
        }
    }

    private WSDLDescription getDescription() {
        if (wsdlDescription != null) {
            return wsdlDescription;
        }
        throw new IllegalStateException("ERROR: A WSDLDescription is not set");
    }

    /**
     * Retruns the Effective policy for a service.
     * 
     * @param serviceName
     * @return
     */
    public Policy getPolicyForService(QName serviceName) {
        return getServicePolicy(serviceName);
    }

    /**
     * Returns the Effective policy for an Endpoint.
     * 
     * @param epName
     * @return
     */
    public Policy getPolicyForEndPoint(QName epName) {
        Policy servicePolicy = null;
        Policy endPointPolicy = null;

        Iterator iterator = wsdlDescription.getServices().values().iterator();

        while (iterator.hasNext()) {
            WSDLService service = (WSDLService) iterator.next();
            if (service.getEndpoints().containsKey(epName)) {
                servicePolicy = getPolicyForService(service.getName());
                break;
            }
        }

        endPointPolicy = getEndPointPolicy(epName);
        
        if (servicePolicy != null) {
        	if (endPointPolicy != null) {
        		return (Policy) servicePolicy.merge(endPointPolicy);
        	}
        	return servicePolicy;        	
        } 
        return endPointPolicy;
    }

    /**
     * Returns the Effective policy for an Operation.
     * 
     * @param endPoint
     * @param operation
     * @return
     */
    public Policy getPolicyForOperation(QName endPoint, QName operation) {
        Policy endPointPolicy = getPolicyForEndPoint(endPoint);
        Policy operationPolicy = getOperationPolicy(endPoint, operation);

        //
        if (operationPolicy == null) {
            return endPointPolicy;

        } else if (endPointPolicy == null) {
            return operationPolicy;

        } else {
            return (Policy) endPointPolicy.merge(operationPolicy);
        }
    }

    /**
     * Returns the effective policy of an Input Message.
     * 
     * @param endPoint
     * @param operation
     * @return
     */
    public Policy getPolicyForInputMessage(QName endPoint, QName operation) {
        Policy operationPolicy = getPolicyForOperation(endPoint, operation);
        Policy inputMsgPolicy = getInputMeassagePolicy(endPoint, operation);

        if (operationPolicy == null) {
            return inputMsgPolicy;

        } else if (inputMsgPolicy == null) {
            return operationPolicy;

        } else {
            return (Policy) operationPolicy.merge(inputMsgPolicy);
        }
    }

    /**
     * Returns the effective policy of an Output Message.
     * 
     * @param endPoint
     * @param operation
     * @return
     */
    public Policy getPolicyForOutputMessage(QName endPoint, QName operation) {
        Policy operationPolicy = getPolicyForOperation(endPoint, operation);
        Policy outputMsgPolicy = getOutputMeassagePolicy(endPoint, operation);

        if (operationPolicy == null) {
            return outputMsgPolicy;

        } else if (outputMsgPolicy == null) {
            return operationPolicy;

        } else {
            return (Policy) operationPolicy.merge(outputMsgPolicy);
        }
    }

    /**
     * 
     * @param qname
     * @return
     */
    public Policy getServicePolicy(QName qname) throws IllegalArgumentException {

        WSDLService service = getDescription().getService(qname);

        if (service == null) {
            throw new IllegalArgumentException("no such service:" + qname);
        }

        Policy policy = getPolicyFromComponent(service);
        return (policy == null) ? null : (Policy) policy.normalize();
    }

    public Policy getEndPointPolicy(QName qname) {
        WSDLEndpoint endpoint = getEndpoint(qname);
        if (endpoint == null) {
            throw new IllegalArgumentException("no such portType:" + qname);
        }

        ArrayList policies = new ArrayList();

        // wsdl:port
        Assertion epPolicy = getPolicyFromComponent(endpoint);
        if (epPolicy != null) {
            policies.add(getPolicyFromComponent(endpoint));
        }

        //wsdl:binding
        WSDLBinding wsdlBinding = endpoint.getBinding();
        Assertion wsdlBindingPolicy = getPolicyFromComponent(wsdlBinding);
        if (wsdlBindingPolicy != null) {
            policies.add(getPolicyFromComponent(wsdlBinding));
        }

        //wsdl:portType
        WSDLInterface wsdlInterface = wsdlBinding.getBoundInterface();
        Assertion portTypePolicy = getPolicyFromComponent(wsdlInterface);
        if (portTypePolicy != null) {
            policies.add(getPolicyFromComponent(wsdlInterface));
        }

        return getSinglePolicy(policies);
    }

    public Policy getOperationPolicy(QName eqame, QName oqname) {
        WSDLEndpoint endPoint = getEndpoint(eqame);
        ArrayList list = new ArrayList();

        //wsdl:binding/wsdl:operation
        WSDLBinding binding = endPoint.getBinding();
        WSDLBindingOperation bindingOperation = (WSDLBindingOperation) binding
                .getBindingOperations().get(oqname);

        Assertion bindingPolicy = getPolicyFromComponent(bindingOperation);
        if (bindingPolicy != null) {
            list.add(bindingPolicy);
        }

        // wsdl:portType/wsdl:operation
        WSDLOperation wsdlOperation = bindingOperation.getOperation();
        Assertion interfacePolicy = getPolicyFromComponent(wsdlOperation);

        if (interfacePolicy != null) {
            list.add(interfacePolicy);

        }
        return getSinglePolicy(list);
    }

    public Policy getInputMeassagePolicy(QName eqname, QName oqname) {
        List policies = new ArrayList();
        WSDLEndpoint endPoint = getEndpoint(eqname);

        // wsdl:binding/wsdl:operation/wsdl:input
        WSDLBindingOperation wsdlBindingOperation = endPoint.getBinding()
                .getBindingOperation(oqname);
        WSDLBindingMessageReference bindingInput = wsdlBindingOperation
                .getInput();

        //List extensibilityAttributes =
        // bindingInput.getExtensibilityAttributes();
        Policy bindingInputPolicy = getSinglePolicy(getPoliciesAsExtensibleElements(bindingInput));
        if (bindingInputPolicy != null) {
            policies.add(bindingInputPolicy);
        }

        // wsdl:portType/wsdl:operation/wsdl:input
        WSDLOperation wsdlOperation = wsdlBindingOperation.getOperation();
        MessageReference operationInput = wsdlOperation.getInputMessage();
        Policy operationInputPolicy = getSinglePolicy(getPoliciesAsExtensibilityAttribute(operationInput));
        if (operationInputPolicy != null) {
            policies.add(operationInputPolicy);
        }

        // wsdl:Message
        // TODO

        return getSinglePolicy(policies);
    }

    public Policy getOutputMeassagePolicy(QName endPointName, QName opName) {
        List policies = new ArrayList();
        WSDLEndpoint endPoint = getEndpoint(endPointName);

        // wsdl:binding/wsdl:operation/wsdl:output
        WSDLBindingOperation wsdlBindingOperation = endPoint.getBinding()
                .getBindingOperation(opName);
        WSDLBindingMessageReference bindingOutput = wsdlBindingOperation
                .getOutput();
        Policy bindingOutputPolicy = getSinglePolicy(getPoliciesAsExtensibleElements(bindingOutput));
        if (bindingOutputPolicy != null) {
            policies.add(getPolicyFromComponent(bindingOutput));
        }

        // wsdl:portType/wsdl:operation/wsdl:output
        WSDLOperation wsdlOperation = wsdlBindingOperation.getOperation();
        MessageReference operationOutput = wsdlOperation.getOutputMessage();
        Policy operationOutputPolicy = getSinglePolicy(getPoliciesAsExtensibilityAttribute(operationOutput));
        if (operationOutputPolicy != null) {
            policies.add(operationOutputPolicy);
        }

        // wsdl:Message
        // TODO

        return getSinglePolicy(policies);
    }

    public Policy getFaultMeassagePolicy(QName endPointName, QName opName,
            QName fault) {
        throw new UnsupportedOperationException();
    }

    public PolicyRegistry getPolicyRegistry() {
        return reg;
    }

    public Element getSchemaElement(String uri) {
        return schemaRegistry.lookup(uri);
    }

    private Policy getSinglePolicy(List policies) {
        Policy result = null;

        if (!policies.isEmpty()) {
            Iterator iter = policies.iterator();
            result = (Policy) iter.next();
            while (iter.hasNext()) {
                Policy next = (Policy) iter.next();
                result = (Policy) result.merge(next, reg);
            }
        }
        return result;
    }

    private WSDLEndpoint getEndpoint(QName epName) {
        Iterator iterator = wsdlDescription.getServices().values().iterator();
        while (iterator.hasNext()) {
            WSDLService service = (WSDLService) iterator.next();
            if (service.getEndpoints().containsKey(epName)) {
                return service.getEndpoint(epName);
            }
        }
        return null;
    }

    private Policy getPolicyFromComponent(Component component) {
        List list = new ArrayList();
        List policiesAsAttributes = getPoliciesAsExtensibilityAttribute(component),
        policiesAsElements = getPoliciesAsExtensibleElements(component);

        list.addAll(policiesAsAttributes);
        list.addAll(policiesAsElements);

        return getSinglePolicy(list);
    }

    private List getPoliciesAsExtensibilityAttribute(Component component) {
        Iterator iterator;
        List policyURIStrings = new ArrayList();
        List policies = new ArrayList();
        iterator = component.getExtensibilityAttributes().iterator();

        while (iterator.hasNext()) {
        	WSDLExtensibilityAttribute exAttribute = (WSDLExtensibilityAttribute) iterator
                    .next();
            QName qname = exAttribute.getKey();

            if (qname.getNamespaceURI().equals(
                    PolicyConstants.WS_POLICY_NAMESPACE_URI)
                    && qname.getLocalPart().equals("PolicyURIs")) {
                String value = exAttribute.getValue().toString();
                String[] uriStrings = value.split(" ");

                for (int i = 0; i < uriStrings.length; i++) {
                    policyURIStrings.add(uriStrings[i].trim());
                }
            }
        }

        if (!policyURIStrings.isEmpty()) {
            iterator = policyURIStrings.iterator();

            do {
                String policyURIString = (String) iterator.next();
                Policy policy = getPolicyFromURI(policyURIString);
                policies.add(policy);
            } while (iterator.hasNext());
        }
        return policies;
    }

    private List getPoliciesAsExtensibleElements(Component component) {
        ArrayList policies = new ArrayList();
        Iterator iterator = component.getExtensibilityElements().iterator();

        while (iterator.hasNext()) {
            Object extensibilityElement = iterator.next();

            if (extensibilityElement instanceof PolicyExtensibilityElement) {
            	PolicyExtensibilityElement policyExtensibilityElement = (PolicyExtensibilityElement) extensibilityElement;
            	Object policyElement = policyExtensibilityElement.getPolicyElement();
            	
            	if (policyElement instanceof Policy) {
            		policies.add(policyElement);
            		
            	} else if (policyElement instanceof PolicyReference) {
            		policies.add(getPolicyFromURI(((PolicyReference) policyElement).getPolicyURIString()));
            		
            	}
//            	
//            	
//                if (element.getNamespaceURI().equals(
//                        PolicyConstants.WS_POLICY_NAMESPACE_URI)
//                        && element.getLocalName().equals("PolicyReference")) {
//                    policies.add(getPolicyFromPolicyRef(element));
//
//                } else if (element.getNamespaceURI().equals(
//                        PolicyConstants.WS_POLICY_NAMESPACE_URI)
//                        && element.getLocalName().equals("Policy")) {
//                    policies.add(getPolicyFromElement(element));
//                }
            }
            //          WSDLExtensibilityElement exElement = (WSDLExtensibilityElement)
            // iterator.next();
            //          Element element = (Element) exElement.getElement();
            //          if
            // (element.getNamespaceURI().equals(WSPConstants.WS_POLICY_NAMESPACE_URI)
            // && element.getLocalName().equals("PolicyReference")) {
            //              policyList.add(getPolicyAsPolicyRef(element));
            //              
            //          } else if
            // (element.getNamespaceURI().equals(WSPConstants.WS_POLICY_NAMESPACE_URI)
            // && element.getLocalName().equals("Policy")) {
            //              policyList.add(getPolicyAsElement(element));
            //          }

        }
        
        return policies;
    }

    private Policy getPolicyFromPolicyRef(Element element) {
        String policyURIString = element.getAttribute("URI");
        if (policyURIString != null && policyURIString.length() != 0) {
            return getPolicyFromURI(policyURIString);
        }
        return null;
    }

    private Policy getPolicyFromElement(Element element) {
        InputStream policyInputStream = createInputStream(element);
        PolicyReader reader = PolicyFactory
                .getPolicyReader(PolicyFactory.OM_POLICY_READER);
        return reader.readPolicy(policyInputStream);
    }

    private InputStream createInputStream(Element element) {
        // some improvements ..???
        StringWriter sw = new StringWriter();
        DOM2Writer.serializeAsXML(element, sw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private Policy getPolicyFromURI(String policyURIString) {
        return reg.lookup(policyURIString);
    }

    public String getTargetURI() {
        return getDescription().getTargetNameSpace();
    }

    private void populateSchemaRegistry() {
        WSDLDescription des = getDescription();
        WSDLTypes types = des.getTypes();

        Iterator iterator = types.getExtensibilityElements().iterator();
        while (iterator.hasNext()) {
            Object extElement = iterator.next();
            if (extElement instanceof Schema) {
                Element schemaElement = ((Schema) extElement).getElement();
                schemaRegistry.register(schemaElement
                        .getAttribute("targetNamespace"), schemaElement);
            }
        }
    }

    private void populatePolicyRegistry() {
        Iterator iterator;
        WSDLDescription des = getDescription();
        List extElements = des.getExtensibilityElements();
        registerPoliciesAsElements(extElements);

        iterator = des.getWsdlInterfaces().values().iterator();
        while (iterator.hasNext()) {
            WSDLInterface interfaze = (WSDLInterface) iterator.next();
            registerPoliciesInWSDLInterface(interfaze);
        }

        iterator = des.getBindings().values().iterator();
        while (iterator.hasNext()) {
            WSDLBinding wsdlBinding = (WSDLBinding) iterator.next();
            registerPoliciesInWSDLBinding(wsdlBinding);
        }

        iterator = des.getServices().values().iterator();
        while (iterator.hasNext()) {
            WSDLService service = (WSDLService) iterator.next();
            registerPoliciesInService(service);
        }

        iterator = reg.keys();
        while (iterator.hasNext()) {
            String uriString = (String) iterator.next();
            Policy policy = reg.lookup(uriString);
            if (policy == null) {
                try {
                    URI policyURI = new URI(uriString);
                    URL policyURL = policyURI.toURL();
                    PolicyReader reader = PolicyFactory
                            .getPolicyReader(PolicyFactory.OM_POLICY_READER);
                    Policy newPolicy = reader
                            .readPolicy(policyURL.openStream());
                    reg.register(uriString, newPolicy);

                } catch (Exception e) {
                    e.printStackTrace();
                    reg.unregister(uriString);
                    iterator = reg.keys();
                }
            }
        }
    }

    private void registerPoliciesInService(WSDLService service) {
        List extensibilityElements = service.getExtensibilityElements();
        registerPoliciesAsElements(extensibilityElements);

        Iterator iterator = service.getEndpoints().values().iterator();
        while (iterator.hasNext()) {
            WSDLEndpoint wsdlEndpoint = (WSDLEndpoint) iterator.next();
            extensibilityElements = wsdlEndpoint.getExtensibilityElements();
            registerPoliciesAsElements(extensibilityElements);
        }
    }

    private void registerPoliciesInWSDLBinding(WSDLBinding wsdlBinding) {
        List extensibilityElements = wsdlBinding.getExtensibilityElements();
        registerPoliciesAsElements(extensibilityElements);

        Iterator iterator = wsdlBinding.getBindingOperations().values()
                .iterator();
        while (iterator.hasNext()) {
            WSDLBindingOperation wsdlBindingOperation = (WSDLBindingOperation) iterator
                    .next();
            registerPoliciesInBindOperation(wsdlBindingOperation);
        }
    }

    private void registerPoliciesInBindOperation(
            WSDLBindingOperation wsdlBindingOperation) {
        List extensibilityElements = wsdlBindingOperation
                .getExtensibilityElements();
        registerPoliciesAsElements(extensibilityElements);

        if (wsdlBindingOperation.getInput() != null) {
            extensibilityElements = wsdlBindingOperation.getInput()
                    .getExtensibilityElements();
            registerPoliciesAsElements(extensibilityElements);
        }
        if (wsdlBindingOperation.getOutput() != null) {
            extensibilityElements = wsdlBindingOperation.getOutput()
                    .getExtensibilityElements();
            registerPoliciesAsElements(extensibilityElements);
        }
    }

    private void registerPoliciesInWSDLInterface(WSDLInterface wsdlInterface) {
        registerPoliciesInElement(wsdlInterface);
        Iterator iterator = wsdlInterface.getOperations().values().iterator();
        while (iterator.hasNext()) {
            WSDLOperation wsdlOperation = (WSDLOperation) iterator.next();
            registerPoliciesInWSDLOperation(wsdlOperation);
        }
    }

    private void registerPoliciesInWSDLOperation(WSDLOperation wsdlOperation) {
        List extensibilityElements = wsdlOperation.getExtensibilityElements();
        registerPoliciesAsElements(extensibilityElements);

        if (wsdlOperation.getInputMessage() != null) {
            registerPoliciesInElement(wsdlOperation.getInputMessage());
        }
        if (wsdlOperation.getOutputMessage() != null) {
            registerPoliciesInElement(wsdlOperation.getOutputMessage());
        }
    }

    private void registerPoliciesInElement(Component component) {
        registerPoliciesAsAttribute(component.getExtensibilityAttributes());
        registerPoliciesAsElements(component.getExtensibilityElements());
    }

    private void registerPoliciesAsElements(List elements) {
        Iterator iterator = elements.iterator();
        while (iterator.hasNext()) {
            Object extensibilityElement = iterator.next();

            if (extensibilityElement instanceof PolicyExtensibilityElement) {
                PolicyExtensibilityElement policyExtensibilityElement = (PolicyExtensibilityElement) extensibilityElement;
                Object policyElement = policyExtensibilityElement.getPolicyElement();
                
                if (policyElement instanceof Policy) {
                	String policyURI = ((Policy) policyElement).getPolicyURI();
                	
                	if (policyURI != null) {
                		reg.register(policyURI, ((Policy) policyElement));
                	}
                	
                } else if (policyElement instanceof PolicyReference) {
                	String policyRefURI = ((PolicyReference) policyElement).getPolicyURIString();
                	
                	if (reg.lookup(policyRefURI) == null) {
                		reg.register(policyRefURI, null);
                	}
                }   
            }
        }
    }

    private void registerPoliciesAsAttribute(List elements) {
        Iterator iterator = elements.iterator();

        while (iterator.hasNext()) {
            WSDLExtensibilityAttribute wsdlExtensibilityAttribute = (WSDLExtensibilityAttribute) iterator
                    .next();
            QName qname = wsdlExtensibilityAttribute.getKey();

            if (qname.getNamespaceURI().equals(
                    PolicyConstants.WS_POLICY_NAMESPACE_URI)
                    && qname.getLocalPart().equals("PolicyURIs")) {
                String value = wsdlExtensibilityAttribute.getValue().toString();
                String[] policyURIs = value.split(" ");
                for (int i = 0; i < policyURIs.length; i++) {
                    String policyURI = policyURIs[i].trim();

                    if (reg.lookup(policyURI) == null) {
                        reg.register(policyURI, null);
                    }
                }
            }
        }
    }

    public boolean hasPolicies() {
    	return (reg.values().hasNext());
    }
}
