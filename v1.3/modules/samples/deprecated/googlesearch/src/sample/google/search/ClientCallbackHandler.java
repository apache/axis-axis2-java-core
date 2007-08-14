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

package sample.google.search;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;

import java.util.Iterator;

/**
 * This class implements the onComplete method extended by call back
 * receives the Response
 * process the soap with OM to extract the data
 * Find the <NavigationURL> element and get the text from it
 */
public class ClientCallbackHandler extends Callback {

    /**
     * HTML Header to desplay snippet text
     */
    private String beginHTML = "<HTML><HEAD><TITLE>Wow</TITLE></HEAD><BODY>";

    /**
     * HTML footer
     */
    private String endHTML = "</BODY></HTML>";

    /**
     * Store the texts read by NavigationURL of soap
     */
    private String snippet = beginHTML;

    /**
     * Store the URLs read by snippet element of soap
     */
    private String strURL;

    /**
     * Store texts temperaly
     */
    private String tempStr;


    private GUIHandler handler;

    public ClientCallbackHandler(GUIHandler handler) {
        this.handler = handler;
    }

    /**
     * method onComplete
     *
     * @param result
     */

    public void onComplete(AsyncResult result) {
        extractDetails(result);
    }

    /**
     * method extractDetails
     * Go through the children of soap
     * stores requres information in variables
     * Call to desplay the results
     *
     * @param result
     */
    private void extractDetails(AsyncResult result) {
        Iterator iterator, iterator2;
        OMNode node;
        SOAPBody body;
        OMElement operation, elem;
        SOAPEnvelope resEnvelope;
        resEnvelope = result.getResponseEnvelope();
        body = resEnvelope.getBody();
        operation = body.getFirstElement();
        if (body.hasFault()) {
            snippet =
                    snippet +
                    "A Fault message received, Check your Licence key. Else you have reached the" +
                    " daily limit of 1000 requests";
        } else {
            OMElement part = operation.getFirstElement();

            iterator = part.getChildren();
            while (iterator.hasNext()) {
                node = (OMNode) iterator.next();
                if (node.getType() == OMNode.ELEMENT_NODE) {
                    elem = (OMElement) node;
                    String str = elem.getLocalName();
                    //System.out.println(str);
                    if (str.equals("resultElements")) {
                        //System.out.println("Got the Result Elements");
                        Iterator iterator0 = elem.getChildren();
                        while (iterator0.hasNext()) {
                            node = (OMNode) iterator0.next();
                            if (node.getType() == OMNode.ELEMENT_NODE) {
                                elem = (OMElement) node;
                                if (elem.getLocalName().equals("item")) {
                                    iterator2 = elem.getChildren();
                                    while (iterator2.hasNext()) {
                                        node = (OMNode) iterator2.next();
                                        if (node.getType() ==
                                                OMNode.ELEMENT_NODE) {
                                            elem = (OMElement) node;
                                            String str3 = elem.getLocalName();
                                            if (elem.getLocalName().equals(
                                                    "snippet")) {
                                                //System.out.println("Got the snippet");
                                                tempStr = elem.getText();

                                                //System.out.println(tempStr);
                                                snippet = snippet + tempStr;
                                            }

                                            if (elem.getLocalName().equals(
                                                    "URL")) {
                                                //System.out.println("Got the URL");
                                                strURL = elem.getText();
                                            }
                                        }
                                    }
                                }
                                snippet = snippet + "<br> URL:-<a href=" +
                                        strURL +
                                        ">" +
                                        strURL +
                                        "</a\n\n> <br><br>";
                            }
                        }
                    }
                }
            }
        }
        snippet = snippet + endHTML;
        this.handler.showResults(snippet);
    }

    public void onError(Exception e) {
        e.printStackTrace();
    }
}