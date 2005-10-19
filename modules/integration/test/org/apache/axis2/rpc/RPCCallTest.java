package org.apache.axis2.rpc;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.InOutOperationDescrition;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ParameterImpl;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.rpc.client.RPCCall;
import org.apache.axis2.rpc.receivers.BeanSerializer;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
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
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 13, 2005
 * Time: 1:51:02 PM
 */
public class RPCCallTest extends TestCase {

    private SimpleDateFormat zulu =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
       //  0123456789 0 123456789



    protected EndpointReference targetEPR =
            new EndpointReference("http://127.0.0.1:"
                    + (UtilServer.TESTING_PORT)
                    + "/axis/services/EchoXMLService/concat");
    protected Log log = LogFactory.getLog(getClass());
    protected QName serviceName = new QName("EchoXMLService");
    protected QName operationName = new QName("http://localhost/my","concat");
    protected QName transportName = new QName("http://localhost/my",
            "NullTransport");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected ServiceDescription service;

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
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
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

        OMElement response = call.invokeBlocking(operationName,args.toArray());
        MyBean resBean =(MyBean) new  BeanSerializer(MyBean.class,response).deserialize();
        assertNotNull(resBean);
        assertEquals(resBean.getAge(),159);
        call.close();
    }

    private void configureSystem(String opName) throws AxisFault {
        targetEPR =
                new EndpointReference("http://127.0.0.1:"
                    + (UtilServer.TESTING_PORT)
                        + "/axis/services/EchoXMLService/"+ opName);
        String className = "org.apache.axis2.rpc.RPCServiceClass";
        operationName = new QName("http://localhost/my",opName,"req");
        ServiceDescription service = new ServiceDescription(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter( new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS,
                className));
        OperationDescription axisOp = new InOutOperationDescrition(operationName);
        axisOp.setMessageReceiver(new RPCMessageReceiver());
        axisOp.setStyle(WSDLService.STYLE_RPC);
        service.addOperation(axisOp);
        UtilServer.deployService(service);
    }

    public void testEchoBean() throws AxisFault {
        configureSystem("echoBean");
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

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


        OMElement response = call.invokeBlocking(operationName,args.toArray());
        MyBean resBean =(MyBean) new  BeanSerializer(MyBean.class,response).deserialize();
        assertNotNull(resBean);
        assertEquals(resBean.getAge(),100);
        call.close();
    }


    public void testEchoString() throws AxisFault {
        configureSystem("echoString");
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

        ArrayList args = new ArrayList();
        args.add("foo");
        OMElement response = call.invokeBlocking(operationName,args.toArray());
        assertEquals(response.getFirstElement().getText(),"foo");
        call.close();
    }

    public void testEchoInt() throws AxisFault {
        configureSystem("echoInt");
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

        ArrayList args = new ArrayList();
        args.add("100");

        OMElement response = call.invokeBlocking(operationName,args.toArray());
        assertEquals(Integer.parseInt(response.getFirstElement().getText()),100);
        call.close();
    }

    public void testAdd() throws AxisFault {
        configureSystem("add");
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);
        ArrayList args = new ArrayList();
        args.add("100");
        args.add("200");

        OMElement response = call.invokeBlocking(operationName,args.toArray());
        assertEquals(Integer.parseInt(response.getFirstElement().getText()),300);
        call.close();
    }

    public void testDivide() throws AxisFault {
        configureSystem("divide");
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

        ArrayList args = new ArrayList();
        args.add("10");
        args.add("0");
        OMElement response = call.invokeBlocking(operationName,args.toArray());
        assertEquals(response.getFirstElement().getText(),"INF");
        call.close();
    }

    public void testEchoBool() throws AxisFault {
        configureSystem("echoBool");
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);


        ArrayList args = new ArrayList();
        args.add("true");

        OMElement response = call.invokeBlocking(operationName,args.toArray());
        assertEquals(Boolean.valueOf(response.getFirstElement().getText()).booleanValue(),true);
        call.close();
    }

    public void testEchoByte() throws AxisFault {
        configureSystem("echoByte");
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

        ArrayList args = new ArrayList();
        args.add("1");
        OMElement response = call.invokeBlocking(operationName,args.toArray());
        assertEquals(Byte.parseByte(response.getFirstElement().getText()),1);
        call.close();
    }

    public void testEchoOM() throws AxisFault {
        configureSystem("echoOM");
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

        ArrayList args = new ArrayList();
        args.add("1");
        OMElement response = call.invokeBlocking(operationName,args.toArray());
        assertEquals(Byte.parseByte(response.getFirstElement().getText()),1);
        call.close();
    }

    public void testCalender() throws AxisFault {
        zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
        configureSystem("echoCalander");
        RPCCall call =
                new RPCCall("target/test-resources/intregrationRepo");

        call.setTo(targetEPR);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP,
                false);

        ArrayList args = new ArrayList();
        Date    date = Calendar.getInstance().getTime();
        args.add(zulu.format(date));
        OMElement response = call.invokeBlocking(operationName,args.toArray());
        assertEquals(response.getFirstElement().getText(),zulu.format(date));
        call.close();
    }

}
