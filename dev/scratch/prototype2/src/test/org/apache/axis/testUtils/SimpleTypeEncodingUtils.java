/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.testUtils;

import org.apache.axis.engine.AxisFault;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;

public class SimpleTypeEncodingUtils {


    public static String[] deserializeStringArray(XMLStreamReader xpp)
        throws AxisFault {
        ArrayList strings = new ArrayList();

        try {
            int event = xpp.next();
            while (true) {
                if (XMLStreamConstants.START_ELEMENT == event) {
                    strings.add(deserializeString(xpp));
                } else if (XMLStreamConstants.END_ELEMENT == event) {
                    break;
                } else if (XMLStreamConstants.END_DOCUMENT == event) {
                    throw new AxisFault("premature and of file");
                }
                event = xpp.next();
            }
            String[] stringvals = new String[strings.size()];
            for (int i = 0; i < strings.size(); i++) {
                stringvals[i] = (String) strings.get(i);
            }
            return stringvals;
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }

    }

    public static String deserializeStringWithWiteSpaces(XMLStreamReader xpp)
        throws AxisFault {
                StringBuffer value = new StringBuffer();
                try {
                    int event = xpp.getEventType();
                    while(XMLStreamConstants.START_ELEMENT != event){
                        event = xpp.next();
                    }
                    event = xpp.next();
                    while(XMLStreamConstants.END_ELEMENT != event){
                        if(XMLStreamConstants.CHARACTERS == event){
                            value.append(xpp.getText());
                        }
                        event = xpp.next();
                    }
                } catch (XMLStreamException e) {
                    throw AxisFault.makeFault(e);
                }
                if(value.length() == 0){
                    return null;
                }else{
                    return value.toString();        
                }
        }
        
    public static String deserializeString(XMLStreamReader xpp)
        throws AxisFault {
        String value = null;
        try {
            int event = xpp.getEventType();
            while (XMLStreamConstants.START_ELEMENT != event) {
                event = xpp.next();
            }
            event = xpp.next();
            while (XMLStreamConstants.END_ELEMENT != event) {
                if (XMLStreamConstants.CHARACTERS == event
                    && !xpp.isWhiteSpace()) {
                    value = xpp.getText();
                }
                event = xpp.next();
            }
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }
        return value;
    }

    public static int deserializeInt(XMLStreamReader xpp) throws AxisFault {
        String val = deserializeString(xpp);
        if (val == null) {
            throw new AxisFault("Number format exception value is null");
        }
        return Integer.parseInt(val);
    }
    public static void serialize(
        XMLStreamWriter out,
        QName elementName,
        String value)
        throws AxisFault {
        try {
            out.writeStartElement(
                elementName.getNamespaceURI(),
                elementName.getLocalPart());
            out.writeCharacters(value);
            out.writeEndElement();
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }

    }

}
