package org.apache.axis.om;


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
 * Date: Oct 4, 2004
 * Time: 11:39:01 AM
 * <p/>
 * OMElement and OMAttribute must have namespace support and those two behave in same manner as far as the namespaces are concerned.
 * So one can implement this interface specific methods in an abstract class.
 */
public interface OMNamedNode extends OMNode {

    public String getLocalName();
    public void setLocalName(String localName);

    public OMNamespace getNamespace() throws OMException;
    public void setNamespace(OMNamespace namespace);
}
