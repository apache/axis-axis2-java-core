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

package org.apache.axis2.rpc;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.*;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DefaultObjectSupplier;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MultirefTest extends UtilServerBasedTestCase{

    protected EndpointReference targetEPR =
            new EndpointReference("http://127.0.0.1:"
                    + (UtilServer.TESTING_PORT)
                    + "/axis2/services/EchoXMLService/concat");
	private static final Log log = LogFactory.getLog(MultirefTest.class);
    protected QName serviceName = new QName("EchoXMLService");
    protected QName operationName = new QName(NAMESPACE, "concat");
    protected QName transportName = new QName(NAMESPACE,
            "NullTransport");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;

    protected boolean finish = false;
    public static final String NAMESPACE = "http://rpc.axis2.apache.org/xsd";

    public static Test suite() {
        return getTestSetup(new TestSuite(MultirefTest.class));
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    private void configureSystem(String opName) throws AxisFault {
        targetEPR =
                new EndpointReference("http://127.0.0.1:"
                        + (UtilServer.TESTING_PORT)
                        + "/axis2/services/EchoXMLService/" + opName);
        String className = "org.apache.axis2.rpc.RPCServiceClass";
        operationName = new QName("http://rpc.axis2.apache.org/xsd", opName, "req");
        AxisService service = AxisService.createService(
                className,UtilServer.getConfigurationContext().getAxisConfiguration());
        service.setName("EchoXMLService");
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        UtilServer.deployService(service);
    }

    public void testMulitref1() throws AxisFault {
        configureSystem("echoString");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(NAMESPACE, "my");
        OMElement method = fac.createOMElement("echoString", omNs);
        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href", null, "#1"));
        method.addChild(value);
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);

        OMElement ref = fac.createOMElement("reference", null);
        ref.addAttribute(fac.createOMAttribute("id", null, "1"));
        ref.setText("hello Axis2");
        envelope.getBody().addChild(ref);

        Options options = new Options();

        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configConetxt = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null,null);
        RPCServiceClient rpcClient = new RPCServiceClient(configConetxt, null);
        rpcClient.setOptions(options);
        MessageContext reqMessageContext = ContextFactory.createMessageContext(configConetxt);
        OperationClient opClinet = rpcClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        opClinet.setOptions(options);
        reqMessageContext.setEnvelope(envelope);

        opClinet.addMessageContext(reqMessageContext);
        opClinet.execute(true);

        MessageContext responseMessageContx = opClinet.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

        SOAPEnvelope env = responseMessageContx.getEnvelope();


        assertEquals(env.getBody().getFirstElement().getFirstElement().getText(), "hello Axis2");
    }

    public void testadd() throws AxisFault {
        configureSystem("add");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(NAMESPACE, "my");
        OMElement method = fac.createOMElement("add", omNs);
        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href", null, "#1"));
        method.addChild(value);

        OMElement value2 = fac.createOMElement("arg1", null);
        value2.addAttribute(fac.createOMAttribute("href", null, "#2"));
        method.addChild(value2);

        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);

        OMElement ref = fac.createOMElement("reference", null);
        ref.addAttribute(fac.createOMAttribute("id", null, "1"));
        ref.setText("10");
        envelope.getBody().addChild(ref);

        OMElement ref2 = fac.createOMElement("reference", null);
        ref2.addAttribute(fac.createOMAttribute("id", null, "2"));
        ref2.setText("10");
        envelope.getBody().addChild(ref2);

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configConetxt = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null,null);
        RPCServiceClient rpcClient = new RPCServiceClient(configConetxt, null);
        rpcClient.setOptions(options);
        MessageContext reqMessageContext = ContextFactory.createMessageContext(configConetxt);
        OperationClient opClinet = rpcClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        opClinet.setOptions(options);
        reqMessageContext.setEnvelope(envelope);

        opClinet.addMessageContext(reqMessageContext);
        opClinet.execute(true);

        MessageContext responseMessageContx = opClinet.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

        SOAPEnvelope env = responseMessageContx.getEnvelope();


        assertEquals(env.getBody().getFirstElement().getFirstElement().getText(), "20");
    }

    public void testaddSameRef() throws AxisFault {
        configureSystem("add");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(NAMESPACE, "my");
        OMElement method = fac.createOMElement("add", omNs);
        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href", null, "#1"));
        method.addChild(value);

        OMElement value2 = fac.createOMElement("arg1", null);
        value2.addAttribute(fac.createOMAttribute("href", null, "#1"));
        method.addChild(value2);

        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);

        OMElement ref = fac.createOMElement("reference", null);
        ref.addAttribute(fac.createOMAttribute("id", null, "1"));
        ref.setText("10");
        envelope.getBody().addChild(ref);

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configConetxt = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null,null);
        RPCServiceClient rpcClient = new RPCServiceClient(configConetxt, null);
        rpcClient.setOptions(options);
        MessageContext reqMessageContext = ContextFactory.createMessageContext(configConetxt);
        OperationClient opClinet = rpcClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        opClinet.setOptions(options);
        reqMessageContext.setEnvelope(envelope);

        opClinet.addMessageContext(reqMessageContext);
        opClinet.execute(true);

        MessageContext responseMessageContx = opClinet.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        SOAPEnvelope env = responseMessageContx.getEnvelope();

        assertEquals(env.getBody().getFirstElement().getFirstElement().getText(), "20");
    }

    public void testaddError() {
        try {
            configureSystem("add");
            OMFactory fac = OMAbstractFactory.getOMFactory();

            OMNamespace omNs = fac.createOMNamespace(NAMESPACE, "my");
            OMElement method = fac.createOMElement("add", omNs);
            OMElement value = fac.createOMElement("arg0", null);
            value.addAttribute(fac.createOMAttribute("href", null, "#1"));
            method.addChild(value);

            OMElement value2 = fac.createOMElement("arg1", null);
            value2.addAttribute(fac.createOMAttribute("href", null, "#2"));
            method.addChild(value2);

            SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope envelope = factory.getDefaultEnvelope();
            envelope.getBody().addChild(method);

            OMElement ref = fac.createOMElement("reference", null);
            ref.addAttribute(fac.createOMAttribute("id", null, "1"));
            ref.setText("10");
            envelope.getBody().addChild(ref);

            OMElement ref2 = fac.createOMElement("reference", null);
            ref2.addAttribute(fac.createOMAttribute("id", null, "3"));
            ref2.setText("10");
            envelope.getBody().addChild(ref2);

            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

            ConfigurationContext configConetxt = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(null,null);
            RPCServiceClient rpcClient = new RPCServiceClient(configConetxt, null);
            rpcClient.setOptions(options);
            MessageContext reqMessageContext = ContextFactory.createMessageContext(configConetxt);
            OperationClient opClinet = rpcClient.createClient(ServiceClient.ANON_OUT_IN_OP);
            opClinet.setOptions(options);
            reqMessageContext.setEnvelope(envelope);

            opClinet.addMessageContext(reqMessageContext);
            opClinet.execute(true);

            fail("This should fail with : " + "org.apache.axis2.AxisFault: Invalid reference :2");
        } catch (AxisFault axisFault) {
            String val = axisFault.getFaultDetailElement().toString();
            System.out.println("val = " + val);
            int index = val.indexOf("org.apache.axis2.AxisFault: Invalid reference :2");
            if (index < 0) {
                fail("This should fail with : " + "org.apache.axis2.AxisFault: Invalid reference :2");
            }
        }
    }


    public void testMulitrefBean() throws AxisFault {
        configureSystem("editBean");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(NAMESPACE, "my");
        OMElement method = fac.createOMElement("editBean", omNs);
        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href", null, "#1"));
        method.addChild(value);
        OMElement value2 = fac.createOMElement("arg1", null);
        value2.setText("159");
        method.addChild(value2);


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);


        String ref1 = "<reference id=\"1\"><name>Deepal</name><value href=\"#2\"/><address href=\"#3\"/></reference>";
        OMElement om1 = getOMelemnt(ref1, fac);
        envelope.getBody().addChild(om1);
        String ref2 = "<reference id=\"2\">false</reference>";
        OMElement om2 = getOMelemnt(ref2, fac);
        envelope.getBody().addChild(om2);
        String ref3 = "<reference id=\"3\"><town href=\"#4\"/><number>1010</number></reference>";
        OMElement om3 = getOMelemnt(ref3, fac);
        envelope.getBody().addChild(om3);
        String ref4 = "<reference id=\"4\">Colombo3</reference>";
        OMElement om4 = getOMelemnt(ref4, fac);
        envelope.getBody().addChild(om4);

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configConetxt = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null,null);
        RPCServiceClient rpcClient = new RPCServiceClient(configConetxt, null);
        rpcClient.setOptions(options);
        MessageContext reqMessageContext = ContextFactory.createMessageContext(configConetxt);
        OperationClient opClinet = rpcClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        opClinet.setOptions(options);
        reqMessageContext.setEnvelope(envelope);

        opClinet.addMessageContext(reqMessageContext);
        opClinet.execute(true);

        MessageContext responseMessageContx = opClinet.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

        SOAPEnvelope env = responseMessageContx.getEnvelope();

        OMElement response = env.getBody().getFirstElement();
        MyBean resBean = (MyBean) BeanUtil.deserialize(MyBean.class, response.getFirstElement() , new DefaultObjectSupplier(), null);
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 159);
    }


    public void testbeanOM() throws AxisFault {
        configureSystem("beanOM");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(NAMESPACE, "my");
        OMElement method = fac.createOMElement("beanOM", omNs);
        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href", null, "#1"));
        method.addChild(value);
        OMElement value2 = fac.createOMElement("arg1", null);
        value2.setText("159");
        method.addChild(value2);


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);


        String ref1 = "<reference id=\"1\"><name>Deepal</name><value href=\"#2\"/><address href=\"#3\"/></reference>";
        OMElement om1 = getOMelemnt(ref1, fac);
        envelope.getBody().addChild(om1);
        String ref2 = "<reference id=\"2\">false</reference>";
        OMElement om2 = getOMelemnt(ref2, fac);
        envelope.getBody().addChild(om2);
        String ref3 = "<reference id=\"3\"><town href=\"#4\"/><number>1010</number></reference>";
        OMElement om3 = getOMelemnt(ref3, fac);
        envelope.getBody().addChild(om3);
        String ref4 = "<reference id=\"4\">Colombo3</reference>";
        OMElement om4 = getOMelemnt(ref4, fac);
        envelope.getBody().addChild(om4);

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configConetxt = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null,null);
        RPCServiceClient rpcClient = new RPCServiceClient(configConetxt, null);
        rpcClient.setOptions(options);
        MessageContext reqMessageContext =ContextFactory.createMessageContext(configConetxt);
        OperationClient opClinet = rpcClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        opClinet.setOptions(options);
        reqMessageContext.setEnvelope(envelope);

        opClinet.addMessageContext(reqMessageContext);
        opClinet.execute(true);

        MessageContext responseMessageContx = opClinet.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

        SOAPEnvelope env = responseMessageContx.getEnvelope();

        OMElement response = env.getBody().getFirstElement();
        MyBean resBean = (MyBean) BeanUtil.deserialize(MyBean.class, response.getFirstElement()  , new DefaultObjectSupplier(), null);
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 159);
    }


    public void testomrefs() throws AxisFault {
        configureSystem("omrefs");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(NAMESPACE, "my");
        OMElement method = fac.createOMElement("omrefs", omNs);

        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href", null, "#1"));
        method.addChild(value);

        OMElement value2 = fac.createOMElement("arg1", null);
        value2.addAttribute(fac.createOMAttribute("href", null, "#1"));
        method.addChild(value2);


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);


        String ref1 = "<reference id=\"1\"><name>Deepal</name><value href=\"#2\"/><address href=\"#3\"/></reference>";
        OMElement om1 = getOMelemnt(ref1, fac);
        envelope.getBody().addChild(om1);
        String ref2 = "<reference id=\"2\">false</reference>";
        OMElement om2 = getOMelemnt(ref2, fac);
        envelope.getBody().addChild(om2);
        String ref3 = "<reference id=\"3\"><town href=\"#4\"/><number>1010</number></reference>";
        OMElement om3 = getOMelemnt(ref3, fac);
        envelope.getBody().addChild(om3);
        String ref4 = "<reference id=\"4\">Colombo3</reference>";
        OMElement om4 = getOMelemnt(ref4, fac);
        envelope.getBody().addChild(om4);
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configConetxt = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null,null);
        RPCServiceClient rpcClient = new RPCServiceClient(configConetxt, null);
        rpcClient.setOptions(options);
        MessageContext reqMessageContext =ContextFactory.createMessageContext(configConetxt);
        OperationClient opClinet = rpcClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        opClinet.setOptions(options);
        reqMessageContext.setEnvelope(envelope);

        opClinet.addMessageContext(reqMessageContext);
        opClinet.execute(true);

        MessageContext responseMessageContx = opClinet.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

        SOAPEnvelope env = responseMessageContx.getEnvelope();

        OMElement response = env.getBody().getFirstElement();

        ArrayList args = new ArrayList();
        args.add(boolean.class);

        Object [] resBean = BeanUtil.deserialize(response, args.toArray()  , new DefaultObjectSupplier());
        assertNotNull(resBean);
        assertEquals(((Boolean) resBean[0]).booleanValue(), true);
    }

    private OMElement getOMelemnt(String str, OMFactory fac) throws AxisFault {
        StAXOMBuilder staxOMBuilder;
        try {
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new
                    ByteArrayInputStream(str.getBytes()));
            staxOMBuilder = new
                    StAXOMBuilder(fac, xmlReader);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError factoryConfigurationError) {
            throw new AxisFault(factoryConfigurationError);
        }
        return staxOMBuilder.getDocumentElement();
    }


    public void testechoEmployee() throws AxisFault {
        configureSystem("echoEmployee");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(NAMESPACE, "my");
        OMElement method = fac.createOMElement("echoEmployee", omNs);

        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href", null, "#1"));
        method.addChild(value);


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);


        String str = "<reference id=\"1\">\n" +
                " <name>John</name>\n" +
                " <age>50</age>\n" +
                " <emplyer href=\"#1\"/>\n" +
                " <address href=\"#2\"/>\n" +
                "</reference>";
        envelope.getBody().addChild(getOMelemnt(str, fac));
        str = "<reference id=\"2\">\n" +
                "<town>Colombo3</town><number>1010</number>\n" +
                "</reference>";
        envelope.getBody().addChild(getOMelemnt(str, fac));

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        ConfigurationContext configConetxt = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null,null);
        RPCServiceClient rpcClient = new RPCServiceClient(configConetxt, null);
        rpcClient.setOptions(options);
        MessageContext reqMessageContext = ContextFactory.createMessageContext(configConetxt);
        OperationClient opClinet = rpcClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        opClinet.setOptions(options);
        reqMessageContext.setEnvelope(envelope);

        opClinet.addMessageContext(reqMessageContext);
        opClinet.execute(true);

        MessageContext responseMessageContx = opClinet.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

        SOAPEnvelope env = responseMessageContx.getEnvelope();

        Employee emp = (Employee) BeanUtil.deserialize(Employee.class, env.getBody().getFirstElement().getFirstElement()  , new DefaultObjectSupplier(), null);
        assertNotNull(emp);
    }


    public void testMulitrefArray() throws AxisFault {
        configureSystem("handleArrayList");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(NAMESPACE, "my");
        OMElement method = fac.createOMElement("handleArrayList", omNs);

        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href", null, "#1"));
        method.addChild(value);

        OMElement value2 = fac.createOMElement("arg1", null);
        value2.setText("10");
        method.addChild(value2);


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);


        String str = "<reference id=\"1\">\n" +
                "    <item0>abc</item0>\n" +
                "    <item0>def</item0>\n" +
                "    <item0>ghi</item0>\n" +
                "    <item0>klm</item0>\n" +
                "</reference>";
        StAXOMBuilder staxOMBuilder;
        try {
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new
                    ByteArrayInputStream(str.getBytes()));
            staxOMBuilder = new
                    StAXOMBuilder(fac, xmlReader);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError factoryConfigurationError) {
            throw new AxisFault(factoryConfigurationError);
        }
        envelope.getBody().addChild(staxOMBuilder.getDocumentElement());

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);


        ConfigurationContext configConetxt = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(null,null);
        RPCServiceClient rpcClient = new RPCServiceClient(configConetxt, null);
        rpcClient.setOptions(options);
        MessageContext reqMessageContext = ContextFactory.createMessageContext(configConetxt);
        OperationClient opClinet = rpcClient.createClient(ServiceClient.ANON_OUT_IN_OP);
        opClinet.setOptions(options);
        reqMessageContext.setEnvelope(envelope);

        opClinet.addMessageContext(reqMessageContext);
        opClinet.execute(true);

        MessageContext responseMessageContx = opClinet.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

        SOAPEnvelope env = responseMessageContx.getEnvelope();
        assertEquals(env.getBody().getFirstElement().getFirstElement().getText(), "abcdefghiklm10");
    }

}
