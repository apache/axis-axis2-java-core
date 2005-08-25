/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.oasis.ping;

import org.xmlsoap.ping.PingResponse;
import org.xmlsoap.ping.PingResponseDocument;
    /**
     *  Auto generated java skeleton for the service by the Axis code generator
     */
    public class PingPortSkeleton {
     
		 
        /**
         * Auto generated method signature
          * @param param4
         
         */
        public  org.xmlsoap.ping.PingResponseDocument Ping(org.xmlsoap.ping.PingDocument param4 ){
        	PingResponseDocument response = PingResponseDocument.Factory.newInstance();
        	PingResponse pingRes = response.addNewPingResponse();
        	pingRes.setText("Response: " + param4.getPing().getText());
            return response;
        }
     
    }
    