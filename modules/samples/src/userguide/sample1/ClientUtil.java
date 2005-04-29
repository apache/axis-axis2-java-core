
package userguide.sample1;

import org.apache.axis.om.*;

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


public class ClientUtil {

	public static SOAPEnvelope getEchoSoapEnvelop(){
		SOAPFactory omFactory = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope envelope = omFactory.getDefaultEnvelope();
		OMNamespace namespace = envelope.declareNamespace("http://sample1.org/sample1", "sample1");
		
		OMElement bodyContent = omFactory.createOMElement("echo", namespace);
		
		
		OMElement text = omFactory.createOMElement("Text", namespace);
		text.addChild(omFactory.createText("Axis2 Echo String"));
		bodyContent.addChild(text);
		envelope.getBody().addChild(bodyContent);
		return envelope;
	}
}
