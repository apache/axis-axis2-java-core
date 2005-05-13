package org.apache.axis.soap;

import org.apache.axis.om.OMElement;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public interface SOAPFaultNode extends OMElement {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */

    /**
     * each SOAP node is identified by a URI. The value of the Node element
     * information item is the URI that identifies the SOAP node that generated
     * the fault. SOAP nodes that do not act as the ultimate SOAP receiver MUST
     * include this element information item. An ultimate SOAP receiver MAY
     * include this element information item to indicate explicitly that it
     * generated the fault.
     *
     * @param uri
     */
    public void setNodeValue(String uri);

    public String getNodeValue();
}
