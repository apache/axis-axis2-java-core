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
package org.apache.axis2.saaj;

import org.apache.axiom.om.impl.dom.ElementImpl;

import javax.xml.soap.DetailEntry;

/**
 * The content for a Detail object, giving details for a SOAPFault object.
 * A DetailEntry object, which carries information about errors related to the
 * SOAPBody  object that contains it, is application-specific.
 */
public class DetailEntryImpl extends SOAPElementImpl implements DetailEntry {

    /**
     * @param element
     */
    public DetailEntryImpl(ElementImpl element) {
        super(element);
    }

}
