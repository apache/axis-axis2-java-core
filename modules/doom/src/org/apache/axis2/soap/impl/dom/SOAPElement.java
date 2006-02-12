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

package org.apache.axis2.soap.impl.dom;

import org.apache.axis2.om.impl.dom.DocumentImpl;
import org.apache.axis2.om.impl.dom.ElementImpl;
import org.apache.axis2.om.impl.dom.NamespaceImpl;
import org.apache.axis2.om.impl.dom.ParentNode;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMXMLParserWrapper;
import org.apache.ws.commons.soap.SOAPProcessingException;

public abstract class SOAPElement extends ElementImpl {


    /**
     * @param parent
     */
    protected SOAPElement(OMElement parent,
                          String localName,
                          boolean extractNamespaceFromParent) throws SOAPProcessingException {
        super((ParentNode)parent, localName, null);
        if (parent == null) {
            throw new SOAPProcessingException(
                    " Can not create " + localName +
                    " element without a parent !!");
        }
        checkParent(parent);

        if (extractNamespaceFromParent) {
            this.namespace = parent.getNamespace();
        }
        this.localName = localName;
    }


    protected SOAPElement(OMElement parent,
                          String localName,
                          OMXMLParserWrapper builder) {
        super((ParentNode)parent, localName, null, builder);
    }

    protected SOAPElement(DocumentImpl doc, String localName,OMNamespace ns) {
		super(doc, localName, (NamespaceImpl)ns);
	}
    
    protected SOAPElement(DocumentImpl ownerDocument, String tagName, NamespaceImpl ns, OMXMLParserWrapper builder) {
    	super(ownerDocument, tagName, ns, builder);
    }
    
// /**
// * Caution : This Constructor is meant to be used only by the SOAPEnvelope.
//     * <p/>
//     * Reasons : This can be used to create a SOAP Element programmatically. But we need to make sure that the user
//     * always passes a parent for the element being created. But SOAP Envelope has no parent.
//     *
//     * @param localName
//     * @param ns
//     */
//    protected SOAPElement(String localName, OMNamespace ns) {
//        super(localName, ns);
//
//    }

    /**
     * This has to be implemented by all the derived classes to check for the correct parent.
     */
    protected abstract void checkParent(OMElement parent) throws SOAPProcessingException;

}
