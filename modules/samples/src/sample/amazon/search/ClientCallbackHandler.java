/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package sample.amazon.search;

import java.util.Iterator;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.clientapi.AsyncResult;
import org.apache.axis2.clientapi.Callback;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements the onComplete method extended by call back
 * recieves the Response
 * process the soap with OM to extract the data
 * Find the <NavigationURL> element and get the text from it
 *
 * @auther Gayan Asanka  (gayan@opensource.lk)
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
     * Store the URLs read by soap
     */
    private String strURL = beginHTML;

    /**
     * Stores the response
     */
    private AsyncResult myResult;
    private Log log = LogFactory.getLog(getClass());

    /**
     * method onComplete
     *
     * @param result
     */
    public void onComplete(AsyncResult result) {
        log.info(
                "Responce message received to the ClientCallbackHandler ...");
        try {
            result.getResponseEnvelope().serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(
                            System.out));
        } catch (XMLStreamException e) {
            log.info("Error occured after responce is received");
            e.printStackTrace();
        } catch (FactoryConfigurationError e) {
            log.info("Error occured after responce is received");
            e.printStackTrace();
        }
        myResult = result;
        extractDetails(myResult);
    }

    /**
     * method extractDetails
     *
     * @param result
     */
    private void extractDetails(AsyncResult result) {
        Iterator iterator0, iterator1, iterator2, iterator3, iterator4;
        SOAPEnvelope resEnvilop;
        OMElement body;
        OMElement operation;
        OMNode node;
        OMElement elem;

        resEnvilop = result.getResponseEnvelope();
        body = resEnvilop.getBody();
        operation = body.getFirstElement();

        String opLocalName = operation.getLocalName();
        if (opLocalName.equals("Fault")) {
            log.info(
                    "A Fault message recieved, Check your Licence key");
            strURL =
                    strURL +
                    "A Fault message recieved, Check your Licence key. Else you have reached the " +
                    "daily limit of 1000 requests";
        } else {
            log.info("this is opera: " + operation.getLocalName());
            iterator0 = operation.getChildren();

            while (iterator0.hasNext()) {
                node = (OMNode) iterator0.next();
                if (node.getType() == OMNode.ELEMENT_NODE) {
                    elem = (OMElement) node;
                    String str = elem.getLocalName();
                    log.info(str);
                    if (str.equals("SearchResult")) {
                        log.info("Got Search Results");
                        iterator1 = elem.getChildren();
                        while (iterator1.hasNext()) {
                            node = (OMNode) iterator1.next();
                            if (node.getType() == OMNode.ELEMENT_NODE) {
                                elem = (OMElement) node;
                                String str1 = elem.getLocalName();
                                log.info(str1);
                                if (str1.equals("Alexa")) {
                                   log.info("Got Alexa");
                                    elem = elem.getFirstElement(); //elem -> websearch
                                    log.info("Should be WebSearch " +
                                            elem.getLocalName());
                                    iterator2 = elem.getChildren();
                                    while (iterator2.hasNext()) {
                                        node = (OMNode) iterator2.next();
                                        if (node.getType() ==
                                                OMNode.ELEMENT_NODE) {
                                            elem = (OMElement) node;
                                            String str3 = elem.getLocalName();
                                            if (str3.equals("Results")) {
                                                iterator3 = elem.getChildren();
                                                while (iterator3.hasNext()) {
                                                    node =
                                                            (OMNode) iterator3.next();
                                                    if (node.getType() ==
                                                            OMNode.ELEMENT_NODE) {
                                                        elem =
                                                                (OMElement) node;
                                                        String str4 = elem.getLocalName();
                                                        if (str4.equals(
                                                                "Result")) {
                                                            iterator4 =
                                                                    elem.getChildren();
                                                            while (iterator4.hasNext()) {
                                                                node =
                                                                        (OMNode) iterator4.next();
                                                                if (node.getType() ==
                                                                        OMNode.ELEMENT_NODE) {
                                                                    elem =
                                                                            (OMElement) node;
                                                                    String str5 = elem.getLocalName();
                                                                    System.out.println(
                                                                            str5);
                                                                    if (str5.equals(
                                                                            "NavigableUrl")) {
                                                                        String txt = elem.getText();
                                                                        strURL =
                                                                                strURL +
                                                                                "<a href= " +
                                                                                txt +
                                                                                ">" +
                                                                                txt +
                                                                                "</a><br>";
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Outside of loop");
        GUIHandler.showResults(strURL + endHTML);
    }

    public void reportError(Exception e) {
        System.out.println("Error occured after responce is received");
        e.printStackTrace();
    }
}