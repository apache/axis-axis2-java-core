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

package org.apache.rampart;

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.util.WSSecurityUtil;
import org.apache.xml.security.utils.EncryptionConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;

public class ValidatorData {

    private RampartMessageData rmd;
    ArrayList encryptedDataRefIds = new ArrayList();
    private String bodyEncrDataId;
    
    public ValidatorData(RampartMessageData rmd) {
        this.rmd = rmd;
        this.extractEncryptedPartInformation();
    }
    
    private void extractEncryptedPartInformation() {
        Node start = rmd.getDocument().getDocumentElement();
        while(start != null) {
            Element elem = (Element) WSSecurityUtil.findElement(start, 
                    EncryptionConstants._TAG_ENCRYPTEDDATA, WSConstants.ENC_NS);
            if(elem != null) {
                Element parentElem = (Element)elem.getParentNode();
                if(parentElem != null && parentElem.getLocalName().equals(SOAP11Constants.BODY_LOCAL_NAME) &&
                        parentElem.getNamespaceURI().equals(rmd.getSoapConstants().getEnvelopeURI())) {
                    this.bodyEncrDataId = elem.getAttribute("Id");
                } else {
                    encryptedDataRefIds.add(elem.getAttribute("Id"));
                }
                
                if(elem.getNextSibling() != null) {
                    start = elem.getNextSibling();
                } else {
                    start = elem.getParentNode().getNextSibling();
                }
            } else {
                if(start.getNextSibling() != null) {
                    start = start.getNextSibling();
                } else {
                    start = start.getParentNode().getNextSibling();
                }
            }
            
        }
        
    }

    public ArrayList getEncryptedDataRefIds() {
        return encryptedDataRefIds;
    }

    public RampartMessageData getRampartMessageData() {
        return rmd;
    }

    public String getBodyEncrDataId() {
        return bodyEncrDataId;
    }
    
}
