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
package sample.amazon.amazonSimpleQueueService;

import org.apache.axis.om.*;
import org.apache.axis.soap.SOAPFactory;

/**
 * This will create the OMElement needed to be used in invokeNonBlocking() method
 *
 * @author Saminda Abeyruwan <saminda@opensource.lk>
 */
public class Enqueue {
    public static OMElement enqueueElement(String queueEntyBody, String queueIden, String key) {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        OMNamespace opN = factory.createOMNamespace(
                "http://webservices.amazon.com/AWSSimpleQueueService/2005-01-01", "nsQ");
        OMElement enque = factory.createOMElement("Enqueue", opN);
        OMElement subID = factory.createOMElement("SubscriptionId", opN);
        OMElement request = factory.createOMElement("Request", opN);
        //OMElement queueName = factory.createOMElement("QueueName",opN);
        OMElement queueID = factory.createOMElement("QueueId", opN);
        OMElement queueEntryBodies = factory.createOMElement("QueueEntryBodies", opN);
        OMElement queueEntryBody1 = factory.createOMElement("QueueEntryBody", opN);
        queueEntryBodies.addChild(queueEntryBody1);
        //request.addChild(queueName);
        request.addChild(queueID);
        request.addChild(queueEntryBodies);
        subID.addChild(factory.createText(key));
        //queueName.addChild(factory.createText("Test Queue LSF "));
        queueID.addChild(factory.createText(queueIden));
        queueEntryBody1.addChild(factory.createText(queueEntyBody));
        enque.addChild(subID);
        enque.addChild(request);
        return enque;
    }
}
