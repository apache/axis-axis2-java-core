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
package org.apache.wsdl.util;

import org.apache.wsdl.WSDLTypes;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class Utils {
    public static Document newDocument(InputStream in) throws ParserConfigurationException, SAXException, IOException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(in);
    }
    
    public static WSDLTypes buildWSDL2ComplientMessageType(Message message){
    	Iterator messageTypeIterator = message.getParts().values().iterator();
    	if(1 == message.getParts().size() && messageTypeIterator.hasNext()){
    		Part part = (Part)messageTypeIterator.next();
    		/// new WSDLTypesImpl().set
    	}
    	return null;
    }
}
