<<<<<<< .mine
package org.apache.axis.om.impl;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;

import java.io.PrintStream;

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
 * Date: Oct 6, 2004
 * Time: 1:36:55 PM
 */
public class OMTextImpl extends OMNodeImpl implements OMText {

    protected short textType;

    public OMTextImpl(OMElement parent, String text) {
        super(parent);
        setValue(text);
        done = true;
    }

    public OMTextImpl(String s) {
        super();
        setValue(s);
    }

    /**
     * We use the OMText class to hold comments, text, characterData, CData, etc.,
     * The codes are found in OMNode class
     *
     * @param type
     */
    public void setTextType(short type) {
        this.textType = type;
    }

    public short getTextType() {
        return textType;  
    }

    public OMNode getFirstChild() throws OMException {
        throw new OMException();
    }

    public void setFirstChild(OMNode node) throws OMException {
        throw new OMException();
    }

    public void print(PrintStream s) throws OMException {
        s.print(value);
    }

    /**
     * This is to get the type of node, as this is the super class of all the nodes
     *
     * @return
     * @throws org.apache.axis.om.OMException
     */
    public short getType() throws OMException {
        return OMNode.TEXT_NODE;    //TODO implement this
    }




}
=======
package org.apache.axis.om.impl;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;

import java.io.PrintStream;

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
 * Date: Oct 6, 2004
 * Time: 1:36:55 PM
 */
public class OMTextImpl extends OMNodeImpl implements OMText {

    protected short textType;

    public OMTextImpl(OMElement parent, String text) {
        super(parent);
        setValue(text);
        done = true;
    }

    public OMTextImpl(String s) {
        super();
        setValue(s);
    }

    /**
     * We use the OMText class to hold comments, text, characterData, CData, etc.,
     * The codes are found in OMNode class
     *
     * @param type
     */
    public void setTextType(short type) {
        this.textType = type;
    }

    public short getTextType() {
        return textType;  
    }

    public OMNode getFirstChild() throws OMException {
        throw new OMException();
    }

    public void setFirstChild(OMNode node) throws OMException {
        throw new OMException();
    }

    public void print(PrintStream s) throws OMException {
        s.print(value);
    }

    /**
     * This is to get the type of node, as this is the super class of all the nodes
     *
     * @return
     * @throws org.apache.axis.om.OMException
     */
    public short getType() throws OMException {
        return OMNode.TEXT_NODE;    //TODO implement this
    }




}
>>>>>>> .r55205
