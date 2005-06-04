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

import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.soap.SOAPFactory;

/**
 * This will create the OMElement needed to be used in invokeNonBlocking() method
 * Reading from a give queue is done according to queue name. QueueId can also used.  
 *
 * @author Saminda Abeyruwan <saminda@opensource.lk>
 */
public class Read {
    public static OMElement read(String requiredQueueName,String key) {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        OMNamespace opN = factory.createOMNamespace(
                "http://webservices.amazon.com/AWSSimpleQueueService/2005-01-01", "nsQ");
        OMElement read = factory.createOMElement("Read", opN);
        OMElement subID = factory.createOMElement("SubscriptionId", opN);
        OMElement request = factory.createOMElement("Request", opN);
        OMElement queueName = factory.createOMElement("QueueName", opN);
        //OMElement queueID = factory.createOMElement("QueueId",opN);
        OMElement readCount = factory.createOMElement("ReadCount", opN);
        request.addChild(queueName);
        //request.addChild(queueID);
        request.addChild(readCount);
        subID.addChild(factory.createText(key));
        queueName.addChild(factory.createText(requiredQueueName));
        //queueID.addChild(factory.createText(queueIden));
        readCount.addChild(factory.createText("25"));
        read.addChild(subID);
        read.addChild(request);
        return read;
    }
}
