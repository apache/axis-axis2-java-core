package org.apache.axis2.soap.impl.llom;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.OMElementImpl;

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
 *
 * author : Eran Chinthaka (chinthaka@apache.org)
 *
 * This will be the base class of all the SOAP specific elements inthe system

 */

public abstract class SOAPElement extends OMElementImpl {


    /**
     * @param parent
     * @param parent
     */
    protected SOAPElement(OMElement parent,
                          String localName,
                          boolean extractNamespaceFromParent) throws SOAPProcessingException {
        super(localName, null, parent);
        if (parent == null) {
            throw new SOAPProcessingException(
                    " Can not create " + localName +
                    " element without a parent !!");
        }
        checkParent(parent);

        if (extractNamespaceFromParent) {
            this.ns = parent.getNamespace();
        }
        this.localName = localName;
    }


    protected SOAPElement(OMElement parent,
                          String localName,
                          OMXMLParserWrapper builder) {
        super(localName, null, parent, builder);
    }

    /**
     * Caution : This Constructor is meant to be used only by the SOAPEnvelope.
     * <p/>
     * Reasons : This can be used to create a SOAP Element programmatically. But we need to make sure that the user
     * always passes a parent for the element being created. But SOAP Envelope has no parent.
     *
     * @param localName
     * @param ns
     */
    protected SOAPElement(String localName, OMNamespace ns) {
        super(localName, ns);

    }

    /**
     * This has to be implemented by all the derived classes to check for the correct parent.
     */
    protected abstract void checkParent(OMElement parent) throws SOAPProcessingException;

}
