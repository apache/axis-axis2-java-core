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
package org.apache.axis2.transport;
import junit.framework.TestCase;
import org.apache.axis2.transport.mail.SimpleMailListener;

public class SimpleMailListnerTest extends TestCase {
    private SimpleMailListener sas;
    public SimpleMailListnerTest(String testName) {
        super(testName);
    }
    
//    public void setUp(){
//        Thread thread = new Thread(new Runnable() {
//            public void run() {
//                boolean optDoThreads = true;
//                String optHostName = "localhost";
//                boolean optUseCustomPort = false;
//                int optCustomPortToUse = 0;
//                String optDir = "FIX_ME_PLS";
//                String optUserName = "server";
//                String optPassword = "server";
//                System.out.println("Starting the mail listner");
//                try {
//                    String host = optHostName;
//                    int port = ((optUseCustomPort) ? optCustomPortToUse : 110);
//                    POP3Client pop3 = new POP3Client();
//                    sas = new SimpleMailListner(host, port, optUserName,
//                            optPassword, optDir);
//                    sas.setDoThreads(optDoThreads);
//                    sas.setPOP3(pop3);
//                    sas.start();
//                } catch (Exception e) {
//                    System.out.println("An error occured in the main method of SimpleMailListner. TODO Detailed error message needs to be inserted here.");
//                    return;
//                }
//
//
//            }
//        }); 
//        thread.start();
//    
//    }
    
    public void testSendViaMailAndRecieve() throws Exception {
//         	// CREATE CLIENT INSTANCE MailClient(String user, String host, String password)
//        	MailClient mailclient = new MailClient("client", "localhost", "client");
//
//        	String fileContents = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">	<soapenv:Header></soapenv:Header>	<soapenv:Body>		<samples:echo xmlns:samples=\"http://apache.ws.apache.org/samples\">	        	<samples:param1 xmlns:arrays=\"http://axis.apache.org/encoding/Arrays\">				<arrays:item>Hello testing1</arrays:item>				<arrays:item>Hello testing2</arrays:item>				<arrays:item>Hello testing3</arrays:item>				<arrays:item>Hello testing4</arrays:item>				<arrays:item>Hello testing5</arrays:item>			</samples:param1>		</samples:echo>	</soapenv:Body></soapenv:Envelope>";
//        	String soapService = "sample1";
//
//        	// SEND A MESSAGE TO THE SERVER
//        	mailclient.sendMessage(
//        	      "server@localhost",
//        	      "Testing SOAP with service - " + soapService,
//        	      fileContents, soapService);
//        	
//        	int count =0;
//        	boolean success = false;
//
//        	while (count<10 && !success) {
//            	Thread.sleep(10000);
//            	success = (mailclient.checkInbox(3)>0);        	    
//        	}
//    
    }
    
    public  void tearDown() throws Exception{
//        sas.stop();
    }
    
}

