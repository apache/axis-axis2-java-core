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
public class SimpleQueueEnqueueCallbackHandler extends Callback {
    private static String returnString = "";
    private JTextField createQueue;
    private JTextArea result;
    private JTextField queueCode;
    private JTextField enqueue;

    public SimpleQueueEnqueueCallbackHandler() {
    }//defaultConstructor

    public SimpleQueueEnqueueCallbackHandler(JTextField createQueue,
                                             JTextField queueCode,
                                             JTextField enqueue,
                                             JTextArea result) {
        super();
        this.createQueue = createQueue;
        this.queueCode = queueCode;
        this.enqueue = enqueue;
        this.result = result;
    }

    public void onComplete(AsyncResult result) {
        SOAPBody body = result.getResponseEnvelope().getBody();
        getResults(body);
    }

    public void onError(Exception e) {

    }

    private boolean getResults(OMElement element) {
        Iterator iterator = element.getChildren();
        while (iterator.hasNext()) {
            OMNode omNode = (OMNode) iterator.next();
            if (omNode.getType() == OMNode.ELEMENT_NODE) {
                OMElement omElement = (OMElement) omNode;
                if (omElement.getLocalName().equals("Status")) {
                    this.getText(omElement);
                } else {
                    getResults(omElement);
                }
            }
        }
        return false;
    }

    public static String getReturnString() {
        return returnString;
    }

    private void getText(OMElement element) {
        if (element.getLocalName().equals("Status")) {
            this.result.setText(element.getText() + ".......");
            if (element.getText().equals("Success")) {
                returnString = returnString + "Successfully Enqueued.." +
                        "[" + this.enqueue.getText() + "]" + "..Queue.." + "["
                        + this.createQueue.getText()
                        + "]"
                        + "\n";
                this.result.setText(returnString);
                this.enqueue.setText("");
            }
        }
    }
}
