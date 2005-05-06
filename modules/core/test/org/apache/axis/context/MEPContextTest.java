/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.context;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisTransportIn;
import org.apache.axis.description.AxisTransportOut;
import org.apache.axis.engine.AxisFault;

/**
 * @author chathura@opensource.lk
 *
 */
public class MEPContextTest extends AbstractTestCase {

	private EngineContext engineCtx = new EngineContext(null);

    public MEPContextTest(String arg0) {
        super(arg0);
    }
    
    public void testMEPfindingOnRelatesTO() throws Exception{
    	MessageContext messageContext1 = this.getBasicMessageContext();
    	
    	messageContext1.setMessageID(new Long(System.currentTimeMillis()).toString());
    	AxisOperation axisOperation = new AxisOperation(new QName("test"));
    	OperationContext operationContext1 = axisOperation.findMEPContext(messageContext1, true);
    	
    	MessageContext messageContext2 = this.getBasicMessageContext();
    	messageContext2.setMessageID(new Long(System.currentTimeMillis()).toString());
    	messageContext2.getMessageInformationHeaders().setRelatesTo(new RelatesTo(messageContext1.getMessageID()));
    	OperationContext operationContext2 = axisOperation.findMEPContext(messageContext2, true);
    	assertEquals(operationContext1, operationContext2);
    }
    
    public MessageContext getBasicMessageContext() throws AxisFault{
    	return new MessageContext( new SessionContext() {
			/* (non-Javadoc)
			 * @see org.apache.axis.context.SessionContext#get(java.lang.Object)
			 */
			public Object get(Object key) {
				// TODO Auto-generated method stub
				return null;
			}

			/* (non-Javadoc)
			 * @see org.apache.axis.context.SessionContext#put(java.lang.Object, java.lang.Object)
			 */
			public void put(Object key, Object obj) {
				// TODO Auto-generated method stub

			}
		},new AxisTransportIn(new QName("axis")), new AxisTransportOut(new QName("axis")));
    }
    
}
