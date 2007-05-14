/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.rpclit.enumtype.tests;



import javax.xml.ws.Holder;

import org.apache.axis2.jaxws.rpclit.enumtype.sei.PortType;
import org.apache.axis2.jaxws.rpclit.enumtype.sei.Service;
import org.apache.axis2.jaxws.TestLogger;
import org.test.rpclit.schema.ElementString;

import junit.framework.TestCase;


public class RPCLitEnumTests extends TestCase {
    public void testEnumSimpleType(){
        TestLogger.logger.debug("------------------------------");
        TestLogger.logger.debug("Test : " + getName());
        try{
                Service service = new Service();
                PortType portType = service.getPort();
                Holder<ElementString> pString = new Holder<ElementString>(ElementString.A);
                portType.echoString(pString);
                ElementString es = pString.value;
            TestLogger.logger.debug("Response =" + es);
                System.out.print("---------------------------------");
        }catch(Exception e){
                e.printStackTrace();
                fail();
        }
    }
}
