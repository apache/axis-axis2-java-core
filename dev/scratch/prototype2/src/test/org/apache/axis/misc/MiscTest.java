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
package org.apache.axis.misc;

import java.lang.reflect.InvocationTargetException;

import javax.xml.namespace.QName;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.context.GlobalContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.impl.context.SimpleSessionContext;
import org.apache.axis.impl.providers.RawXMLProvider;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class MiscTest extends AbstractTestCase {

    /**
     * @param testName
     */
    public MiscTest(String testName) {
        super(testName);
    }
    
    public void testSessionContext(){
        SessionContext sc = new SimpleSessionContext();
        String key = "Hello";
        Object val  = new Object();
        sc.put(key,val);
        assertEquals(sc.get(key),val);
    }
    
    public void testGlobalContext(){
        GlobalContext gc = new GlobalContext(null);
        String key = "Hello";
        Object val  = new Object();
        gc.put(key,val);
        assertEquals(gc.get(key),val);
    }


    public void testAxisFault(){
        Exception e = new InvocationTargetException(new Exception());
        assertNotSame(AxisFault.makeFault(e),e);
    }
    
    public void testProviders(){
        RawXMLProvider xmlprovider = new RawXMLProvider();
        QName name = new QName("Hi","testing");
        xmlprovider.setName(name);
        assertEquals(xmlprovider.getName(),name);
    }
}
