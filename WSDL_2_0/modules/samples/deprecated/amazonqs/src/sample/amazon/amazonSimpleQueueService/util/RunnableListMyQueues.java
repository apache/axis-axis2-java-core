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

package sample.amazon.amazonSimpleQueueService.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.Callback;
import sample.amazon.amazonSimpleQueueService.OMElementCreator;

import javax.swing.*;

/**
 * This will create the Excutable code which runs separately of GUI interations
 */
public class RunnableListMyQueues extends QueueManager implements Runnable {
    JTextField createQueue;
    JTextArea result;
    JTextField queueCode;
    JTextField read;
    JButton button;

    public RunnableListMyQueues(JTextField createQueue, JTextField queueCode, JTextField read,
                                JTextArea result, JButton button) {
        this.createQueue = createQueue;
        this.queueCode = queueCode;
        this.read = read;
        this.result = result;
        this.button = button;
    }

    public void run() {
        OMElement listMyQueuesElement = OMElementCreator.queueListElement(
                getKey());
        this.axis2EngineRuns("ListMyQueues",
                listMyQueuesElement,
                new SimpleQueueListMyQueuesCallbackHandler(this.createQueue, this.queueCode,
                        this.read, this.result, this.button));
    }

    private void axis2EngineRuns(String operation, OMElement element,
                                 Callback specificCallbackObject) {
        //endpoint uri is hard coded....
        String url = "http://webservices.amazon.com/onca/soap?Service=AWSSimpleQueueService";
        try {
            Options options = new Options();
            options.setAction("http://soap.amazon.com");
            options.setTo(new EndpointReference(url));
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setProperty(
                    HTTPConstants.CHUNKED,
                    org.apache.axis2.Constants.VALUE_FALSE);
            ServiceClient sender = new ServiceClient();
            sender.setOptions(options);
            sender.sendReceiveNonBlocking(element, specificCallbackObject);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
