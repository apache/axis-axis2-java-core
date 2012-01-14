/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package userguide.clients;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;

/**
 * Sample for asynchronous single channel non-blocking service invocation.
 * Message Exchange Pattern IN-OUT
 */
public class EchoNonBlockingClient {
    private static EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:8080/axis2/services/MyService");
 
    public static void main(String[] args) {
        ServiceClient sender = null;
        try {
            OMElement payload = ClientUtil.getEchoOMElement();
            Options options = new Options();
            options.setTo(targetEPR);
            options.setAction("urn:echo");

            TestCallback axisCallback = new TestCallback("CallBack1") ;
            
            //Non-Blocking Invocation
            sender = new ServiceClient();
            sender.setOptions(options);
            sender.sendReceiveNonBlocking(payload, axisCallback);
            
            while ( ! axisCallback.isComplete( ) ) {
                Thread.sleep(100);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                sender.cleanup();
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();
            }
        }

    }
    
    static class TestCallback implements AxisCallback {

        private String name = null;
        private boolean complete = false;
        
        public TestCallback (String name) {
                this.name = name;
        }

        public void onError (Exception e) {
                e.printStackTrace();
        }

        public void onComplete() {
        	System.out.println( "Message transmission complete") ;
            complete = true;
        }
        
        public boolean isComplete() {
            return complete;
        }
        
        public void onMessage(org.apache.axis2.context.MessageContext arg0) {
           System.out.println( "Call Back " + name + " got Result: " + arg0.getEnvelope() ) ;
        }

        public void onFault(org.apache.axis2.context.MessageContext arg0) {
        	System.out.println( "Call Back " + name + " got Fault: " + arg0.getEnvelope() ) ;
        }
    }
  
}
