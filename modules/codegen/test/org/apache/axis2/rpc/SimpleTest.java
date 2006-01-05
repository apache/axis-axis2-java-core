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

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.DeserializationContext;
import org.apache.axis2.databinding.deserializers.SimpleDeserializerFactory;
import org.apache.axis2.databinding.serializers.CollectionSerializer;
import org.apache.axis2.databinding.serializers.SimpleSerializer;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.transport.local.LocalTransportReceiver;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;

/**
 * SimpleTest
 */
public class SimpleTest extends TestCase {

    /**
     * Here's our test service which we'll be calling with the local transport
     */
    public static class Test {
        TestBean echoBean(TestBean bean) {
            TestBean res = new TestBean();
            res.setField1(bean.getField1() + " --> " + bean.getField2());
            return res;
        }

        String testTwoParams(String param1, int param2) {
            return param1 + " --> " + param2;
        }

        String [] echoArray(String [] string) {
            return string;
        }
    }

    /**
     * Here's a test JavaBean we'll be serializing/deserializing
     */
    public static class TestBean {
        private String field1;
        private int field2;

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public int getField2() {
            return field2;
        }

        public void setField2(int field2) {
            this.field2 = field2;
        }
    }

    RPCMethod method;

    /**
     * Get everything (metadata, engine) set up
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        // Set up method/param metadata
        AxisService service;
        String methodName = "echoArray";
        method = new RPCMethod(new QName(methodName));
        method.setResponseQName(new QName(methodName + "Response"));

        // Find the right Java method - this will be done by an introspector
        // class which syncs an entire service description, but for now just
        // do it manually...
        Method [] methods = Test.class.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method1 = methods[i];
            if (method1.getName().equals(methodName)) {
                method.setJavaMethod(method1);
                break;
            }
        }

        RPCParameter param = new RPCParameter();

        param.setQName(new QName("string"));
        param.setDeserializerFactory(new SimpleDeserializerFactory(String.class,
                new QName("xsd", "string")));
        param.setSerializer(new CollectionSerializer(new QName("string"), false, new SimpleSerializer()));
        param.setMaxOccurs(-1);
        param.setDestClass(String [].class);
        method.addParameter(param);

        param = new RPCParameter();
        param.setQName(new QName("return"));
        param.setDeserializerFactory(new SimpleDeserializerFactory(String.class,
                new QName("xsd", "string")));
        param.setSerializer(new CollectionSerializer(new QName("return"),
                false,
                new SimpleSerializer()));
        param.setMode(RPCParameter.MODE_OUT);
        param.setMaxOccurs(-1);
        param.setDestClass(String [].class);
        method.addParameter(param);

        // Set up Axis configuration

        AxisConfiguration config = new AxisConfiguration();
        TransportInDescription tIn = new TransportInDescription(new QName(Constants.TRANSPORT_LOCAL));
        config.addTransportIn(tIn);

        TransportOutDescription tOut = new TransportOutDescription(new QName(Constants.TRANSPORT_LOCAL));
        config.addTransportOut(tOut);


        LocalTransportReceiver.CONFIG_CONTEXT = new ConfigurationContext(config);

        service = new AxisService("testService");
        service.addParameter(
                new ParameterImpl(AbstractMessageReceiver.SERVICE_CLASS,
                        Test.class.getName()));
        AxisOperation axisOperation = new InOutAxisOperation(new QName(methodName));
        axisOperation.setMessageExchangePattern(WSDLConstants.MEP_URI_IN_OUT);
        axisOperation.setMessageReceiver(new RPCInOutMessageReceiver());
        service.addOperation(axisOperation);
        Parameter parameter = new ParameterImpl();
        parameter.setName(RPCInOutMessageReceiver.RPCMETHOD_PROPERTY);
        parameter.setValue(method);
        axisOperation.addParameter(parameter);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        LocalTransportReceiver.CONFIG_CONTEXT.getAxisConfiguration()
                .addService(service);
    }

    public void testRPC() throws Exception {
//        Call call = new Call("test-resources/xmls");
        ConfigurationContext configcontext = new ConfigurationContextFactory()
                .createConfigurationContextFromFileSystem("test-resources/xmls");
        ServiceClient client = new ServiceClient(configcontext, null);
//        Call call = new Call(".");

        // Make the SOAP envelope
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = factory.createSOAPEnvelope();
        factory.createSOAPHeader(env);
        SOAPBody body = factory.createSOAPBody(env);

        // Now set up an RPCRequestElement containing the metadat for our
        // method and an actual set of instance values to serialize.
        RPCValues values = new RPCValues();
        String [] array = new String []{"one", "two"};
        values.setValue(new QName("string"), array);
        new RPCRequestElement(method, values, body);

        // Ready to go - set the To address and make the call
        Options options = new Options();
        client.setOptions(options);
        OperationClient opClient = client.createClient(ServiceClient.ANON_OUT_IN_OP);
        opClient.setOptions(options);
        MessageContext msgConetxt = new MessageContext(configcontext);
        options.setTo(new EndpointReference("local://services/testService"));

        msgConetxt.setEnvelope(env);
        opClient.addMessageContext(msgConetxt);
        opClient.execute(true);
        MessageContext resMsgCtx = opClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

        SOAPEnvelope respEnv = resMsgCtx.getEnvelope();
        assertNotNull("No response envelope!", respEnv);

        RPCInOutMessageReceiver receiver = new RPCInOutMessageReceiver();
        // Got a response envelope, let's deserialize it back to Java
        DeserializationContext dserContext = new DeserializationContext();
        values = receiver.deserializeRPCElement(dserContext, method, respEnv.getBody().getFirstElement());

        Object ret = method.getResponseParameter().getValue(values);
        assertNotNull("No return parameter value", ret);
        assertTrue("Return wasn't a String []", ret instanceof String []);
        String [] retArray = (String[]) ret;
        assertEquals("Array was wrong size", 2, retArray.length);
        for (int i = 0; i < 2; i++) {
            assertEquals("Value #" + i + " not correct!", array[i], retArray[i]);
        }
    }
}
