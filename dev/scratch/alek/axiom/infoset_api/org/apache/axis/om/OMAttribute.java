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

/**
 * This is <b>immutable</b> value object that represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.attribute">Attribute
 * Information Item</a>
 * with exception of <b>references</b> property.
 * <br />NOTE: this interface has no parent property to make easy classes implementing this interface
 * immutable and very lightweight.
 * <br />NOTE: namespace and prefix properties are folded into XmlNamespace value object.
 */
public interface OMAttribute extends Cloneable
{
    /**
     * Method clone
     *
     * @return   the clone of attribute
     *
     * @exception   CloneNotSupportedException
     *
     */
    public Object clone() throws CloneNotSupportedException;

    /**
     * XML Infoset [owner element] property
     */
    //public XmlElement getOwner();
    //public XmlElement setOwner(XmlElement newOwner);

    /**
     * return XML Infoset [namespace name] property (namespaceName from getNamespace()
     * or null if attribute has no namespace
     */
    public String getNamespaceName();

    /**
     * Combination of XML Infoset [namespace name] and [prefix] properties
     */
    public OMNamespace getNamespace();

    /**
     * XML Infoset [local name] property
     */
    public String getName();


    /**
     * XML Infoset [normalized value] property
     */
    public String getValue();

    /**
     * XML Infoset [attribute type]
     */

    public String getType();
    /**
     * XML Infoset [specified] flag
     */
    public boolean isSpecified();
}
