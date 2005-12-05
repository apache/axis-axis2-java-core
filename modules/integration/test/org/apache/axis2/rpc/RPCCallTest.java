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

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.rpc.client.RPCCall;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.axis2.util.BeanSerializerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class RPCCallTest extends TestCase {

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
    protected AxisService service;

    protected boolean finish = false;

    public RPCCallTest() {
        super(RPCCallTest.class.getName());
    }

    public RPCCallTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilServer.start();
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
        UtilServer.unDeployClientService();
    }

    public void testEditBean() throws AxisFault {
        configureSystem("editBean");
        String clientHome = "target/test-resources/integrationRepo";

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call = new RPCCall(clientHome);
        call.setClientOptions(options);

        MyBean bean = new MyBean();
        bean.setAge(100);
        bean.setName("Deepal");
        bean.setValue(false);
        AddressBean ab = new AddressBean();
        ab.setNumber(1010);
        ab.setTown("Colombo3");
        bean.setAddress(ab);


        ArrayList args = new ArrayList();
        args.add(bean);
        args.add("159");

        OMElement response = call.invokeBlocking(operationName, args.toArray());
//        MyBean resBean =(MyBean) new  BeanSerializer(MyBean.class,response).deserilze();
        MyBean resBean = (MyBean) BeanSerializerUtil.deserialize(MyBean.class, response.getFirstElement());
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 159);
        call.close();
    }

    private void configureSystem(String opName) throws AxisFault {
        targetEPR =
                new EndpointReference("http://127.0.0.1:"
//                        + (5000)
                        + (UtilServer.TESTING_PORT)
                        + "/axis/services/EchoXMLService/" + opName);
        String className = "org.apache.axis2.rpc.RPCServiceClass";
        operationName = new QName("http://localhost/my", opName, "req");
        AxisService service = new AxisService(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS,
                className));
        AxisOperation axisOp = new InOutAxisOperation(operationName);
        axisOp.setMessageReceiver(new RPCMessageReceiver());
        axisOp.setStyle(WSDLService.STYLE_RPC);
        service.addOperation(axisOp);
        UtilServer.deployService(service);
    }

    public void testEchoBean() throws AxisFault {
        configureSystem("echoBean");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        MyBean bean = new MyBean();
        bean.setAge(100);
        bean.setName("Deepal");
        bean.setValue(false);
        AddressBean ab = new AddressBean();
        ab.setNumber(1010);
        ab.setTown("Colombo3");
        bean.setAddress(ab);

        ArrayList args = new ArrayList();
        args.add(bean);


        OMElement response = call.invokeBlocking(operationName, args.toArray());
        MyBean resBean = (MyBean) BeanSerializerUtil.deserialize(MyBean.class, response.getFirstElement());
//        MyBean resBean =(MyBean) new  BeanSerializer(MyBean.class,response).deserilze();
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 100);
        call.close();
    }


    public void testEchoString() throws AxisFault {
        configureSystem("echoString");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        ArrayList args = new ArrayList();
        args.add("foo");
        OMElement response = call.invokeBlocking(operationName, args.toArray());
        assertEquals(response.getFirstElement().getText(), "foo");
        call.close();
    }

    public void testEchoInt() throws AxisFault {
        configureSystem("echoInt");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        ArrayList args = new ArrayList();
        args.add("100");

        OMElement response = call.invokeBlocking(operationName, args.toArray());
        assertEquals(Integer.parseInt(response.getFirstElement().getText()), 100);
        call.close();
    }

    public void testAdd() throws AxisFault {
        configureSystem("add");
        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);
        call.setClientOptions(options);
        ArrayList args = new ArrayList();
        args.add("100");
        args.add("200");

        OMElement response = call.invokeBlocking(operationName, args.toArray());
        assertEquals(Integer.parseInt(response.getFirstElement().getText()), 300);
        call.close();
    }

    public void testDivide() throws AxisFault {
        configureSystem("divide");
        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");

        Options options = new Options();
        call.setClientOptions(options);
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        ArrayList args = new ArrayList();
        args.add("10");
        args.add("0");
        OMElement response = call.invokeBlocking(operationName, args.toArray());
        assertEquals(response.getFirstElement().getText(), "INF");
        call.close();
    }

    public void testEchoBool() throws AxisFault {
        configureSystem("echoBool");
        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);
        call.setClientOptions(options);

        ArrayList args = new ArrayList();
        args.add("true");

        OMElement response = call.invokeBlocking(operationName, args.toArray());
        assertEquals(Boolean.valueOf(response.getFirstElement().getText()).booleanValue(), true);
        call.close();
    }

    public void testEchoByte() throws AxisFault {
        configureSystem("echoByte");

        Options options = new Options();

        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        ArrayList args = new ArrayList();
        args.add("1");
        OMElement response = call.invokeBlocking(operationName, args.toArray());
        assertEquals(Byte.parseByte(response.getFirstElement().getText()), 1);
        call.close();
    }

    public void testCompany() throws AxisFault {
        configureSystem("echoCompany");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        Company com = new Company();
        com.setName("MyCompany");

        ArrayList ps = new ArrayList();

        Person p1 = new Person();
        p1.setAge(10);
        p1.setName("P1");
        ps.add(p1);

        Person p2 = new Person();
        p2.setAge(15);
        p2.setName("P2");
        ps.add(p2);

        Person p3 = new Person();
        p3.setAge(20);
        p3.setName("P3");
        ps.add(p3);

        com.setPersons(ps);
        ArrayList args = new ArrayList();
        args.add(com);
        OMElement response = call.invokeBlocking(operationName, args.toArray());
        call.close();
    }

    public void testEchoOM() throws AxisFault {
        configureSystem("echoOM");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        ArrayList args = new ArrayList();
        args.add("1");
        OMElement response = call.invokeBlocking(operationName, args.toArray());
        assertEquals(Byte.parseByte(response.getFirstElement().getText()), 1);
        call.close();
    }

    public void testCalender() throws AxisFault {
        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
        configureSystem("echoCalander");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        ArrayList args = new ArrayList();
        Date date = Calendar.getInstance().getTime();
        args.add(zulu.format(date));
        OMElement response = call.invokeBlocking(operationName, args.toArray());
        assertEquals(response.getFirstElement().getText(), zulu.format(date));
        call.close();
    }


    ////////////////////////////////////////////////// Invoking by Passing Return types //////////
    public void testechoBean2() throws AxisFault {
        configureSystem("echoBean");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        MyBean bean = new MyBean();
        bean.setAge(100);
        bean.setName("Deepal");
        bean.setValue(false);
        AddressBean ab = new AddressBean();
        ab.setNumber(1010);
        ab.setTown("Colombo3");
        bean.setAddress(ab);

        ArrayList args = new ArrayList();
        args.add(bean);

        ArrayList ret = new ArrayList();
        ret.add(MyBean.class);

        Object [] response = call.invokeBlocking(operationName, args.toArray(), ret.toArray());
        MyBean resBean = (MyBean) response[0];
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 100);
        call.close();
    }

    public void testechoInt2() throws AxisFault {
        configureSystem("echoInt");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        ArrayList args = new ArrayList();
        args.add("100");

        ArrayList ret = new ArrayList();
        ret.add(Integer.class);

        Object [] response = call.invokeBlocking(operationName, args.toArray(), ret.toArray());
        assertEquals(((Integer) response[0]).intValue(), 100);
        call.close();
    }

    public void testmultireturn() throws AxisFault {
        configureSystem("multireturn");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        ArrayList args = new ArrayList();
        args.add("1");

        ArrayList ret = new ArrayList();
        ret.add(Integer.class);
        ret.add(String.class);

        Object [] response = call.invokeBlocking(operationName, args.toArray(), ret.toArray());
        assertEquals(((Integer) response[0]).intValue(), 10);
        assertEquals(response[1], "foo");
//        assertEquals(Byte.parseByte(response.getFirstElement().getText()),1);
        call.close();
    }

    public void testmulReturn() throws AxisFault {
        configureSystem("mulReturn");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        ArrayList args = new ArrayList();
        args.add("foo");


        OMElement response = call.invokeBlocking(operationName, args.toArray());
//        assertEquals(response.getFirstElement().getText(), "foo");
        call.close();
    }


    public void testhandleArrayList() throws AxisFault {
        configureSystem("handleArrayList");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(false);

        RPCCall call =
                new RPCCall("target/test-resources/integrationRepo");
        call.setClientOptions(options);

        OMElement elem = call.invokeBlocking("handleArrayList", getpayLoad());
        assertEquals(elem.getFirstElement().getText(), "abcdefghiklm10");
        call.close();
    }

    private OMElement getpayLoad() throws AxisFault {
        String str = "<handleArrayList>\n" +
                "  <arg0>\n" +
                "    <item0>abc</item0>\n" +
                "    <item0>def</item0>\n" +
                "    <item0>ghi</item0>\n" +
                "    <item0>klm</item0>\n" +
                "  </arg0><arg1>10</arg1>" +
                "</handleArrayList>";
        StAXOMBuilder staxOMBuilder;
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(new
                    ByteArrayInputStream(str.getBytes()));
            OMFactory fac = OMAbstractFactory.getOMFactory();

            staxOMBuilder = new
                    StAXOMBuilder(fac, xmlReader);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError factoryConfigurationError) {
            throw new AxisFault(factoryConfigurationError);
        }
        return staxOMBuilder.getDocumentElement();
    }


}
