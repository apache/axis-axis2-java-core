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
 */
public interface OMXMLParserWrapper {
    /**
     * Proceed the parser one step and return the event value
     *
     * @return
     * @throws org.apache.axis.om.OMException
     */
    int next() throws OMException;

    /**
     * Discard the current element
     * This should remove the given element and its decendants.
     *
     * @param el
     * @throws org.apache.axis.om.OMException
     */
    void discard(OMElement el) throws OMException;

    /**
     * @param b
     * @throws org.apache.axis.om.OMException
     */
    void setCache(boolean b) throws OMException;

    /**
     * Allows to access the underlying parser. Since the parser
     * depends on the underlying implementation,an Object is returned
     * However the implementations may have restrictions in letting access to
     * the parser
     *
     * @return
     */
    Object getParser();

    /**
     * @return the complete status
     */
    boolean isCompleted();

    /**
     * @return the document element
     */
    OMElement getDocumentElement();

    /**
     * Returns the type of the builder.
     * Can be either the
     * PUSH_TYPE_BUILDER or PULL_TYPE_BUILDER
     * @return
     */
    short getBuilderType();

    /**
     * Registers an external content handler. Especially useful for
     * push type builders. will throw an unsupportedOperationExcveption if
     * such handler registration is not supported     
     * @param obj
     */
    void registerExternalContentHandler(Object obj);

    /**
     * get the registered external content handler
     * @return
     */
    Object getRegisteredContentHandler();

}
