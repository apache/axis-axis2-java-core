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

package org.apache.axis2.clientapi;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.OMText;

import javax.xml.namespace.QName;
import java.util.Iterator;


public abstract class AbstractCallbackSupporter {

    protected boolean runOptimizer = false;

    /**
     *
     * @param element
     * @param qNames
     */
    protected static void optimizeContent(OMElement element, QName[] qNames){
        int length = qNames.length;
        QName qName;
        for (int i = 0; i < length; i++) {
            qName = qNames[i];
            markElementsAsOptimized(qName,element);
        }
    }

    /**
     *
     * @param qName
     * @param rootElt
     */
    private static void markElementsAsOptimized(QName qName,OMElement rootElt){
        if (rootElt.getQName().equals(qName)){
            //get the text node and mark it
            OMNode node = rootElt.getFirstOMChild();
            if (node.getType()==OMNode.TEXT_NODE){
                ((OMText)node).setOptimize(true);
            }

        }
        Iterator childElements = rootElt.getChildElements();
        while (childElements.hasNext()) {
            markElementsAsOptimized(qName,(OMElement)childElements.next());
        }
    }
}
