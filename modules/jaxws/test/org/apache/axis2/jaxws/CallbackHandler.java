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
package org.apache.axis2.jaxws;

import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

public class CallbackHandler<T> implements AsyncHandler <T> {

    public void handleResponse(Response response) {
        System.out.println(">> Processing async reponse");
        try{
            T res = (T) response.get();
            
            if(res instanceof String){
                System.out.println("Response [" + res + "]");
            }
            else if(res instanceof SAXSource){
            	
    			SAXSource retVal = (SAXSource)res;
    			StringBuffer buffer = new StringBuffer();
    			byte b;
    			while ((b = (byte) retVal.getInputSource().getByteStream().read()) != -1) {
    				char c = (char) b;
    				buffer.append(c);

    			}
    			System.out.println(">> Response [" + buffer + "]");
            }
            else if(res instanceof StreamSource){
            	StreamSource retVal = (StreamSource) res;

    			byte b;
    			StringBuffer buffer = new StringBuffer();
    			while ((b = (byte) retVal.getInputStream().read()) != -1) {
    				char c = (char) b;
    				buffer.append(c);

    			}
    			System.out.println(">> Response [" + buffer + "]");
            }
            else if(res instanceof DOMSource){
            	DOMSource retVal = (DOMSource) res;

            	StringWriter writer = new StringWriter();
    			Transformer trasformer = TransformerFactory.newInstance().newTransformer();
    			Result result = new StreamResult(writer);
    			trasformer.transform(retVal, result);
    			StringBuffer buffer = writer.getBuffer();
    			System.out.println(">> Response [" + buffer + "]");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
