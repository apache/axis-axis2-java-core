/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.handlers;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Constants;
import org.apache.axis.engine.Service;
import org.apache.axis.engine.context.MessageContext;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMEnvelope;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class OpNameFinder extends AbstractHandler{
    public void invoke(MessageContext msgContext) throws AxisFault {
        int style = msgContext.getMessageStyle();
        if(Constants.SOAP_STYLE_RPC_ENCODED == style || style == Constants.SOAP_STYLE_RPC_LITERAL){
            OMEnvelope envelope = msgContext.getEnvelope();
            OMNode node = null;
            OMElement element = envelope.getBody();
            if(Constants.ELEM_BODY.equals(element.getLocalName())){
                Iterator bodychilderen = element.getChildren();
                while(bodychilderen.hasNext()){
                    node = (OMNode)bodychilderen.next();
                    
                    //TODO
                    if(node == null){
                        System.out.println("Why the some nodes are null :( :( :(");
                        continue; 
                    }
                    
                    if(node.getType() == OMNode.ELEMENT_NODE){
                        OMElement bodyChild  = (OMElement)node;
                    
                        OMNamespace omns = bodyChild.getNamespace();
    
                        if(omns != null){
                            String ns = omns.getValue();
                            if(ns != null){
                                QName opName = new QName(ns,bodyChild.getLocalName());
                                Service service = msgContext.getService();
                                msgContext.setOperation(service.getOperation(opName)); 
                            }
                        }else{
                            throw new AxisFault("SOAP Body must be NS Qualified");                            
                        }
                
                    }    
                }
            }
        }
    }
}
