package org.apache.axis.om.impl.llom;

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.SOAPHeader;

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
public class SOAPEnvelopeImpl extends OMElementImpl implements SOAPEnvelope, OMConstants {
    /**
     * @param builder
     */
    public SOAPEnvelopeImpl(OMXMLParserWrapper builder) {
        super(SOAPENVELOPE_LOCAL_NAME, null, null, builder);
    }

    public SOAPEnvelopeImpl(OMNamespace ns, OMXMLParserWrapper builder) {
        super(SOAPENVELOPE_LOCAL_NAME, ns, null, builder);
    }

    /**
     * @param ns
     */
    public SOAPEnvelopeImpl(OMNamespace ns) {
        super(SOAPENVELOPE_LOCAL_NAME, ns);
    }

        /**
     * Returns the <CODE>SOAPHeader</CODE> object for this <CODE>
     * SOAPEnvelope</CODE> object.
     * <p/>
     * <P> This SOAPHeader will just be a container for all the headers in the
     * <CODE>OMMessage</CODE>
     * </P>
     *
     * @return the <CODE>SOAPHeader</CODE> object or <CODE>
     *         null</CODE> if there is none
     * @throws org.apache.axis.om.OMException if there is a problem
     *                                        obtaining the <CODE>SOAPHeader</CODE> object
     */
    public SOAPHeader getHeader() throws OMException {
//		if(builder != null){
//	    	while(header == null && body == null){
//	    		builder.next();
//	    	}
//		}	  
		OMNode node = getFirstChild();
		while(node != null){
			if(node != null && node.getType() == OMNode.ELEMENT_NODE){
				OMElement element = (OMElement)node;
				if(OMConstants.HEADER_LOCAL_NAME.equals(element.getLocalName())){
					return (SOAPHeader)element;
				}
			}
			node = node.getNextSibling();
		}
		return null;
    }

    /**
     * Returns the <CODE>SOAPBody</CODE> object associated with
     * this <CODE>SOAPEnvelope</CODE> object.
     * <p/>
     * <P> This SOAPBody will just be a container for all the BodyElements in the
     * <CODE>OMMessage</CODE>
     * </P>
     *
     * @return the <CODE>SOAPBody</CODE> object for this <CODE>
     *         SOAPEnvelope</CODE> object or <CODE>null</CODE> if there
     *         is none
     * @throws org.apache.axis.om.OMException if there is a problem
     *                                        obtaining the <CODE>SOAPBody</CODE> object
     */
    public SOAPBody getBody() throws OMException {
    	//Look at this . this code forces the OM  to build the whole thing because 
    	//the iterator needs to stay <i>ahead</i> one node.We need to do this manually
//    	if(builder != null){
//			while( body == null){
//				builder.next();
//			}
//			
//    	}

//    	return body;

		OMNode node = getFirstChild();
		while(node != null){
			if(node != null && node.getType() == OMNode.ELEMENT_NODE){
				OMElement element = (OMElement)node;
				if(OMConstants.BODY_LOCAL_NAME.equals(element.getLocalName())){
					return (SOAPBody)element;
				}
			}
			node = node.getNextSibling();
		}
		return null;
//        Iterator bodyIterator = this.getChildrenWithName(new QName(OMConstants.BODY_NAMESPACE_URI, OMConstants.BODY_LOCAL_NAME));
//        SOAPBody soapBody = null;
//        if (bodyIterator.hasNext()) {
//            soapBody = (SOAPBody) bodyIterator.next();
//        }
//
//        return soapBody;
    }

    public void detach() throws OMException {
        throw new OMException("Root Element can not be detached");

    }


}
