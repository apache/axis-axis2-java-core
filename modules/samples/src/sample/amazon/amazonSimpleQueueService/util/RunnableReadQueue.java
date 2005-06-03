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

import javax.swing.*;

import org.apache.axis.om.OMElement;
import org.apache.axis.clientapi.Callback;
import org.apache.axis.clientapi.Call;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.Constants;
import sample.amazon.amazonSimpleQueueService.Read;
import sample.amazon.amazonSimpleQueueService.Read;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * This will create the Excutable code which runs seperately of GUI interations
 *
 * @author Saminda Abeyruwan <saminda@opensource.lk>
 */
public class RunnableReadQueue extends QueueManager implements Runnable {
    JTextField createQueue;
    JTextArea result;
    JTextField queueCode;
    JTextField read;

    public RunnableReadQueue(JTextField createQueue, JTextField queueCode, JTextField read,
                             JTextArea result) {
        this.createQueue = createQueue;
        this.queueCode = queueCode;
        this.read = read;
        this.result = result;
    }

    public void run() {
        OMElement readQueueElement = Read.read(this.createQueue.getText(),getKeyFromPropertyFile());
        this.axis2EngineRuns("Read", readQueueElement,
                new SimpleQueueReadCallbackHandler(this.queueCode, this.result));
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
