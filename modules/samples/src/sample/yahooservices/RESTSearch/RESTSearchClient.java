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

package sample.yahooservices.RESTSearch;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.RESTCall;

public class RESTSearchClient {
    public static void main(String[] args) {
        try {

            String epr = "http://api.search.yahoo.com/WebSearchService/V1/webSearch?appid=ApacheRestDemo&query=finances&format=pdf";

            RESTCall call = new RESTCall();
            Options options = new Options();
            call.setOptions(options);
            options.setTo(new EndpointReference(epr));
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(Constants.Configuration.ENABLE_REST_THROUGH_GET, Constants.VALUE_TRUE);

            //if post is through GET of HTTP
            OMElement response = call.sendReceive();
            response.serialize(System.out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

