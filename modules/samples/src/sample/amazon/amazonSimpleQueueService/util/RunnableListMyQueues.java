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

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.clientapi.Callback;
import org.apache.axis2.engine.AxisFault;
import org.apache.axis2.om.OMElement;
import sample.amazon.amazonSimpleQueueService.OMElementCreator;

import javax.swing.*;

/**
 * This will create the Excutable code which runs seperately of GUI interations
 *
 * @author Saminda Abeyruwan <saminda@opensource.lk>
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
        OMElement listMyQueuesElement = OMElementCreator.queueListElement(getKey());
        this.axis2EngineRuns("ListMyQueues", listMyQueuesElement,
                             new SimpleQueueListMyQueuesCallbackHandler(this.createQueue, this.queueCode,
                                                                        this.read, this.result, this.button));
    }

    private void axis2EngineRuns(String operation, OMElement element,
                                 Callback specificCallbackObject) {
        //endpoint uri is hard coded....
        String url = "http://webservices.amazon.com/onca/soap?Service=AWSSimpleQueueService";
        try {
            Call call = new Call();
            call.setTo(new EndpointReference(AddressingConstants.WSA_TO, url));
            call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
            call.invokeNonBlocking(operation, element, specificCallbackObject);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
