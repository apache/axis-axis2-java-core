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

import org.apache.axis.om.OMException;
import org.apache.axis.om.OutObject;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public class OutObjectImpl implements OutObject{
    private ContentHandler cHandler;
    private Object obj = null;
    public OutObjectImpl(Object obj){
       this.obj = obj;
    }
    public ContentHandler getContentHandler() {
        return cHandler;
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.cHandler = contentHandler;
    }

    public void startBuilding() throws OMException {
        try {
            if(obj instanceof String){
                char[] str = ((String)obj).toCharArray();
                cHandler.characters(str,0,str.length);
            }else if(obj instanceof Integer){
                char[] str = obj.toString().toCharArray();
                cHandler.characters(str,0,str.length);
            }else{
                throw new OMException("Unsupported type");
            }
        } catch (SAXException e) {
            throw new OMException(e);
        }

    }

}
