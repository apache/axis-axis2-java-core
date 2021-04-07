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

package sample.json.client;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class JsonClient{

    private String url = "http://localhost:8080/axis2/services/JsonService/echoUser";

    public static void main(String[] args)throws IOException {

        String echoUser = "{\"echoUser\":[{\"arg0\":{\"name\":\"My_Name\",\"surname\":\"MY_Surname\",\"middleName\":" +
            "\"My_MiddleName\",\"age\":123,\"address\":{\"country\":\"My_Country\",\"city\":\"My_City\",\"street\":" +
            "\"My_Street\",\"building\":\"My_Building\",\"flat\":\"My_Flat\",\"zipCode\":\"My_ZipCode\"}}}]}";

        JsonClient jsonClient = new JsonClient();
        String echo = jsonClient.post(echoUser);
        System.out.println (echo);

    }

    public String post(String message) throws IOException {

        HttpEntity stringEntity = new StringEntity(message,ContentType.APPLICATION_JSON);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(stringEntity);
        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {
            CloseableHttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = null;
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                entity = response.getEntity();
            } else {
                throw new ClientProtocolException("Unexpected HTTP response status: " + status);
            }
            if (entity == null || EntityUtils.toString(entity,"UTF-8") == null) {
                throw new ClientProtocolException("Error connecting to url: "+url+" , unexpected response: " + EntityUtils.toString(entity,"UTF-8"));
            }
            return EntityUtils.toString(entity,"UTF-8");
        }finally {
            httpclient.close();
        }
    }

}
