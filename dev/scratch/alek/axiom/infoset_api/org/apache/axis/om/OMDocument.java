/*
 * Copyright 2004 The Apache Software Foundation.
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

import java.io.OutputStream;

/**
 * Represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.document">Document Information Item</a>
 * .
 *
 * @version $Revision: 1.5 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface OMDocument extends OMContainer, Cloneable
{
    //JDK15 covariant public XmlDocument clone() throws CloneNotSupportedException
    public Object clone() throws CloneNotSupportedException;

    /**
     * An ordered list of child information items, in document order.
     * The list contains exactly one element information item.
     * The list also contains one processing instruction information item
     * for each processing instruction outside the document element,
     * and one comment information item for each comment outside the document element.
     * Processing instructions and comments within the DTD are excluded.
     * If there is a document type declaration,
     * the list also contains a document type declaration information item.
     */
    public Iterable children();

    /**
     * top level document element
     */
    public OMElement getDocumentElement();
    // manipulate children
    public void setDocumentElement(OMElement rootElement);

    public String getBaseUri();
    public String getCharacterEncodingScheme();
    public void setCharacterEncodingScheme(String characterEncoding);
    public Boolean isStandalone();
    public String getVersion();
    //public String setVersion();
    public boolean isAllDeclarationsProcessed();


}
