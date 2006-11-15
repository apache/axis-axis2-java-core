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
package org.apache.axis2.jaxws.provider.stringmsg;

import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

@WebServiceProvider()
@ServiceMode(value=Service.Mode.MESSAGE)
public class StringMessageProvider implements Provider<String> {
    private static String responseGood = "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header /><soapenv:Body><provider><message>request processed</message></provider></soapenv:Body></soapenv:Envelope>";
    private static String responseBad  = "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Header /><soapenv:Body><provider><message>ERROR:null request received</message><provider></soapenv:Body></soapenv:Envelope>";
    
    public String invoke(String obj) {
        if (obj != null) {
            String str = (String) obj;
            System.out.println(">> StringMessageProvider received a new request");
            System.out.println(">> request [" + str + "]");
            
            return responseGood;
        }
        System.out.println(">> ERROR:null request received");
        return responseBad;
    }
}
