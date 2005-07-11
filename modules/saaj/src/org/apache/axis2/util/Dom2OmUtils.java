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
package org.apache.axis2.util;

import org.apache.axis2.om.OMNode;
import org.apache.axis2.saaj.NodeImpl;
import org.w3c.dom.Node;

/**
 * Class to convert DOM node to OM Node
 */
public class Dom2OmUtils {

    private OMNode omNode;

    public static OMNode toOM(Node node) {
        if (node instanceof NodeImpl) {
            return ((NodeImpl) node).getOMNode();
        }
        //ELSE Assumes an implemenattion of DOM to be present
        //so, here we convert DOM Node to a OMNode and add it as a
        //child to the omNode member of this NodeImpl
        return null;
    }

}
