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
package org.apache.axis2.saaj.integration;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.impl.MTOMConstants;
import org.apache.axis2.om.impl.llom.OMTextImpl;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.attachments.MIMEHelper;

import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import javax.activation.DataHandler;

/**
 * 
 */
public class EchoService {
     public OMElement echo(OMElement element) throws XMLStreamException {
        System.err.println("################ Echo Service was called, Element=" + element);

        //Praparing the OMElement so that it can be attached to another OM Tree.
        //First the OMElement should be completely built in case it is not fully built and still
        //some of the xml is in the stream.
        element.build();
        //Secondly the OMElement should be detached from the current OMTree so that it can be attached
        //some other OM Tree. Once detached the OmTree will remove its connections to this OMElement.
//        element.detach();
        return element;
    }


}
