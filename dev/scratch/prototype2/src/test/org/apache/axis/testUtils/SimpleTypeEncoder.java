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

import javax.xml.stream.XMLStreamReader;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public class SimpleTypeEncoder implements Encoder{
    private Object obj = null;
    public SimpleTypeEncoder(Object obj){
       this.obj = obj;
    }
    
    
    public void serialize(ContentHandler cHandler) throws OMException {
        try {
            if(obj instanceof String){
                char[] str = ((String)obj).toCharArray();
                cHandler.characters(str,0,str.length);
            }else if(obj instanceof Integer){
                char[] str = obj.toString().toCharArray();
                cHandler.characters(str,0,str.length);
            }else if(obj instanceof String[]){
                String[] strs = (String[])obj;
                char[] str = null;
                for (int i = 0; i < strs.length; i++) {
                    cHandler.startElement(OMConstants.ARRAY_ITEM_NSURI,
                            OMConstants.ARRAY_ITEM_LOCALNAME,
                            OMConstants.ARRAY_ITEM_QNAME ,
                            null);
                    str = strs[i].toCharArray();
                    cHandler.characters(str,0,str.length);
                    cHandler.endElement(OMConstants.ARRAY_ITEM_NSURI,OMConstants.ARRAY_ITEM_LOCALNAME, OMConstants.ARRAY_ITEM_QNAME );
                }

            }else{
                throw new OMException("Unsupported type");
            }
        } catch (SAXException e) {
            throw new OMException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.apache.axis.encoding.Encoder#deSerialize(javax.xml.stream.XMLStreamReader)
     */
    public Object deSerialize(XMLStreamReader xpp) throws AxisFault {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.axis.encoding.Encoder#setObject(java.lang.Object)
     */
    public void setObject(Object obj) {
        this.obj =obj;
    }

}
