package org.apache.axis.om.util;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.impl.OMElementImpl;
import org.apache.axis.om.impl.OMXmlPullParserWrapper;
import org.apache.axis.om.impl.OMNamespaceImpl;
import org.apache.axis.om.impl.OMAttributeImpl;

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
 * Date: Oct 14, 2004
 * Time: 1:19:29 PM
 *
 * I created this class to be dependant on the implementation, so that one has to change only this class
 * to test a different implementation which implements the proposed OM API
 */
public class OMNodeBuilder {
    /**
     *
     * @param parent
     * @return
     */
    public static OMElement createOMElement(OMElement parent){
       return new OMElementImpl(parent);
    }

    /**
     *
     * @param localName
     * @param namespace
     * @return
     */
    public static OMElement createOMElement(String localName, OMNamespace namespace){
       return new OMElementImpl(localName, namespace);
    }

    /**
     *
     * @param localName
     * @param namespace
     * @param parent
     * @param pullParserWrapper
     * @return
     */ 
    public static OMElement createOMElement(String localName, OMNamespace namespace, OMElement parent, OMXmlPullParserWrapper pullParserWrapper){
       return new OMElementImpl(localName, namespace, parent, pullParserWrapper); 
    }

    /**
     *
     * @param uri
     * @param prefix
     * @return
     */
    public static OMNamespace createOMNamespace(String uri, String prefix){
        return new OMNamespaceImpl(uri, prefix);
    }

    /**
     *
     * @param localName
     * @param ns
     * @param value
     * @param parent
     * @return
     */
    public static OMAttribute createOMAttribute(String localName, OMNamespace ns, String value, OMElement parent){
        return new OMAttributeImpl(localName, ns, value, parent);
    }

}
