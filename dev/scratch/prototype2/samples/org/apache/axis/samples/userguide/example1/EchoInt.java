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
package org.apache.axis.samples.userguide.example1;

import java.net.URL;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.engine.EngineUtils;

/**
 * @author chathura@opensource.lk
 * 
 */
public class EchoInt {
	
	public static void main(String[] args) throws Exception {
		InteropTest_Stub clientStub = new InteropTest_Stub();
		URL url = new URL("http","127.0.0.1",EngineUtils.TESTING_PORT,"/axis/services/EchoXMLService");
		clientStub.setEnePointReference(new EndpointReference(AddressingConstants.WSA_TO, url.toString()));
		Integer echoInt = clientStub.echoInt(new Integer(794));
		System.out.println(echoInt);
		
	}

}
