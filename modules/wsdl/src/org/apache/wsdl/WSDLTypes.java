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

package org.apache.wsdl;

import javax.xml.namespace.QName;


/**
 * @author chathura@opensource.lk
 */
public interface WSDLTypes extends Component {
    /**
     * Adds the <code>ExtensionElement</code> to the map keyed with the <code>QName</code>
     *
     * @param qName
     * @param element
     */
    public void addElement(WSDLExtensibilityElement element);

    /**
     * Will return the first Element with the given <code>QName</code>
     * Returns null if not found.
     *
     * @param qName
     * @return
     */
    public WSDLExtensibilityElement getFirstElement(QName qName);
}