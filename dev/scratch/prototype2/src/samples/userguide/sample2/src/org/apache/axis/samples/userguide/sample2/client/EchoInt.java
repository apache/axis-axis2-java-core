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
package org.apache.axis.samples.userguide.sample2.client;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineUtils;


/**
 * @author chathura@opensource.lk
 * 
 */
public class EchoInt extends Thread{

	
	
	public static void main(String[] args) throws Exception {
		new EchoInt().start();
		
		Thread.sleep(600000);
	}
	
	public void run(){
		InteropTest_Stub clientStub = new InteropTest_Stub();
		URL url= null;
		try {
			url = new URL("http","127.0.0.1",EngineUtils.TESTING_PORT,"/axis/services/EchoXMLService");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		clientStub.setEnePointReference(new EndpointReference(AddressingConstants.WSA_TO, url.toString()));
		try {
			
			clientStub.echoInt(new Integer(794), new EchoIntCallBackHandler());
		} catch (AxisFault e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

}
