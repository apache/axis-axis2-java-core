package org.apache.axis.impl.llom;

import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OMNamespace;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * Time: 11:43:23 AM
 */
public class OMAttributeImpl extends OMNamedNodeImpl implements OMAttribute {
    private static String QUOTE_ENTITY = "&quot;";
    private static Matcher matcher = Pattern.compile("\"").matcher(null);

    public OMAttributeImpl(String localName, OMNamespace ns, String value, OMElement parent) {
        super(localName, ns, parent);
        setValue(value);
    }

    public OMAttributeImpl(String localName, OMNamespace ns, String value) {
        super(localName, ns, null);
        setValue(value);
    }

    synchronized static String replaceQuoteWithEntity(String value) {
        matcher.reset(value);
        return matcher.replaceAll(QUOTE_ENTITY);
    }

    public void print(PrintStream s) throws OMException {
        super.print(s);
        s.print('=');
        String v = value;
        char quote = '"';
        if (value.indexOf('"') != -1)
            if (value.indexOf('\'') == -1)
                quote = '\'';
            else
                v = replaceQuoteWithEntity(value);
        s.print(quote);
        s.print(v);
        s.print(quote);
    }

    public void detach() throws OMException {
        
        if (parent == null)
            throw new OMException();
        if (getPreviousSibling() == null)
            parent.setFirstAttribute((OMAttributeImpl) nextSibling);
        else
            previousSibling.setNextSibling(nextSibling);
        if (nextSibling != null)
            nextSibling.setPreviousSibling(previousSibling);
    }

    //overidden to force even null namepaces
    public OMNamespace getNamespace() throws OMException {
        return ns;
    }

}
