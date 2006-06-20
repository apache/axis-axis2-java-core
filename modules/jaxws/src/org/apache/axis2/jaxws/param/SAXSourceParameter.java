/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws.param;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.sax.SAXSource;

import org.apache.axiom.om.OMElement;
import org.xml.sax.InputSource;

public class SAXSourceParameter extends SourceParameter {

    public SAXSourceParameter() {
        //do nothing
    }
    
    public SAXSourceParameter(SAXSource value){
		this.value = value;
	}
	
    public void fromOM(OMElement omElement) {
		if(omElement != null){
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try{
                omElement.serialize(output);
            }catch(XMLStreamException e){
                e.printStackTrace();
            }
            InputStream input = new ByteArrayInputStream(output.toByteArray());
            InputSource inputSrc = new InputSource(input);
            
            value = new SAXSource(inputSrc);
        }
		
	}
}
