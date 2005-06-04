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
 *
 * @author Saminda Abeyruwan <saminda@opensource.lk>
 */
public class ListMyQueues {
    public static OMElement queueListElement(String key) {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        OMNamespace opN = factory.createOMNamespace(
                "http://webservices.amazon.com/AWSSimpleQueueService/2005-01-01",
                "nsQ");
        OMElement listMyQueues = factory.createOMElement("ListMyQueues", opN);
        OMElement subID = factory.createOMElement("SubscriptionId", opN);
        OMElement request = factory.createOMElement("Request", opN);
        subID.addChild(factory.createText(key));
        listMyQueues.addChild(subID);
        listMyQueues.addChild(request);
        return listMyQueues;
    }
}
