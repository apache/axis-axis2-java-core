package org.apache.axis.om.traversal;

import org.apache.axis.om.OMTableModel;
import org.apache.axis.om.StreamingOmBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  * Copyright 2001-2004 The Apache Software Foundation.
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
 *
 * @author Ajith Ranabahu
 * Date: Sep 19, 2004
 * Time: 3:29:34 PM
 *
 * Serving a list in an iterative manner is REALLY DIFFICULT!!!!!!!!!
 */
public abstract class OmIterator implements NodeList {

    protected OMTableModel model;
    protected StreamingOmBuilder builder;

    public OmIterator(OMTableModel model, StreamingOmBuilder builder) {
        this.model = model;
        this.builder = builder;
    }

    public Node item(int index) {
        return null;
    }

    public int getLength() {
        return 0;
    }
}
