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
package org.apache.axis2.saaj;

import junit.framework.TestCase;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

/**
 * 
 */
public class SOAPMessageTest extends TestCase {
    private SOAPMessage msg;

    protected void setUp() throws Exception {
        msg = MessageFactory.newInstance().createMessage();
    }

    public void testSaveRequired() {
        try {
            assertTrue("Save Required is False",msg.saveRequired());
        } catch (Exception e) {
            fail("Unexpected Exception : " + e);
        }
    }

    public void testSaveRequired2() {
        try {
            msg.saveChanges();
            assertFalse("Save Required is True",msg.saveRequired());
        } catch (Exception e) {
            fail("Unexpected Exception : " + e);
        }
    }
}
