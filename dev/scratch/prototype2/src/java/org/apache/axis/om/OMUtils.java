/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.om;

import java.util.Iterator;


public class OMUtils {
    public static OMElement getFirstChildElement(OMElement omele) {
        OMElement childElement = null;
        OMNode child = null;
        if (omele != null) {
            Iterator it = omele.getChildren();
            if (it.hasNext()) {
                child = (OMNode) it.next();
                while (OMNode.ELEMENT_NODE != child.getType()) {
                    if (it.hasNext()) {
                        child = (OMNode) it.next();
                    } else {
                        break;
                    }
                }
                childElement = (OMElement) child;
            }

        }
        return childElement;
    }
}
