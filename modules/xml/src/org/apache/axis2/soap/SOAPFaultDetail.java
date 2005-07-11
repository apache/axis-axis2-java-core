package org.apache.axis2.soap;

import org.apache.axis2.om.OMElement;

import java.util.Iterator;

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

/**
 * The Detail element information item is intended for carrying application
 * specific error information related to the SOAP Body .
 * <p/>
 * The Detail element information item has:
 * A [local name] of Detail .
 * A [namespace name] of http://www.w3.org/2003/05/soap-envelope .
 * Zero or more attribute information items in its [attributes] property.
 * Zero or more child element information items in its [children] property.
 */
public interface SOAPFaultDetail extends OMElement {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
    public void addDetailEntry(OMElement detailElement);

    public Iterator getAllDetailEntries();

}
