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
package sample.sgccalculator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CalcClient {

    final static String addService = "http://localhost:8080/axis2/services/AddService";

    final static String substractService = "http://localhost:8080/axis2/services/SubstractService";

    final static String multiplyService = "http://localhost:8080/axis2/services/MultiplyService";

    public static void main(String[] args) throws AxisFault, IOException {

        System.out.println("\nTHIS IS A SAMPLE APPLICATION TO DEMONSTRATE THE FUNCTIONALITY OF SERVICE GROUPS");
        System.out.println("===============================================================================");
        Options options = new Options();
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        boolean exit = false;
        String serviceGroupContextId = null;
        while (!exit) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("\n\nNew round (n) /continue round (c).../exit (e)");
            String option = reader.readLine();
            if ("e".equalsIgnoreCase(option)) {
                System.out.println("Exiting calculator...");
                return;
            }

            if (!"n".equalsIgnoreCase(option) && !"c".equalsIgnoreCase(option)) {
                System.out.println("Error: Invalid option");
                continue;
            }

            System.out.print("Please Select the service ( '+' / '-' / '*' )....");
            String operation = reader.readLine();
            if (!"+".equalsIgnoreCase(operation) && !"-".equalsIgnoreCase(operation) && !"*".equalsIgnoreCase(operation)) {
                System.out.println("Error: Invalid option");
                continue;
            }

            if ("+".equals(operation))
                options.setTo(new EndpointReference(addService));
            else if ("-".equals(operation))
                options.setTo(new EndpointReference(substractService));
            else if ("*".equals(operation))
                options.setTo(new EndpointReference(multiplyService));

            if ("n".equalsIgnoreCase(option)) {
                System.out.print("Enter parameter 1...");
                String param1Str = reader.readLine();
                System.out.print("Enter parameter 2...");
                String param2Str = reader.readLine();
                int param1 = Integer.parseInt(param1Str);
                int param2 = Integer.parseInt(param2Str);

                String opStr = null;
                if ("+".equals(operation)) {
                    opStr = "add";
                } else if ("-".equals(operation)) {
                    opStr = "substract";
                } else if ("*".equals(operation)) {
                    opStr = "multiply";
                }
                System.out.println("Invoking...");

                ServiceClient serviceClient = new ServiceClient();
                serviceClient.engageModule("addressing");
                serviceClient.setOptions(options);
                options.setAction("urn:" + opStr);
                MessageContext requetMessageContext = new MessageContext();
                requetMessageContext.setEnvelope(getRequestEnvelope(opStr, param1, param2,
                        serviceGroupContextId));

                OperationClient opClient = serviceClient.createClient(ServiceClient.ANON_OUT_IN_OP);
                opClient.addMessageContext(requetMessageContext);
                opClient.setOptions(options);

                opClient.execute(true);

                SOAPEnvelope result = opClient.getMessageContext(
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE).getEnvelope();

                printResult(result);

                if (serviceGroupContextId == null)
                    serviceGroupContextId = getServiceGroupContextId(result);


            } else if ("c".equalsIgnoreCase(option)) {
                if (serviceGroupContextId == null) {
                    System.out.println("Error: First operation must be a New one. Please select 'n'");
                    continue;
                }

                System.out.print("Enter parameter...");
                String paramStr = reader.readLine();
                int param = Integer.parseInt(paramStr);
                String opStr = null;
                if ("+".equals(operation)) {
                    opStr = "addPrevious";
                } else if ("-".equals(operation)) {
                    opStr = "substractPrevious";
                } else if ("*".equals(operation)) {
                    opStr = "multiplyPrevious";
                }

                System.out.println("Invoking...");


                ServiceClient serviceClient = new ServiceClient();
                serviceClient.setOptions(options);
                options.setAction("urn:" + opStr);
                MessageContext requetMessageContext = new MessageContext();
                requetMessageContext.setEnvelope(getPreviousRequestEnvelope(opStr, param,
                        serviceGroupContextId));

                OperationClient opClient = serviceClient.createClient(ServiceClient.ANON_OUT_IN_OP);
                opClient.addMessageContext(requetMessageContext);
                opClient.setOptions(options);

                opClient.execute(true);

                SOAPEnvelope result = opClient.getMessageContext(
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE).getEnvelope();

                printResult(result);

            }

        }
    }

    public static void printResult(SOAPEnvelope result) {
        try {
            XMLStreamWriter writer = XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(System.out);
            if (result != null) {
                OMElement resultOM = result.getBody().getFirstChildWithName(new QName("result"));
                System.out.println("Result is:" + resultOM.getText());
            } else
                System.out.println("Result is null");
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (FactoryConfigurationError e) {
            e.printStackTrace();
        }
    }

    public static SOAPEnvelope getRequestEnvelope(String operationName,
                                                  int param1, int param2, String groupContextId) {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMNamespace namespace = fac.createOMNamespace(
                "http://axis2/test/namespace1", "ns1");

        OMElement params = fac.createOMElement(operationName, namespace);
        OMElement param1OM = fac.createOMElement("param1", namespace);
        OMElement param2OM = fac.createOMElement("param2", namespace);
        param1OM.setText(Integer.toString(param1));
        param2OM.setText(Integer.toString(param2));
        params.addChild(param1OM);
        params.addChild(param2OM);
        envelope.getBody().setFirstChild(params);

        if (groupContextId != null) {
            OMNamespace axis2Namespace = fac.createOMNamespace(
                    Constants.AXIS2_NAMESPACE_URI,
                    Constants.AXIS2_NAMESPACE_PREFIX);
            SOAPHeaderBlock soapHeaderBlock = envelope.getHeader()
                    .addHeaderBlock(Constants.SERVICE_GROUP_ID, axis2Namespace);
            soapHeaderBlock.setText(groupContextId);
        }

        return envelope;
    }

    public static SOAPEnvelope getPreviousRequestEnvelope(String operationName,
                                                          int param, String groupContextId) {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMNamespace namespace = fac.createOMNamespace(
                "http://axis2/test/namespace1", "ns1");

        OMElement params = fac.createOMElement(operationName, namespace);
        OMElement paramOM = fac.createOMElement("param", namespace);
        paramOM.setText(Integer.toString(param));
        params.addChild(paramOM);
        envelope.getBody().setFirstChild(params);

        if (groupContextId != null) {
            OMNamespace axis2Namespace = fac.createOMNamespace(
                    Constants.AXIS2_NAMESPACE_URI,
                    Constants.AXIS2_NAMESPACE_PREFIX);
            SOAPHeaderBlock soapHeaderBlock = envelope.getHeader()
                    .addHeaderBlock(Constants.SERVICE_GROUP_ID, axis2Namespace);
            soapHeaderBlock.setText(groupContextId);
        }

        return envelope;
    }

    public static String getServiceGroupContextId(SOAPEnvelope resultEnvelope) {
        return resultEnvelope.getHeader()
                .getFirstChildWithName(new QName("ReplyTo"))
                .getFirstChildWithName(new QName("ReferenceParameters"))
                .getFirstChildWithName(new QName("ServiceGroupId")).getText();
    }
}