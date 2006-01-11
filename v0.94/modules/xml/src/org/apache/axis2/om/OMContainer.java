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

package org.apache.axis2.om;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Captures the operations related to containment shared by both a document and an element.
 *
 * <p>Exposes the ability to add, find, and iterate over the children of a document or
 * element.</p>
 */
public interface OMContainer {

    /**
     * Adds the given node as the last child. One must preserve the order of children, 
     * in this operation.
     * Tip : appending the new child is preferred.
     *
     * @param omNode
     */
    public void addChild(OMNode omNode);

    /**
     * Returns an iterator for child nodes matching the criteria indicated by the given QName.
     *
     * <p>This function searches in three ways:
     *  <ul>
     *   <li>Exact match - Both parts of the passed QName are non-null.  Only children with the
     *      same namespace and local name will be returned.
     *   </li>
     *  <li>Namespace match - The local name of the passed QName is null.  All children matching the
     *      namespace will be returned by the iterator.
     *  </li>
     *  <li>Local name match - The namespace of the passed QName is null.  All children with the
     *      matching local name will be returned by the iterator.
     *  </li>
     * </ul>
     *
     * <p>
     * <b>Example:</b> <code>header.getChildrenWithName( new QName(ADDRESSING_NAMESPACE, null));</code>
     *  will return all of the "addressing" headers.
     * </p>
     *
     * @param elementQName The QName specifying namespace and local name to match.
     * @return Returns an iterator of {@link OMElement} items that match the given QName appropriately.
     */
    public Iterator getChildrenWithName(QName elementQName);

    /**
     * Returns the first child in document order that matches the given QName criteria.
     *
     * <p>The QName filter is applied as in the function {@link #getChildrenWithName}.</p>
     *
     * @param elementQName The QName to use for matching.
     *
     * @return Returns the first element in document order that matches the <tt>elementQName</tt> criteria.
     *
     * @see #getChildrenWithName
     *
     * @throws OMException Could indirectly trigger building of child nodes.
     */
    public OMElement getFirstChildWithName(QName elementQName) throws OMException;

    /**
     * Returns an iterator for the children of the container.
     *
     * @return Returns a {@link Iterator} of children, all of which implement {@link OMNode}.
     *
     * @see #getFirstChildWithName
     * @see #getChildrenWithName
     */
    public Iterator getChildren();

    /**
     * Gets the first child.
     *
     * @return Returns the first child.  May return null if the container has no children.
     */
    public OMNode getFirstOMChild();

    public boolean isComplete();

    public void buildNext();
}
