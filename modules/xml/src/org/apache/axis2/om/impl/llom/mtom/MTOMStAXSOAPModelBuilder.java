/**  
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 */
package org.apache.axis2.om.impl.llom.mtom;

import org.apache.axis2.attachments.MIMEHelper;
import org.apache.axis2.om.*;
import org.apache.axis2.om.impl.llom.OMTextImpl;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.stream.XMLStreamReader;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 */

public class MTOMStAXSOAPModelBuilder extends StAXSOAPModelBuilder {
	private Log log = LogFactory.getLog(getClass());
	
	/**
	 * <code>mimeHelper</code> handles deffered parsing of incoming MIME
	 * Messages
	 */
	MIMEHelper mimeHelper;
	
	int partIndex = 0;
	
	public MTOMStAXSOAPModelBuilder(XMLStreamReader parser,
			SOAPFactory factory, MIMEHelper mimeHelper) {
		super(parser, factory);
		this.mimeHelper = mimeHelper;
	}
	
	/**
	 * @param reader
	 * @param mimeHelper2
	 */
	public MTOMStAXSOAPModelBuilder(XMLStreamReader reader,
			MIMEHelper mimeHelper) {
		super(reader);
		this.mimeHelper = mimeHelper;
	}
	
	protected OMNode createOMElement() throws OMException {
		
		String elementName = parser.getLocalName();
		
		String namespaceURI = parser.getNamespaceURI();
		
		// create an OMBlob if the element is an <xop:Include>
		if (elementName.equalsIgnoreCase("Include")
				& namespaceURI
				.equalsIgnoreCase("http://www.w3.org/2004/08/xop/include")) {
			
			OMText node;
			String contentID = null;
			String contentIDName = null;
			OMAttribute Attr;
			if (lastNode == null) {
				// Decide whether to ckeck the level >3 or not
				throw new OMException(
				"XOP:Include element is not supported here");
			}
			if (parser.getAttributeCount() > 0) {
				contentID = parser.getAttributeValue(0);
				contentID = contentID.trim();
				contentIDName = parser.getAttributeLocalName(0);
				if (contentIDName.equalsIgnoreCase("href")
						& contentID.substring(0, 3).equalsIgnoreCase("cid")) {
					contentID = contentID.substring(4);
				} else {
					throw new OMException(
					"contentID not Found in XOP:Include element");
				}
			} else {
				throw new OMException(
				"Href attribute not found in XOP:Include element");
			}
			
			// This cannot happen. XOP:Include is always the only child of an parent element
			// cause it is same as having some text
			try{
				OMElement e = (OMElement) lastNode;
				node = new OMTextImpl(contentID, (OMElement) lastNode, this);
				e.setFirstChild(node);
			}catch(ClassCastException e)
				{
					throw new OMException("Last Node & Parent of an OMText should be an Element"+e);
				}
				
			return node;
			
		} else {
			OMElement node;
			if (lastNode == null) {
				node = constructNode(null, elementName, true);
			} else if (lastNode.isComplete()) {
				node = constructNode((OMElement)lastNode.getParent(), elementName, false);
				lastNode.setNextSibling(node);
				node.setPreviousSibling(lastNode);
			} else {
				OMElement e = (OMElement) lastNode;
				node = constructNode((OMElement) lastNode, elementName, false);
				e.setFirstChild(node);
			}
			
			// fill in the attributes
			processAttributes(node);
			//TODO Exception when trying to log . check this
			//			log.info("Build the OMElelment {" + node.getLocalName() + '}'
			//					+ node.getLocalName() + "By the StaxSOAPModelBuilder");
			return node;
		}
	}
	
	public DataHandler getDataHandler(String blobContentID) throws OMException {
		return mimeHelper.getDataHandler(blobContentID);
	}
}
