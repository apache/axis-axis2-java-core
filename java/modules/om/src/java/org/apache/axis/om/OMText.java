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
package org.apache.axis.om;

/**
 * Interface OMText
 */
public interface OMText extends OMNode {
    /**
     * We use the OMText class to hold comments, text, characterData, CData, etc.,
     * The codes are found in OMNode class
     *
     * @param type
     */
    public void setTextType(short type);

    /**
     * Method getTextType
     *
     * @return
     */
    public short getTextType();
}
