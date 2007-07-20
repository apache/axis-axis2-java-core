/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package sample.amazon.amazonSimpleQueueService.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;

import javax.swing.*;
import java.util.Iterator;

/**
 * Callback class which deals with the outcome of the operation
 */
public class SimpleQueueListMyQueuesCallbackHandler extends Callback {
    private String returnString = "Listing Available Queues......\n";
    private JTextField createQueue;
    private JTextArea result;
    private JTextField queueCode;
    private JTextField read;
    private JButton button;

    public SimpleQueueListMyQueuesCallbackHandler() {
    }//defaultConstructor

    public SimpleQueueListMyQueuesCallbackHandler(JTextField createQueue, JTextField queueCode,
                                                  JTextField read, JTextArea result,
                                                  JButton button) {
        super();
        this.createQueue = createQueue;
        this.queueCode = queueCode;
        this.read = read;
        this.result = result;
        this.button = button;
    }

    public void onComplete(AsyncResult result) {
        SOAPBody body = result.getResponseEnvelope().getBody();
        getResults(body);
        this.button.setText("Load Queue");
    }

    public void onError(Exception e) {

    }

    private boolean getResults(OMElement element) {
        Iterator iterator = element.getChildren();
        while (iterator.hasNext()) {
            OMNode omNode = (OMNode) iterator.next();
            if (omNode.getType() == OMNode.ELEMENT_NODE) {
                OMElement omElement = (OMElement) omNode;
                if ((omElement.getLocalName().equals("QueueId"))
                        || (omElement.getLocalName().equals("QueueName"))) {

                    if (omElement.getLocalName().equals("QueueId")) {
                        this.getText(omElement);
                    }
                    if (omElement.getLocalName().equals("QueueName")) {
                        this.getText(omElement);
                    }

                } else {
                    getResults(omElement);
                }
            }
        }
        return false;
    }

    public String getReturnString() {
        return this.returnString;
    }

    private void getText(OMElement element) {
        returnString = returnString + element.getText() + "\n";
        this.result.setText(returnString);
    }
}
