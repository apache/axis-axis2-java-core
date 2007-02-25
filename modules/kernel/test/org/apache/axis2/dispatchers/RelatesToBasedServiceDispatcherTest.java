/*
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
package org.apache.axis2.dispatchers;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.*;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;

public class RelatesToBasedServiceDispatcherTest extends TestCase {

    public void testFindService() throws AxisFault{
        
        MessageContext messageContext;
        
        AxisConfiguration ac = new AxisConfiguration();
        ConfigurationContext cc = new ConfigurationContext(ac);
        
        AxisService as1 = new AxisService("Service1");
        ServiceContext sc1 = new ServiceContext(as1, ContextFactory.createServiceGroupContext(cc,null));

        AxisService as2 = new AxisService("Service2");
        
        ServiceContext sc2 = new ServiceContext(as2, ContextFactory.createServiceGroupContext(cc,null));
        
        
        ac.addService(as1);
        ac.addService(as2);
        
        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        as1.addOperation(operation1);
        as2.addOperation(operation2);
        
        
        OperationContext oc1 = ContextFactory.createOperationContext(operation1,sc1);
        OperationContext oc2 = ContextFactory.createOperationContext(operation2,sc2);

        cc.registerOperationContext("urn:org.apache.axis2.dispatchers.messageid:123", oc1);
        cc.registerOperationContext("urn:org.apache.axis2.dispatchers.messageid:456", oc2);
        messageContext = ContextFactory.createMessageContext(cc);
        messageContext.addRelatesTo(new RelatesTo("urn:org.apache.axis2.dispatchers.messageid:456"));
        
        RelatesToBasedServiceDispatcher ruisd = new RelatesToBasedServiceDispatcher();
        ruisd.invoke(messageContext);
        
        assertEquals(as2, messageContext.getAxisService());
    }

}
