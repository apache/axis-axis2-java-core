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
package samples.userguide.sample2.client;

import org.apache.axis.clientapi.AsyncResult;
import org.apache.axis.clientapi.Callback;
import org.apache.axis.engine.AxisFault;

/**
 * @author chathura@opensource.lk
 * 
 */
public class EchoIntCallBackHandler implements Callback {

	
	public void onComplete(AsyncResult result) {
		
		try {
			System.out.println("Clent is called back. The echoed value is :"+new InteropTest_Stub().getEchoIntFromSOAPEnvelop(result.getResponseEnvelope()));
		} catch (AxisFault e) {
			
			e.printStackTrace();
		}		

	}

	
	public void reportError(Exception e) {
		System.out.println("An Error Occured !!!");
		e.printStackTrace();

	}

}
