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

import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OutObject;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public class OutObjectArrayImpl implements OutObject{
    private Object[] obj = null;
    private Encoder arrayTypeEncoder;
    
    public OutObjectArrayImpl(Object[] obj,Encoder arrayTypeEncoder){
       this.obj = obj;
       this.arrayTypeEncoder = arrayTypeEncoder;
    }
 
 
    public void startBuilding(ContentHandler cHandler) throws OMException {
        try {
        	for(int i = 0;i<obj.length;i++){
				cHandler.startElement(OMConstants.ARRAY_ITEM_NSURI,OMConstants.ARRAY_ITEM_LOCALNAME,"",null);
				arrayTypeEncoder.setObject(obj[i]);
				arrayTypeEncoder.startBuilding(cHandler);
				cHandler.endElement(OMConstants.ARRAY_ITEM_NSURI,OMConstants.ARRAY_ITEM_LOCALNAME,"");
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

}
