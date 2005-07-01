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
package org.apache.wsdl.impl;

import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLTypes;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * @author chathura@opensource.lk
 */
public class WSDLTypesImpl extends ComponentImpl implements WSDLTypes {


    /**
     * Adds the <code>ExtensionElement</code> to the map keyed with the <code>QName</code>
     *
     * @param qName
     * @param element
     */
    public void addElement(WSDLExtensibilityElement element) {
        this.addExtensibilityElement(element);
    }

    /**
     * Will return the first Element with the given <code>QName</code>
     * Returns null if not found.
     *
     * @param qName
     * @return
     */
    public WSDLExtensibilityElement getFirstElement(QName qName) {
        Iterator iterator = this.getExtensibilityElements().iterator();
		while(iterator.hasNext()){
			WSDLExtensibilityElement temp =(WSDLExtensibilityElement)iterator.next();
			if(temp.getType().equals(qName))
				return temp;        	
        }
		
		return null;
    }
}
