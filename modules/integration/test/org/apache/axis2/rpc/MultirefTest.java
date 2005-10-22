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
*
* @author : Deepal Jayasinghe (deepal@apache.org)
*/

package org.apache.axis2.rpc;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.InOutOperationDescrition;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.util.BeanSerializerUtil;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.axis2.rpc.client.RPCCall;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.FactoryConfigurationError;
import java.text.SimpleDateFormat;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

public class MultirefTest extends TestCase {

    private SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    //  0123456789 0 123456789


    protected EndpointReference targetEPR =
            new EndpointReference("http://127.0.0.1:"
                    + (UtilServer.TESTING_PORT)
                    + "/axis/services/EchoXMLService/concat");
    protected Log log = LogFactory.getLog(getClass());
    protected QName serviceName = new QName("EchoXMLService");
    protected QName operationName = new QName("http://localhost/my", "concat");
    protected QName transportName = new QName("http://localhost/my",
            "NullTransport");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected ServiceDescription service;

    protected boolean finish = false;

    protected void setUp() throws Exception {
        UtilServer.start();
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }
    private void configureSystem(String opName) throws AxisFault {
        targetEPR =
                new EndpointReference("http://127.0.0.1:"
//                        + (5000)
                        + (UtilServer.TESTING_PORT)
                        + "/axis/services/EchoXMLService/" + opName);
        String className = "org.apache.axis2.rpc.RPCServiceClass";
        operationName = new QName("http://localhost/my", opName, "req");
        ServiceDescription service = new ServiceDescription(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS,
                className));
        OperationDescription axisOp = new InOutOperationDescrition(operationName);
        axisOp.setMessageReceiver(new RPCMessageReceiver());
        axisOp.setStyle(WSDLService.STYLE_RPC);
        service.addOperation(axisOp);
        UtilServer.deployService(service);
    }

    public void testMulitref1() throws AxisFault {
        configureSystem("echoString");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoString", omNs);
        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href",null,"#1"));
        method.addChild(value);
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);

        OMElement ref = fac.createOMElement("reference", null);
        ref.addAttribute(fac.createOMAttribute("id",null,"1"));
        ref.setText("hello Axis2");
        envelope.getBody().addChild(ref);
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        SOAPEnvelope env = call.invokeBlocking("echoString",envelope);
        assertEquals(env.getBody().getFirstElement().getFirstElement().getText(), "hello Axis2");
    }

    public void testadd() throws AxisFault {
        configureSystem("add");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("add", omNs);
        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href",null,"#1"));
        method.addChild(value);

        OMElement value2 = fac.createOMElement("arg1", null);
        value2.addAttribute(fac.createOMAttribute("href",null,"#2"));
        method.addChild(value2);

        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);

        OMElement ref = fac.createOMElement("reference", null);
        ref.addAttribute(fac.createOMAttribute("id",null,"1"));
        ref.setText("10");
        envelope.getBody().addChild(ref);

        OMElement ref2 = fac.createOMElement("reference", null);
        ref2.addAttribute(fac.createOMAttribute("id",null,"2"));
        ref2.setText("10");
        envelope.getBody().addChild(ref2);


        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        SOAPEnvelope env = call.invokeBlocking("add",envelope);
        assertEquals(env.getBody().getFirstElement().getFirstElement().getText(), "20");
    }

    public void testaddSameRef() throws AxisFault {
        configureSystem("add");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("add", omNs);
        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href",null,"#1"));
        method.addChild(value);

        OMElement value2 = fac.createOMElement("arg1", null);
        value2.addAttribute(fac.createOMAttribute("href",null,"#1"));
        method.addChild(value2);

        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);

        OMElement ref = fac.createOMElement("reference", null);
        ref.addAttribute(fac.createOMAttribute("id",null,"1"));
        ref.setText("10");
        envelope.getBody().addChild(ref);
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        SOAPEnvelope env = call.invokeBlocking("add",envelope);
        assertEquals(env.getBody().getFirstElement().getFirstElement().getText(), "20");
    }

    public void testaddError() {
        try {
            configureSystem("add");
            OMFactory fac = OMAbstractFactory.getOMFactory();

            OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
            OMElement method = fac.createOMElement("add", omNs);
            OMElement value = fac.createOMElement("arg0", null);
            value.addAttribute(fac.createOMAttribute("href",null,"#1"));
            method.addChild(value);

            OMElement value2 = fac.createOMElement("arg1", null);
            value2.addAttribute(fac.createOMAttribute("href",null,"#2"));
            method.addChild(value2);

            SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope envelope = factory.getDefaultEnvelope();
            envelope.getBody().addChild(method);

            OMElement ref = fac.createOMElement("reference", null);
            ref.addAttribute(fac.createOMAttribute("id",null,"1"));
            ref.setText("10");
            envelope.getBody().addChild(ref);

            OMElement ref2 = fac.createOMElement("reference", null);
            ref2.addAttribute(fac.createOMAttribute("id",null,"3"));
            ref2.setText("10");
            envelope.getBody().addChild(ref2);


            RPCCall call =
                    new RPCCall("target/test-resources/intregrationRepo");

            call.setTo(targetEPR);
            call.setTransportInfo(Constants.TRANSPORT_HTTP,
                    Constants.TRANSPORT_HTTP,
                    false);
            call.invokeBlocking("add",envelope);
            fail("This should fail with : " + "org.apache.axis2.AxisFault: Invalid reference :2");
        } catch (AxisFault axisFault) {
            String val = axisFault.getMessage();
            int index =  val.indexOf("org.apache.axis2.AxisFault: Invalid reference :2") ;
            if(index <0){
                fail("This should fail with : " + "org.apache.axis2.AxisFault: Invalid reference :2");
            }
        }
    }


    public void testMulitrefBean() throws AxisFault {
        configureSystem("editBean");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("editBean", omNs);
        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href",null,"#1"));
        method.addChild(value);
        OMElement value2 = fac.createOMElement("arg1", null);
        value2.setText("159");
        method.addChild(value2);


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);


        String ref1 ="<reference id=\"1\"><name>Deepal</name><value href=\"#2\"/><address href=\"#3\"/></reference>";
        OMElement om1 = getOMelemnt(ref1,fac);
        envelope.getBody().addChild(om1);
        String ref2 = "<reference id=\"2\">false</reference>";
        OMElement om2 = getOMelemnt(ref2,fac);
        envelope.getBody().addChild(om2);
        String ref3 = "<reference id=\"3\"><town href=\"#4\"/><number>1010</number></reference>";
        OMElement om3 = getOMelemnt(ref3,fac);
        envelope.getBody().addChild(om3);
        String ref4 = "<reference id=\"4\">Colombo3</reference>";
        OMElement om4 = getOMelemnt(ref4,fac);
        envelope.getBody().addChild(om4);

        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        SOAPEnvelope env = call.invokeBlocking("editBean",envelope);
        OMElement response = env.getBody().getFirstElement();
        MyBean resBean = (MyBean) BeanSerializerUtil.deserialize(MyBean.class, response.getFirstElement());
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 159);
        call.close();
    }


    public void testbeanOM() throws AxisFault {
        configureSystem("beanOM");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("beanOM", omNs);
        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href",null,"#1"));
        method.addChild(value);
        OMElement value2 = fac.createOMElement("arg1", null);
        value2.setText("159");
        method.addChild(value2);


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);


        String ref1 ="<reference id=\"1\"><name>Deepal</name><value href=\"#2\"/><address href=\"#3\"/></reference>";
        OMElement om1 = getOMelemnt(ref1,fac);
        envelope.getBody().addChild(om1);
        String ref2 = "<reference id=\"2\">false</reference>";
        OMElement om2 = getOMelemnt(ref2,fac);
        envelope.getBody().addChild(om2);
        String ref3 = "<reference id=\"3\"><town href=\"#4\"/><number>1010</number></reference>";
        OMElement om3 = getOMelemnt(ref3,fac);
        envelope.getBody().addChild(om3);
        String ref4 = "<reference id=\"4\">Colombo3</reference>";
        OMElement om4 = getOMelemnt(ref4,fac);
        envelope.getBody().addChild(om4);

        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        SOAPEnvelope env = call.invokeBlocking("beanOM",envelope);
        OMElement response = env.getBody().getFirstElement();
        MyBean resBean = (MyBean) BeanSerializerUtil.deserialize(MyBean.class, response.getFirstElement());
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 159);
        call.close();
    }


    public void testomrefs() throws AxisFault {
        configureSystem("omrefs");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("omrefs", omNs);

        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href",null,"#1"));
        method.addChild(value);

        OMElement value2 = fac.createOMElement("arg1", null);
        value2.addAttribute(fac.createOMAttribute("href",null,"#1"));
        method.addChild(value2);


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);


        String ref1 ="<reference id=\"1\"><name>Deepal</name><value href=\"#2\"/><address href=\"#3\"/></reference>";
        OMElement om1 = getOMelemnt(ref1,fac);
        envelope.getBody().addChild(om1);
        String ref2 = "<reference id=\"2\">false</reference>";
        OMElement om2 = getOMelemnt(ref2,fac);
        envelope.getBody().addChild(om2);
        String ref3 = "<reference id=\"3\"><town href=\"#4\"/><number>1010</number></reference>";
        OMElement om3 = getOMelemnt(ref3,fac);
        envelope.getBody().addChild(om3);
        String ref4 = "<reference id=\"4\">Colombo3</reference>";
        OMElement om4 = getOMelemnt(ref4,fac);
        envelope.getBody().addChild(om4);

        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        SOAPEnvelope env = call.invokeBlocking("omrefs",envelope);
        OMElement response = env.getBody().getFirstElement();

        ArrayList args = new ArrayList();
        args.add(boolean.class);

        Object [] resBean = BeanSerializerUtil.deserialize(response,args.toArray());
        assertNotNull(resBean);
        assertEquals(((Boolean)resBean[0]).booleanValue(),true);
        call.close();
    }

    private OMElement getOMelemnt(String str,OMFactory fac) throws AxisFault {
        StAXOMBuilder staxOMBuilder;
        try {
            XMLStreamReader xmlReader=  XMLInputFactory.newInstance().createXMLStreamReader(new
                    ByteArrayInputStream(str.getBytes()));
            staxOMBuilder = new
                    StAXOMBuilder(fac,xmlReader);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError factoryConfigurationError) {
            throw new AxisFault(factoryConfigurationError);
        }
        return staxOMBuilder.getDocumentElement();
    }


    public void testMulitrefArray() throws AxisFault {
        configureSystem("handleArrayList");
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("handleArrayList", omNs);

        OMElement value = fac.createOMElement("arg0", null);
        value.addAttribute(fac.createOMAttribute("href",null,"#1"));
        method.addChild(value);

        OMElement value2 = fac.createOMElement("arg1", null);
        value2.setText("10");
        method.addChild(value2);


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.getDefaultEnvelope();
        envelope.getBody().addChild(method);



        String str= "<reference id=\"1\">\n" +
                "    <item0>abc</item0>\n" +
                "    <item0>def</item0>\n" +
                "    <item0>ghi</item0>\n" +
                "    <item0>klm</item0>\n" +
                "</reference>";
        StAXOMBuilder staxOMBuilder ;
        try {
            XMLStreamReader xmlReader=  XMLInputFactory.newInstance().createXMLStreamReader(new
                    ByteArrayInputStream(str.getBytes()));
            staxOMBuilder = new
                    StAXOMBuilder(fac,xmlReader);
        } catch (XMLStreamException e) {
            throw  new AxisFault(e);
        } catch (FactoryConfigurationError factoryConfigurationError) {
            throw  new AxisFault(factoryConfigurationError);
        }
        envelope.getBody().addChild(staxOMBuilder.getDocumentElement());
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        SOAPEnvelope env = call.invokeBlocking("handleArrayList",envelope);
        assertEquals(env.getBody().getFirstElement().getFirstElement().getText(), "abcdefghiklm10");
    }

}
