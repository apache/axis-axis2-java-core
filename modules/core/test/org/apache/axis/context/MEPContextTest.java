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

import javax.xml.namespace.QName;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransportIn;
import org.apache.axis.description.AxisTransportOut;
import org.apache.axis.engine.AxisFault;

/**
 * @author chathura@opensource.lk
 *
 */
public class MEPContextTest extends AbstractTestCase {

	private SystemContext engineCtx = new SystemContext(null);

    public MEPContextTest(String arg0) {
        super(arg0);
    }
    //FIXME TODO Chathura
    public void testMEPfindingOnRelatesTO() throws Exception{
    	 
    	ServiceContext sessionContext = new ServiceContext(new AxisService(),new SystemContext(null));
		MessageContext messageContext1 = this.getBasicMessageContext();
    	
    	messageContext1.setMessageID(new Long(System.currentTimeMillis()).toString());
    	AxisOperation axisOperation = new AxisOperation(new QName("test"));
    	OperationContext operationContext1 = axisOperation.findOperationContext(messageContext1, sessionContext, true);
    	
    	MessageContext messageContext2 = this.getBasicMessageContext();
    	messageContext2.setMessageID(new Long(System.currentTimeMillis()).toString());
    	messageContext2.getMessageInformationHeaders().setRelatesTo(new RelatesTo(messageContext1.getMessageID()));
    	OperationContext operationContext2 = axisOperation.findOperationContext(messageContext2, sessionContext,  true);
    	assertEquals(operationContext1, operationContext2);
    }
    
    public MessageContext getBasicMessageContext() throws AxisFault{

    	return new MessageContext(null,new AxisTransportIn(new QName("axis")), new AxisTransportOut(new QName("axis")),engineCtx);

    }
    
}
