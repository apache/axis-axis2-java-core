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

import java.util.ArrayList;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.engine.AxisFault;

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public class ArrayTypeEncoder implements Encoder{
    private Object[] obj = null;
    private Encoder arrayTypeEncoder;
    
    public ArrayTypeEncoder(Object[] obj,Encoder arrayTypeEncoder){
       this.obj = obj;
       this.arrayTypeEncoder = arrayTypeEncoder;
    }
 
    public ArrayTypeEncoder(Encoder arrayTypeEncoder){
       this.arrayTypeEncoder = arrayTypeEncoder;
    }

 
    public void serialize(ContentHandler cHandler) throws OMException {
        try {
        	for(int i = 0;i<obj.length;i++){
				cHandler.startElement(OMConstants.ARRAY_ITEM_NSURI,OMConstants.ARRAY_ITEM_LOCALNAME,OMConstants.ARRAY_ITEM_QNAME,null);
				arrayTypeEncoder.setObject(obj[i]);
				arrayTypeEncoder.serialize(cHandler);
				cHandler.endElement(OMConstants.ARRAY_ITEM_NSURI,OMConstants.ARRAY_ITEM_LOCALNAME,OMConstants.ARRAY_ITEM_QNAME);
        	}
        } catch (SAXException e) {
            throw new OMException(e);
        }

    }

	/**
	 * @return
	 */
	public Encoder getArrayTypeEncoder() {
		return arrayTypeEncoder;
	}

	/**
	 * @param encoder
	 */
	public void setArrayTypeEncoder(Encoder encoder) {
		arrayTypeEncoder = encoder;
	}

    public Object deSerialize(XMLStreamReader xpp) throws AxisFault {
        ArrayList objs = new ArrayList();

        try {
            int event = xpp.next();
            while (XMLStreamConstants.START_ELEMENT != event
                && XMLStreamConstants.END_ELEMENT != event) {
                event = xpp.next();
            }
            if (XMLStreamConstants.END_ELEMENT == event) {
                return null;
            }

            event = xpp.next();
            while (true) {
                if (XMLStreamConstants.START_ELEMENT == event) {
                    objs.add(arrayTypeEncoder.deSerialize(xpp));
                } else if (XMLStreamConstants.END_ELEMENT == event) {
                    break;
                } else if (XMLStreamConstants.END_DOCUMENT == event) {
                    throw new AxisFault("premature and of file");
                }
                event = xpp.next();
            }
            Object[] vals = new Object[objs.size()];
            for (int i = 0; i < objs.size(); i++) {
                vals[i] = objs.get(i);
            }
            return vals;
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axis.encoding.Encoder#setObject(java.lang.Object)
     */
    public void setObject(Object obj) {
        this.obj = (Object[])obj;
    }

}
