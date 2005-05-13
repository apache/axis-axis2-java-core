package userguide.sample1;

import org.apache.axis.clientapi.AsyncResult;
import org.apache.axis.clientapi.Callback;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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


public class ClientEchoCallbackHandler extends Callback {

	
	public void onComplete(AsyncResult result) {
		System.out.println("Responce message received to the ClientEchoCallbackHandler ...");
		
		try {
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			result.getResponseEnvelope().serializeWithCache(writer);
			writer.flush();
		} catch (XMLStreamException e) {
			System.out.println("Error occured after responce is received");
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			System.out.println("Error occured after responce is received");
			e.printStackTrace();
		}
		System.out.println();

	}

	
	public void reportError(Exception e) {
		System.out.println("Error occured after responce is received");
		e.printStackTrace();

	}

}
