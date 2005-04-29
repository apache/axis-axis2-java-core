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
 
package org.apache.axis.encoding;

import java.lang.reflect.Method;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.*;
import org.apache.axis.testUtils.SimpleTypeEncodingUtils;


public class EncodingTest extends TestCase {
     OMFactory fac;

    /**
     * @param testName
     */
    public EncodingTest(String testName) {
        super(testName);
        fac = OMAbstractFactory.getOMFactory();
    }

    public void testDeserializingInt() throws SecurityException, NoSuchMethodException, AxisFault {
        Method method = Echo.class.getMethod("echoInt", new Class[]{int.class});

        OMNamespace omNs = fac.createOMNamespace("http://host/my", "my");
        OMElement omel = fac.createOMElement("value", omNs);
        omel.addChild(fac.createText("1234"));

        deserialize(method, omel.getXMLStreamReader());
    }


    public void testDeserializingString() throws SecurityException, NoSuchMethodException, AxisFault {
        Method method = Echo.class.getMethod("echoInt", new Class[]{int.class});
        OMNamespace omNs = fac.createOMNamespace("http://host/my", "my");
        OMElement omel = fac.createOMElement("value", omNs);
        omel.addChild(fac.createText("1234"));

        deserialize(method, omel.getXMLStreamReader());
    }


    public void testDeserializingStringArray() throws SecurityException, NoSuchMethodException, AxisFault {
        Method method = Echo.class.getMethod("echoStringArray", new Class[]{String[].class});
        OMNamespace omNs = fac.createOMNamespace("http://host/my", "my");
        OMElement omel = fac.createOMElement("Array", omNs);

        for (int i = 0; i < 5; i++) {
            OMElement temp = fac.createOMElement("val", omNs);
            temp.addChild(fac.createText(String.valueOf(i)));
            omel.addChild(temp);
        }

        deserialize(method, omel.getXMLStreamReader());
    }

    public void testDeserializingStringArrayVal() throws SecurityException, NoSuchMethodException, AxisFault, XMLStreamException, FactoryConfigurationError {
        OMNamespace omNs = fac.createOMNamespace("http://host/my", "my");
        OMElement omel = fac.createOMElement("Array", omNs);

        for (int i = 0; i < 5; i++) {
            OMElement temp = fac.createOMElement("val", omNs);
            temp.addChild(fac.createText(String.valueOf(i)));
            omel.addChild(temp);
        }

        omel.serializeWithCache(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
        XMLStreamReader xpp = omel.getXMLStreamReader();
        String[] strs = SimpleTypeEncodingUtils.deserializeStringArray(xpp);
        for (int i = 0; i < strs.length; i++) {
            System.out.println(strs[i]);
        }

    }

    public void deserialize(Method method, XMLStreamReader xpp) throws AxisFault {
        Class[] parms = method.getParameterTypes();
        Object[] objs = new Object[parms.length];

        for (int i = 0; i < parms.length; i++) {
            if (int.class.equals(parms[i])) {
                objs[i] =
                        new Integer(SimpleTypeEncodingUtils.deserializeInt(xpp));
            } else if (String.class.equals(parms[i])) {
                objs[i] = SimpleTypeEncodingUtils.deserializeString(xpp);
            } else if (String[].class.equals(parms[i])) {
                objs[i] = SimpleTypeEncodingUtils.deserializeStringArray(xpp);
            } else {
                throw new UnsupportedOperationException("Only int,String and String[] is supported yet");
            }
        }

    }


    public class Echo {
        public int echoInt(int intVal) {
            return intVal;
        }

        public String[] echoStringArray(String[] in) {
            return in;
        }
    }

}
