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
 * Time: 2:04:43 PM
 */
public interface OMText extends OMNode {
    /**
     * We use the OMText class to hold comments, text, characterData, CData, etc.,
     * The codes are found in OMNode class
     *
     * @param type
     */
    public void setTextType(short type);
    public short getTextType();
}
