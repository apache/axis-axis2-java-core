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
package org.apache.axis.message;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;

/**
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class SOAPHeaders {
    private HashMap headermap = new HashMap();
    /**
     * @param headers .. the OM element corresposds to the <Header>...</Header>
     */
    public SOAPHeaders(OMElement headers)throws AxisFault{
        Iterator childeren = headers.getChildren();
        
        while(childeren.hasNext()){
            OMNode node = (OMNode)childeren.next();
              
            //TODO
            if(node == null){
                System.out.println("Why the some nodes are null :( :( :(");
                continue; 
            }
            
            if(node.getType() == OMNode.ELEMENT_NODE){
                OMElement headerElement = (OMElement)node;
                
                OMNamespace omns = headerElement.getNamespace();
                if(omns != null){
                    String ns = omns.getValue();
                    if(ns != null){
                        QName name = new QName(ns,headerElement.getLocalName()); 
                        SOAPHeader header = new SOAPHeader(headerElement);
                        headermap.put(name,header);
                    }
                }else{
                    throw new AxisFault("SOAP Header must be NS Qualified");                
                }
            } 
        }

    }
    
    public SOAPHeader getHeader(QName name){
        return (SOAPHeader)headermap.get(name); 
    }

}
