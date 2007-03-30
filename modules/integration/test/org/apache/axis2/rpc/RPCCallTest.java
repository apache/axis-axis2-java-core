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
*/
package org.apache.axis2.rpc;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.DefaultObjectSupplier;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class RPCCallTest extends UtilServerBasedTestCase {

    private SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    //  0123456789 0 123456789


    protected EndpointReference targetEPR;
    protected QName serviceName = new QName("EchoXMLService");
    protected QName operationName = new QName("http://org.apache.axis2/xsd", "concat");
    protected QName transportName = new QName("http://org.apache.axis2/xsd",
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

    public static Test suite() {
        return getTestSetup(new TestSuite(RPCCallTest.class));
    }


    protected void setUp() throws Exception {
        String className = "org.apache.axis2.rpc.RPCServiceClass";
        UtilServer.start();
        service = AxisService.createService(
                className, UtilServer.getConfigurationContext().getAxisConfiguration());
        service.setName("EchoXMLService");
        service.setClassLoader(Thread.currentThread().getContextClassLoader());

        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testEditBean() throws AxisFault {
        configureSystem("editBean");

        Options options = new Options();
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);
        options.setTo(targetEPR);

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

        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        MyBean resBean = (MyBean)BeanUtil.deserialize(MyBean.class, response.getFirstElement(),
                                                      new DefaultObjectSupplier(), null);
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 159);
    }

    private void configureSystem(String opName) {
        targetEPR =
                new EndpointReference("http://127.0.0.1:"
                        + (UtilServer.TESTING_PORT)
                        + "/axis2/services/EchoXMLService/" + opName);
        operationName = new QName("http://rpc.axis2.apache.org/xsd", opName, "req");
        AxisOperation axisOp = new InOutAxisOperation(operationName);
        axisOp.setMessageReceiver(new RPCMessageReceiver());
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);
    }

    public void testEchoBean() throws AxisFault {
        configureSystem("echoBean");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

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


        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        MyBean resBean = (MyBean)BeanUtil.deserialize(MyBean.class,
                                                      response.getFirstElement(),
                                                      new DefaultObjectSupplier(), null);
//        MyBean resBean =(MyBean) new  BeanSerializer(MyBean.class,response).deserilze();
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 100);
    }

    public void testechoMail() throws AxisFault {
        configureSystem("echoMail");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        Mail mail = new Mail();
        mail.setBody("My Body");
        mail.setContentType("ContentType");
        mail.setFrom("From");
        mail.setId("ID");
        mail.setSubject("Subject");
        mail.setTo("To");

        ArrayList args = new ArrayList();
        args.add(mail);


        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        Mail resBean = (Mail)BeanUtil.deserialize(Mail.class, response.getFirstElement(),
                                                  new DefaultObjectSupplier(), null);
//        MyBean resBean =(MyBean) new  BeanSerializer(MyBean.class,response).deserilze();
        assertNotNull(resBean);
        assertEquals(resBean.getBody(), "My Body");
    }


    public void testEchoString() throws AxisFault {
        configureSystem("echoString");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        ArrayList args = new ArrayList();
        args.add("foo");
        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        assertEquals(response.getFirstElement().getText(), "foo");
    }

    public void testEchoInt() throws AxisFault {
        configureSystem("echoInt");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        ArrayList args = new ArrayList();
        args.add("100");

        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        assertEquals(Integer.parseInt(response.getFirstElement().getText()), 100);
    }

    public void testAdd() throws AxisFault {
        configureSystem("add");
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        sender.setOptions(options);
        ArrayList args = new ArrayList();
        args.add("100");
        args.add("200");

        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        assertEquals(Integer.parseInt(response.getFirstElement().getText()), 300);
    }

    public void testByteArray() throws AxisFault {
        configureSystem("testByteArray");
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        sender.setOptions(options);
        ArrayList args = new ArrayList();
        String hello = "hello";
        args.add(hello.getBytes());

        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        assertEquals(response.getFirstElement().getText(), hello);
    }

    //

    public void testDivide() throws AxisFault {
        configureSystem("divide");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);


        ArrayList args = new ArrayList();
        args.add("10");
        args.add("0");
        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        assertEquals(response.getFirstElement().getText(), "INF");
    }

    public void testEchoBool() throws AxisFault {
        configureSystem("echoBool");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        ArrayList args = new ArrayList();
        args.add("true");

        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        assertEquals(Boolean.valueOf(response.getFirstElement().getText()).booleanValue(), true);
    }

    public void testEchoByte() throws AxisFault {
        configureSystem("echoByte");

        Options options = new Options();

        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        ArrayList args = new ArrayList();
        args.add("1");
        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        assertEquals(Byte.parseByte(response.getFirstElement().getText()), 1);
    }

    public void testCompany() throws AxisFault {
        configureSystem("echoCompany");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

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
        sender.invokeBlocking(operationName, args.toArray());
    }


    public void testtestCompany() throws AxisFault {
        configureSystem("testCompanyArray");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

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
        args.add(com);
        args.add(com);
        args.add(com);

        ArrayList req = new ArrayList();
        req.add(args.toArray());
        OMElement value = sender.invokeBlocking(operationName, req.toArray());
        assertEquals("4", value.getFirstElement().getText());
    }

    public void testCompanyArray() throws AxisFault {
        configureSystem("CompanyArray");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

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
        args.add(com);
        args.add(com);
        args.add(com);

        ArrayList req = new ArrayList();
        req.add(args.toArray());
        ArrayList resobj = new ArrayList();
        resobj.add(Company.class);
        resobj.add(Company.class);
        resobj.add(Company.class);
        resobj.add(Company.class);
        Object [] value = sender.invokeBlocking(operationName, req.toArray(),
                                                (Class[])resobj.toArray(new Class[resobj.size()]));
        assertEquals(4, value.length);
        assertEquals(((Company)value[0]).getName(), "MyCompany");
    }


    public void testEchoOM() throws AxisFault {
        configureSystem("echoOM");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        ArrayList args = new ArrayList();
        args.add("1");
        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        assertEquals(Byte.parseByte(response.getFirstElement().getFirstElement().getText()), 1);
    }

    public void testCalender() throws AxisFault {
        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
        configureSystem("echoCalander");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        ArrayList args = new ArrayList();
        Date date = Calendar.getInstance().getTime();
        args.add(zulu.format(date));
        OMElement response = sender.invokeBlocking(operationName, args.toArray());
        assertEquals(response.getFirstElement().getText(), zulu.format(date));
    }


    ////////////////////////////////////////////////// Invoking by Passing Return types //////////
    public void testechoBean2() throws AxisFault {
        configureSystem("echoBean");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

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

        Object [] response = sender.invokeBlocking(operationName, args.toArray(),
                                                   (Class[])ret.toArray(new Class[ret.size()]));
        MyBean resBean = (MyBean)response[0];
        assertNotNull(resBean);
        assertEquals(resBean.getAge(), 100);
    }

    public void testechoInt2() throws AxisFault {
        configureSystem("echoInt");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        ArrayList args = new ArrayList();
        args.add("100");

        ArrayList ret = new ArrayList();
        ret.add(Integer.class);

        Object [] response = sender.invokeBlocking(operationName, args.toArray(),
                                                   (Class[])ret.toArray(new Class[ret.size()]));
        assertEquals(((Integer)response[0]).intValue(), 100);
    }

//    public void testmultireturn() throws AxisFault {
//        configureSystem("multireturn");
//
//        Options options = new Options();
//        options.setTo(targetEPR);
//        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
//
//        ConfigurationContext configContext =
//                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
//        RPCServiceClient sender = new RPCServiceClient(configContext, null);
//        sender.setOptions(options);
//
//        ArrayList args = new ArrayList();
//        args.add("1");
//
//        ArrayList ret = new ArrayList();
//        ret.add(Integer.class);
//        ret.add(String.class);
//
//        Object [] response = sender.invokeBlocking(operationName, args.toArray(), ret.toArray());
//        assertEquals(((Integer) response[0]).intValue(), 10);
//        assertEquals(response[1], "foo");
//    }

    public void testStringArray() throws AxisFault {
        configureSystem("handleStringArray");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        ArrayList args = new ArrayList();
        String [] values = new String[] { "abc", "cde", "efg" };
        args.add(values);
        ArrayList ret = new ArrayList();
        ret.add(Boolean.class);
        Object [] objs = sender.invokeBlocking(operationName, args.toArray(),
                                               (Class[])ret.toArray(new Class[ret.size()]));
        assertNotNull(objs);
        assertEquals(Boolean.TRUE, Boolean.valueOf(objs[0].toString()));
    }

    public void testmultiArrays() throws AxisFault {
        configureSystem("multiArrays");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        ArrayList args = new ArrayList();
        String [] values = new String[] { "abc", "cde", "efg" };
        args.add(values);

        String [] values2 = new String[] { "abc", "cde", "efg" };
        args.add(values2);
        String [] values3 = new String[] { "abc", "cde", "efg" };
        args.add(values3);
        args.add("10");

        ArrayList ret = new ArrayList();
        ret.add(Integer.class);
        Object [] objs = sender.invokeBlocking(operationName, args.toArray(),
                                               (Class[])ret.toArray(new Class[ret.size()]));
        assertNotNull(objs);
        assertEquals(19, Integer.parseInt(objs[0].toString()));
    }

    public void testmulReturn() throws AxisFault {
        configureSystem("mulReturn");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        ArrayList args = new ArrayList();
        args.add("foo");


        OMElement element = sender.invokeBlocking(operationName, args.toArray());
        System.out.println("element = " + element);
//        assertEquals(response.getFirstElement().getText(), "foo");
    }


    public void testhandleArrayList() throws AxisFault {
        configureSystem("handleArrayList");

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        OMElement elem = sender.sendReceive(getpayLoad());
        assertEquals(elem.getFirstElement().getText(), "abcdefghiklm10");
    }

    public void testomElementArray() throws AxisFault {
        configureSystem("omElementArray");
        String str = "<req:omElementArray xmlns:req=\"http://rpc.axis2.apache.org/xsd\">\n" +
                "    <arg0><abc>vaue1</abc></arg0>\n" +
                "    <arg0><abc>vaue2</abc></arg0>\n" +
                "    <arg0><abc>vaue3</abc></arg0>\n" +
                "    <arg0><abc>vaue4</abc></arg0>\n" +
                "</req:omElementArray>";
        StAXOMBuilder staxOMBuilder;
        try {
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new
                    ByteArrayInputStream(str.getBytes()));
            OMFactory fac = OMAbstractFactory.getOMFactory();

            staxOMBuilder = new
                    StAXOMBuilder(fac, xmlReader);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        } catch (FactoryConfigurationError factoryConfigurationError) {
            throw new AxisFault(factoryConfigurationError);
        }
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        RPCServiceClient sender = new RPCServiceClient(configContext, null);
        sender.setOptions(options);

        OMElement elem = sender.sendReceive(staxOMBuilder.getDocumentElement());
        assertEquals("4", elem.getFirstElement().getText());
    }

    private OMElement getpayLoad() throws AxisFault {
        String str = "<req:handleArrayList xmlns:req=\"http://rpc.axis2.apache.org/xsd\">\n" +
                "  <arg0>\n" +
                "    <item0>abc</item0>\n" +
                "    <item0>def</item0>\n" +
                "    <item0>ghi</item0>\n" +
                "    <item0>klm</item0>\n" +
                "  </arg0><arg1>10</arg1>" +
                "</req:handleArrayList>";
        StAXOMBuilder staxOMBuilder;
        try {
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new
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
