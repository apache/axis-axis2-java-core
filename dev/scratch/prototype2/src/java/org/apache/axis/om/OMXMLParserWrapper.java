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
 * Date: Nov 11, 2004
 * Time: 12:54:18 PM
 *
 * This will wrap the underlying parser OM uses. For example this will define and interface, so that either XPP or StAX or tStAX can be used
 *
 *
 */
public interface OMXMLParserWrapper {

    /**
     * @return
     * @throws OMException
     */
    public OMEnvelope getOMEnvelope() throws OMException;

    /**
     *  Proceed the parser one step and return the event value
     * @return
     * @throws OMException
     */
    public int next() throws OMException;

    /**
     *  Discard the current element
     * This should remove the given element and its decendants.
     * @param el
     * @throws OMException
     */
    public void discard(OMElement el) throws OMException;

    /**
     * @param b
     * @throws OMException
     */
    public void setCache(boolean b) throws OMException;

/**
     * Allows to access the underlying parser. Since the parser
     * depends on the underlying implementation,an Object is returned
     * However the implementations may have restrictions in letting access to
     * the parser
     * @return
     */
    public Object getParser();

    /**
     *
     * @return the complete status
     */
    public boolean isCompleted();

}
