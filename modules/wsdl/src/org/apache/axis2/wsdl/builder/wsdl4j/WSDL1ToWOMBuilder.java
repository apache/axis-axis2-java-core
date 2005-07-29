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
package org.apache.axis2.wsdl.builder.wsdl4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilder;
import org.apache.axis2.wsdl.builder.WSDLComponentFactory;
import org.apache.axis2.wsdl.util.XMLUtils;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.impl.WSDLDescriptionImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author chathura@opensource.lk
 */
public class WSDL1ToWOMBuilder implements WOMBuilder {

	/**
	 * Buils a WOM and a WSDL4J object model from given the URI of the WSDL file and
	 * will be returned as a wrapper object WSDLVersionWrapper.
	 * @param in InputStream from which the WSDL document can be read in.
	 * @return WSDLVersionWrapper which contains both the WSDL 2.0 and WSDL 1.1 
	 * object models.
	 * @throws WSDLException
	 */
    public WSDLVersionWrapper build(InputStream in) throws WSDLException {
    	return build(in, null);
    }
    
    /**
	 * Buils a WOM and a WSDL4J object model from given the URI of the WSDL file and
	 * will be returned as a wrapper object WSDLVersionWrapper. A WSDL Component Factory
	 * can be passed into the builder using which the WOM component can be built out of.
	 * For example: The Enigne uses the WOM's components in the context hierarchy but 
	 * those are extended components. 
	 * (<code>ServiceDescription</code> extends <code>WSDLService</code>.)
	 * So when deployment build the WOM it would prefer to get a <code>ServiceDescription</code>
	 * built in place of a <code>WSDLService</code>. This can be achieved by passing the 
	 * correct Component Factory that will instanciate the correct object for the WOM builder.
	 * @param in InputStream from which the WSDL document can be read in.
	 * @param wsdlComponentFactory The ComponentFactory that will be used to create the
	 * WOm components out of.
	 * @return WSDLVersionWrapper which contains both the WSDL 2.0 and WSDL 1.1 
	 * object models.
	 * @throws WSDLException
	 */
    public WSDLVersionWrapper build(InputStream in, 
    								WSDLComponentFactory wsdlComponentFactory) throws WSDLException {
    	if(null == wsdlComponentFactory){
    		wsdlComponentFactory = new WSDLDescriptionImpl();
    	}
    	WSDLDescription wsdlDescription = wsdlComponentFactory.createDescription();

        Definition wsdl1Definition = this.readInTheWSDLFile(in);
        WSDLPump pump = new WSDLPump(wsdlDescription, wsdl1Definition);
        pump.pump();

        return new WSDLVersionWrapper(wsdlDescription, wsdl1Definition);
    }
    

    /**
	 * Buils a WOM and a WSDL4J object model from given the URI of the WSDL file and
	 * will be returned as a wrapper object WSDLVersionWrapper.
	 * @param uri URI pointing to the WSDL document.
	 * @return WSDLVersionWrapper which contains both the WSDL 2.0 and WSDL 1.1 
	 * object models.
	 * @throws WSDLException
	 */
     public WSDLVersionWrapper build(String uri) throws WSDLException {
        return build(uri, null);
    }

     /**
 	 * Buils a WOM and a WSDL4J object model from given the URI of the WSDL file and
 	 * will be returned as a wrapper object WSDLVersionWrapper. A WSDL Component Factory
 	 * can be passed into the builder using which the WOM component can be built out of.
 	 * For example: The Enigne uses the WOM's components in the context hierarchy but 
 	 * those are extended components. 
 	 * (<code>ServiceDescription</code> extends <code>WSDLService</code>.)
 	 * So when deployment build the WOM it would prefer to get a <code>ServiceDescription</code>
 	 * built in place of a <code>WSDLService</code>. This can be achieved by passing the 
 	 * correct Component Factory that will instanciate the correct object for the WOM builder.
 	 * @param uri URI pointing to the WSDL document.
 	 * @param wsdlComponentFactory The ComponentFactory that will be used to create the
 	 * WOm components out of.
 	 * @return WSDLVersionWrapper which contains both the WSDL 2.0 and WSDL 1.1 
 	 * object models.
 	 * @throws WSDLException
 	 */
    public WSDLVersionWrapper build(String uri,
                                    WSDLComponentFactory wsdlComponentFactory) throws WSDLException {
    	if(null == wsdlComponentFactory){
    		wsdlComponentFactory = new WSDLDescriptionImpl();
    	}
        WSDLDescription wsdlDescription = wsdlComponentFactory.createDescription();

        Definition wsdl1Definition = this.readInTheWSDLFile(uri);
        WSDLPump pump = new WSDLPump(wsdlDescription,
                wsdl1Definition,
                wsdlComponentFactory);
        pump.pump();

        return new WSDLVersionWrapper(wsdlDescription, wsdl1Definition);

    }

    private Definition readInTheWSDLFile(String uri) throws WSDLException {

        WSDLReader reader =
                WSDLFactory.newInstance().newWSDLReader();
        File file = new File(uri);
        String baseURI = file.getParentFile()!=null?file.getParentFile().toURI().toString():null;

        Document doc;
        try {
            doc = XMLUtils.newDocument(uri);
        } catch (ParserConfigurationException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser Configuration Error",
                    e);
        } catch (SAXException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser SAX Error",
                    e);

        } catch (IOException e) {
            throw new WSDLException(WSDLException.INVALID_WSDL, "IO Error", e);
        }

        return reader.readWSDL(baseURI, doc);
    }
    
    private Definition readInTheWSDLFile(InputStream in) throws WSDLException {
    	
        WSDLReader reader =
                WSDLFactory.newInstance().newWSDLReader();

        Document doc;
        try {
            doc = XMLUtils.newDocument(in);
        } catch (ParserConfigurationException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser Configuration Error",
                    e);
        } catch (SAXException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser SAX Error",
                    e);

        } catch (IOException e) {
            throw new WSDLException(WSDLException.INVALID_WSDL, "IO Error", e);
        }

        return reader.readWSDL(null, doc);
    }


}
