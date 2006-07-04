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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

public class RESTSearchClient {
    public static void main(String[] args) {
        try {

            String epr = "http://api.search.yahoo.com/WebSearchService/V1/webSearch";

            ServiceClient client = new ServiceClient();
            Options options = new Options();
            client.setOptions(options);
            options.setTo(new EndpointReference(epr));
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            options.setProperty(Constants.Configuration.HTTP_METHOD, Constants.Configuration.HTTP_METHOD_GET);

            //if post is through GET of HTTP
            OMElement response = client.sendReceive(getPayloadForYahooSearchCall());
            System.out.println("response = " + response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static OMElement getPayloadForYahooSearchCall() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement rootElement = fac.createOMElement("webSearch", null);

        OMElement appId = fac.createOMElement("appid", null, rootElement);
        appId.setText("ApacheRestDemo");

        OMElement query = fac.createOMElement("query", null, rootElement);
        query.setText("Axis2");

        OMElement format = fac.createOMElement("format", null, rootElement);
        format.setText("pdf");

        return rootElement;
    }


}

