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

package javax.xml.soap;

import org.w3c.dom.NodeList;

import javax.xml.transform.dom.DOMResult;

public class SAAJResult extends DOMResult {

    public SAAJResult()
            throws SOAPException {
        this(MessageFactory.newInstance().createMessage());
        org.w3c.dom.Node node = this.getNode();
        NodeList nodeList = node.getChildNodes();
        if (nodeList != null) {
            int size = nodeList.getLength();
            for (int a = 0; a < size; a++) {
                node.removeChild(nodeList.item(a));
            }
        }
        this.setNode(null);
    }

    public SAAJResult(String s)
            throws SOAPException {
        this(MessageFactory.newInstance(s).createMessage());
    }

    public SAAJResult(SOAPMessage soapmessage) {
        super(soapmessage.getSOAPPart());
    }

    public SAAJResult(SOAPElement soapelement) {
        super(soapelement);
    }

    public javax.xml.soap.Node getResult() {
        org.w3c.dom.Node node = super.getNode();
        //When using SAAJResult saajResult = new SAAJResult();
        if (node == null) {
            return null;
        }
        if (node instanceof SOAPPart) {
            try {
                return ((SOAPPart)node).getEnvelope();
            } catch (SOAPException e) {
                throw new RuntimeException(e);
            }
        }
        return (javax.xml.soap.Node)node.getFirstChild();
    }
}
