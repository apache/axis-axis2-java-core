/*
 * Copyright 2003,2004 The Apache Software Foundation.
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

package org.apache.axis.integration;

import junit.framework.TestCase;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.engine.EngineUtils;
import org.apache.axis.om.*;
import org.apache.axis.testUtils.*;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


public class EchoTest extends TestCase {
    private final OMFactory fac = OMFactory.newInstance();
    private final OMNamespace ns =
            fac.createOMNamespace("http://apache.ws.apache.org/samples", "samples");
    private final OMNamespace arrayNs =
            fac.createOMNamespace(OMConstants.ARRAY_ITEM_NSURI,
                    OMConstants.ARRAY_ITEM_NS_PREFIX);
    private final OMNamespace targetNs =
            fac.createOMNamespace("http://axis.apache.org", "s");
    private final URLClassLoader cl;

    public EchoTest() throws MalformedURLException {
        cl = new URLClassLoader(new URL[]{
            new File("target/test-resources/samples/services/echo.jar")
                .toURL()}, EchoTest.class.getClassLoader());
    }

    public EchoTest(String arg0) throws MalformedURLException {
        super(arg0);
        cl = new URLClassLoader(new URL[]{
            new File("target/test-resources/samples/services/echo.jar")
                .toURL()}, EchoTest.class.getClassLoader());

    }

    private SOAPEnvelope createRawMessage(String method,
                                          OMElement parameters) {
        SOAPEnvelope envelope = fac.getDefaultEnvelope();

        OMElement responseMethodName = fac.createOMElement(method, ns);
        envelope.getBody().addChild(responseMethodName);
        responseMethodName.addChild(parameters);
        return envelope;

    }

    private XMLStreamReader invokeTheService(SOAPEnvelope envelope)
            throws Exception {
        EndpointReference targetEPR =
                new EndpointReference(AddressingConstants.WSA_TO,
                        "http://127.0.0.1:"
                + (EngineUtils.TESTING_PORT)
                + "/axis/services/echo");
        Call call = new Call();
        call.setTo(targetEPR);
        call.setListenerTransport(Constants.TRANSPORT_HTTP,true);
        SOAPEnvelope responseEnv = call.sendReceive(envelope);

        SOAPBody body = responseEnv.getBody();
        if (body.hasFault()) {
            throw body.getFault().getException();
        }
        XMLStreamReader xpp = body.getPullParser(true);

        int event = xpp.next();
        while (event != XMLStreamConstants.START_ELEMENT) {
            event = xpp.next();
        }
        event = xpp.next();
        while (event != XMLStreamConstants.START_ELEMENT) {
            event = xpp.next();
        }
        event = xpp.next();
        while (event != XMLStreamConstants.START_ELEMENT) {
            event = xpp.next();
        }
        return xpp;
    }

    public void testEchoString() throws Exception {
        String message = "Hello testing";

        OMElement returnelement = fac.createOMElement("param1", ns);
        returnelement.setBuilder(new ObjectToOMBuilder(returnelement,
                new SimpleTypeEncoder(message)));
        returnelement.declareNamespace(arrayNs);
        SOAPEnvelope envelope = createRawMessage("echoString", returnelement);
        XMLStreamReader xpp = invokeTheService(envelope);
        String value = SimpleTypeEncodingUtils.deserializeString(xpp);
        assertEquals(value, message);
    }

    public void testEchoStringArray() throws Exception {
        String[] messages =
                new String[]{
                    "Hello testing1",
                    "Hello testing2",
                    "Hello testing3",
                    "Hello testing4",
                    "Hello testing5"};
        OMElement returnelement = fac.createOMElement("param1", ns);

        ObjectToOMBuilder builder =
                new ObjectToOMBuilder(returnelement,
                        new ArrayTypeEncoder(messages, new SimpleTypeEncoder(null)));

        returnelement.setBuilder(builder);
        returnelement.declareNamespace(arrayNs);
        SOAPEnvelope envelope = createRawMessage("echoStringArray", returnelement);

        XMLStreamReader xpp = invokeTheService(envelope);
        String[] values = SimpleTypeEncodingUtils.deserializeStringArray(xpp);
        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i], messages[i]);
        }
    }

    public void testEchoStruct() throws Exception {
        String[] messages =
                new String[]{
                    "Hello testing1",
                    "Hello testing2",
                    "Hello testing3",
                    "Hello testing4",
                    "Hello testing5"};
        Class clasname =
                Class.forName("EchoStruct", true, cl);
        Object obj = clasname.newInstance();

        Method method1 =
                clasname.getMethod("setValue1", new Class[]{String.class});
        method1.invoke(obj, new Object[]{"Ruy Lopez"});
        Method method2 =
                clasname.getMethod("setValue2", new Class[]{String.class});
        method2.invoke(obj, new Object[]{"Kings Gambit"});
        Method method3 =
                clasname.getMethod("setValue3", new Class[]{int.class});
        method3.invoke(obj, new Object[]{new Integer(345)});
        Method method4 =
                clasname.getMethod("setValue4", new Class[]{String.class});
        method4.invoke(obj, new Object[]{"Kings Indian Defence"});
        Method method5 =
                clasname.getMethod("setValue5", new Class[]{String.class});
        method5.invoke(obj, new Object[]{"Musio Gambit"});
        Method method6 =
                clasname.getMethod("setValue6", new Class[]{String.class});
        method6.invoke(obj, new Object[]{"Benko Gambit"});
        Method method7 =
                clasname.getMethod("setValue7", new Class[]{String.class});
        method7.invoke(obj, new Object[]{"Secillian Defance"});
        Method method8 =
                clasname.getMethod("setValue8", new Class[]{String.class});
        method8.invoke(obj, new Object[]{"Queens Gambit"});
        Method method9 =
                clasname.getMethod("setValue9", new Class[]{String.class});
        method9.invoke(obj, new Object[]{"Queens Indian Defense"});
        Method method10 =
                clasname.getMethod("setValue10", new Class[]{String.class});
        method10.invoke(obj, new Object[]{"Alekine's Defense"});
        Method method11 =
                clasname.getMethod("setValue11", new Class[]{String.class});
        method11.invoke(obj, new Object[]{"Perc Defense"});
        Method method12 =
                clasname.getMethod("setValue12", new Class[]{String.class});
        method12.invoke(obj, new Object[]{"Scotch Gambit"});
        Method method13 =
                clasname.getMethod("setValue13", new Class[]{String.class});
        method13.invoke(obj, new Object[]{"English Opening"});

        OMElement returnelement = fac.createOMElement("param1", ns);

        Class encoderClass =
                Class.forName("EchoStructEncoder",
                        true,
                        cl);
        Constructor constCt =
                encoderClass.getConstructor(new Class[]{clasname});
        Object obj1 = constCt.newInstance(new Object[]{obj});

        ObjectToOMBuilder builder =
                new ObjectToOMBuilder(returnelement, (Encoder) obj1);

        returnelement.setBuilder(builder);
        returnelement.declareNamespace(OMConstants.ARRAY_ITEM_NSURI,
                OMConstants.ARRAY_ITEM_NS_PREFIX);
        returnelement.declareNamespace(targetNs);

        SOAPEnvelope envelope =
                createRawMessage("echoEchoStruct", returnelement);

        XMLStreamReader xpp = invokeTheService(envelope);

        Method deserializeMethod =
                encoderClass.getMethod("deSerialize",
                        new Class[]{XMLStreamReader.class});
        Object result = deserializeMethod.invoke(obj1, new Object[]{xpp});
        assertTrue(result.equals(obj));
    }

    public void testEchoStructArray() throws Exception {
        Object[] objs = new Object[10];
        Class clasname =
                Class.forName("EchoStruct", true, cl);

        for (int i = 0; i < objs.length; i++) {

            objs[i] = clasname.newInstance();
            Method method1 =
                    clasname.getMethod("setValue1", new Class[]{String.class});
            method1.invoke(objs[i], new Object[]{"Ruy Lopez"});
            Method method2 =
                    clasname.getMethod("setValue2", new Class[]{String.class});
            method2.invoke(objs[i], new Object[]{"Kings Gambit"});
            Method method3 =
                    clasname.getMethod("setValue3", new Class[]{int.class});
            method3.invoke(objs[i], new Object[]{new Integer(345)});
            Method method4 =
                    clasname.getMethod("setValue4", new Class[]{String.class});
            method4.invoke(objs[i], new Object[]{"Kings Indian Defence"});
            Method method5 =
                    clasname.getMethod("setValue5", new Class[]{String.class});
            method5.invoke(objs[i], new Object[]{"Musio Gambit"});
            Method method6 =
                    clasname.getMethod("setValue6", new Class[]{String.class});
            method6.invoke(objs[i], new Object[]{"Benko Gambit"});
            Method method7 =
                    clasname.getMethod("setValue7", new Class[]{String.class});
            method7.invoke(objs[i], new Object[]{"Secillian Defance"});
            Method method8 =
                    clasname.getMethod("setValue8", new Class[]{String.class});
            method8.invoke(objs[i], new Object[]{"Queens Gambit"});
            Method method9 =
                    clasname.getMethod("setValue9", new Class[]{String.class});
            method9.invoke(objs[i], new Object[]{"Queens Indian Defense"});
            Method method10 =
                    clasname.getMethod("setValue10", new Class[]{String.class});
            method10.invoke(objs[i], new Object[]{"Alekine's Defense"});
            Method method11 =
                    clasname.getMethod("setValue11", new Class[]{String.class});
            method11.invoke(objs[i], new Object[]{"Perc Defense"});
            Method method12 =
                    clasname.getMethod("setValue12", new Class[]{String.class});
            method12.invoke(objs[i], new Object[]{"Scotch Gambit"});
            Method method13 =
                    clasname.getMethod("setValue13", new Class[]{String.class});
            method13.invoke(objs[i], new Object[]{"English Opening"});
        }

        OMElement returnelement = fac.createOMElement("param1", ns);

        Class encoderClass =
                Class.forName("EchoStructEncoder",
                        true,
                        cl);
        Constructor constCt =
                encoderClass.getConstructor(new Class[]{clasname});
        Object obj1 = constCt.newInstance(new Object[]{null});

        ObjectToOMBuilder builder =
                new ObjectToOMBuilder(returnelement,
                        new ArrayTypeEncoder(objs, (Encoder) obj1));

        returnelement.setBuilder(builder);
        returnelement.declareNamespace(arrayNs);
        returnelement.declareNamespace(targetNs);

        SOAPEnvelope envelope =
                createRawMessage("echoEchoStructArray", returnelement);

        XMLStreamReader xpp = invokeTheService(envelope);

        Encoder enc = new ArrayTypeEncoder(objs, (Encoder) obj1);

        Method deserializeMethod =
                encoderClass.getMethod("deSerialize",
                        new Class[]{XMLStreamReader.class});
        Object obj = enc.deSerialize(xpp);
        Object[] structs = (Object[]) obj;

        for (int i = 0; i < structs.length; i++) {
            assertTrue(structs[i].equals(objs[i]));

        }


    }

    protected void setUp() throws Exception {
        UtilServer.start();
    }

    protected void tearDown() throws Exception {
        UtilServer.stop();
    }
}
