package org.apache.axis.impl.llom;

import org.apache.axis.om.OMBodyBlock;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMXMLParserWrapper;

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
 * Date: Nov 8, 2004
 * Time: 1:09:37 PM
 */
public class OMBodyElementImpl extends OMElementImpl implements OMBodyBlock{

    /**
     *
     * @param localName
     * @param ns
     * @param parent
     * @param builder
     */
    public OMBodyElementImpl(String localName, OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        super(localName, ns, parent, builder);
    }

    public OMBodyElementImpl(String localName, OMNamespace ns) {
        super(localName, ns);
    }
}
