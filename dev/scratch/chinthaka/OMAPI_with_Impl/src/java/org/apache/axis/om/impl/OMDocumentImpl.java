package org.apache.axis.om.impl;

import org.apache.axis.om.OMDocument;
import org.apache.axis.om.OMElement;

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
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Oct 7, 2004
 * Time: 10:52:17 AM
 */
public class OMDocumentImpl extends OMNodeImpl implements OMDocument {

    private OMElementImpl root;
    private OMXmlPullParserWrapper parserWrapper;

    public OMDocumentImpl(OMXmlPullParserWrapper parserWrapper) {
        this.parserWrapper = parserWrapper;
    }

    public OMElementImpl getRootElement() {
        if (root == null) {
            parserWrapper.next();
        }
        return root;
    }

    public void setRootElement(OMElementImpl root) {
        this.root = root;
    }

    /**
     * Get the root element of this document
     *
     * @return the root element
     *
     * This method should be changed as getRootElement
     *
     */
    public OMElement getDocumentElement() {
        return getRootElement();
    }
}
