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
 
package interop.doclit;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMException;
import org.apache.axis.testUtils.Encoder;
import org.apache.axis.testUtils.SimpleTypeEncodingUtils;
import org.xml.sax.ContentHandler;

public class SOAPStructEncoder implements Encoder {
    
    public static final String URI = "http://soapinterop.org/xsd";
    private static final String VAR_STRING = "varString";
    private static final String VAR_INT = "varInt";
    private static final String VAR_FLOAT = "varFloat";

    private ContentHandler contentHandler;
    private SOAPStruct struct;
    
    
    public SOAPStructEncoder() {
    }

    public SOAPStructEncoder(SOAPStruct struct) {
        this.struct = struct;
    }

    public Object deSerialize(XMLStreamReader xpp) throws AxisFault {
        SOAPStruct struct = new SOAPStruct();

        try {
            int event = xpp.next();
            while (true) {
                if (XMLStreamConstants.START_ELEMENT == event) {
                    String localName = xpp.getLocalName();

                    if (VAR_STRING.equals(localName)) {
                        struct.setVarString(
                            SimpleTypeEncodingUtils.deserializeString(xpp));
                    }else if(VAR_INT.equals(localName)){
                        struct.setVarInt(SimpleTypeEncodingUtils.deserializeInt(xpp));                        
                    }else if(VAR_FLOAT.equals(localName)){
                        struct.setVarFloat(SimpleTypeEncodingUtils.deserializeFloat(xpp));                                
                    }else{
                        throw new AxisFault("Unknown element "+ localName);
                    }
                }
                if (XMLStreamConstants.END_ELEMENT == event) {
                    break;
                }
                if (XMLStreamConstants.END_DOCUMENT == event) {
                    throw new AxisFault("premature and of file");
                }
                event = xpp.next();
            }

            return struct;
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }

    }


    
    

    public void serialize(ContentHandler contentHandler)
        throws OMException {
        if (contentHandler == null) {
            throw new OMException("Please set the content Handler");
        }
        try {
            SimpleTypeEncodingUtils.writeElement(contentHandler,"varString",URI,"s:varString",struct.getVarString());
            SimpleTypeEncodingUtils.writeElement(contentHandler,"varFloat",URI,"s:varFloat",String.valueOf(struct.getVarFloat()));
            SimpleTypeEncodingUtils.writeElement(contentHandler,"varInt",URI,"s:varInt",String.valueOf(struct.getVarInt()));
        } catch (Exception e) {
            throw new OMException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.apache.axis.encoding.Encoder#setObject(java.lang.Object)
     */
    public void setObject(Object obj) {
        this.struct = (SOAPStruct) obj;

    }

}


