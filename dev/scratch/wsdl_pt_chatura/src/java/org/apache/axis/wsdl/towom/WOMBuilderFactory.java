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
package org.apache.axis.wsdl.towom;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.utils.XMLUtils;
import org.apache.axis.wsdl.WSDLException;
import org.apache.axis.wsdl.wom.WSDLConstants;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author chathura@opensource.lk
 *
 */
public class WOMBuilderFactory {

    private static final int WSDL11 = 1;
    private static final int wsdl20 = 2;
    
    
    
    public static WOMBuilder getBuilder(InputStream in) throws WSDLException{
        // Load the wsdl as a DOM
        Document doc;
        try{
            doc = XMLUtils.newDocument(in);
        }
        catch(ParserConfigurationException e){
            throw new WSDLException("Parser Configuration Exception", e);
        }
        catch(IOException e1){
            throw new WSDLException("WSDL Document read error", e1);
        }
        catch(SAXException e2){
            throw new WSDLException("Parser Exception", e2);
        }
        
        
        //Check the target namespace of the WSDL and determine the WSDL version.
        int version = getWSDLVersion(doc);
        
        if(version == WSDL11){
            return (WOMBuilder)new WSDL1ToWOMBuilder();
        }
        else if(version == wsdl20){
            return (WOMBuilder)new WSDL2ToWOMBuilder();
        }
        
        throw new WSDLException("Unable to Figure out the WSDL vesion of the Document");
    }
    /**
     * Will return an int that will represent the wsdl version and the int will correspond to the static 
     * variables defined in this class.
     * @param doc 
     * @return
     * @throws WSDLException If the version cannot be determined
     */
    private static int getWSDLVersion(Document doc) throws WSDLException{
        //TODO check weather the namespaces are correct and the / problem too
        if(WSDLConstants.WSDL2_0_NAMESPACE.equals(doc.getDocumentElement().getNamespaceURI())){
            return wsdl20;
        }else if(WSDLConstants.WSDL1_1_NAMESPACE.equals(doc.getDocumentElement().getNamespaceURI())){
            return WSDL11;
        }
        
        throw new WSDLException("Unable to Figure out the WSDL vesion of the Document");
    }
}
