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
package org.apache.axis.encoding;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.engine.AxisFault;


public class SimpleTypeEncodingUtils {
    public static String deserializeString(XMLStreamReader xpp)throws AxisFault{
        StringBuffer value = null;
        try {
            int event = xpp.getEventType();
            while(XMLStreamConstants.START_ELEMENT != event){
                event = xpp.next();
            }
            event = xpp.next();
            while(XMLStreamConstants.END_ELEMENT != event){
                event = xpp.next();
                if(XMLStreamConstants.CHARACTERS == event){
                    value.append(xpp.getText());
                }
            }
        } catch (XMLStreamException e) {
            AxisFault.makeFault(e);
        }
        if(value.length() == 0){
            return null;
        }else{
            return value.toString();        
        }
    }
    
    public static int deserializeInt(XMLStreamReader xpp)throws AxisFault{
        String val = deserializeString(xpp);
        if(val == null){
            throw new AxisFault("Number format exception value is null");
        }
        return Integer.parseInt(val);
    
    }
}
