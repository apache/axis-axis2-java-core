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

package org.apache.axis2.fastinfoset;

import java.io.InputStream;

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMNodeImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.fastinfoset.stax.StAXDocumentParser;

/**
 * @author Sanjaya Karunasena
 * @date May 19, 2007
 */

public class FastInfosetPOXBuilder implements Builder {

	private Log logger = LogFactory.getLog(FastInfosetBuilder.class);
	
	/**
	 * Returns a OMElement handler to the document element of the Fast Infoset message.
	 * 
	 * @param inputStream InputStream to the message
	 * @param contentType Content type of the message
	 * @param messageContext MessageContext to be used
	 * 
	 * @return OMElement handler to the document element
	 * 
	 * @see org.apache.axis2.builder.Builder#processDocument(InputStream, String, MessageContext)
	 */
	public OMElement processDocument(InputStream inputStream, String contentType, 
			MessageContext messageContext) throws AxisFault {
		if (logger.isDebugEnabled()) {
			logger.debug("Processing a Document with the content type: " + contentType);
		}
		XMLStreamReader streamReader = new StAXDocumentParser(inputStream);
		StAXBuilder builder = new StAXOMBuilder(streamReader);
        //TODO Check whether we need to perform any validations here...
        OMElement element = builder.getDocumentElement();
//        element = (OMElement) element.detach();
//        ((OMNodeImpl) element).setParent(null);
        return element;
	}
}
