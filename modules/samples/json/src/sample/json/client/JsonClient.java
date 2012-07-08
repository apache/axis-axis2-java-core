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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class JsonClient{

    private String url = "http://localhost:8080/axis2/services/JsonService/echoUser";
    private String contentType = "application/json-impl";
    private String charSet = "UTF-8";

    public static void main(String[] args)throws IOException {
        String echoUser = "{\"echoUser\":{\"user\":{\"name\":\"My_Name\",\"surname\":\"MY_Surname\",\"middleName\":" +
            "\"My_MiddleName\",\"age\":123,\"address\":{\"country\":\"My_Country\",\"city\":\"My_City\",\"street\":" +
            "\"My_Street\",\"building\":\"My_Building\",\"flat\":\"My_Flat\",\"zipCode\":\"My_ZipCode\"}}}}";

        JsonClient jsonClient = new JsonClient();
        jsonClient.post(echoUser);
    }

    public boolean post(String message) throws UnsupportedEncodingException {
        PostMethod post = new PostMethod(url);
        RequestEntity entity = new StringRequestEntity(message , contentType, charSet);
        post.setRequestEntity(entity);
        HttpClient httpclient = new HttpClient();
        try {
            int result = httpclient.executeMethod(post);
            System.out.println("Response status code: " + result);
            System.out.println("Response body: ");
            System.out.println(post.getResponseBodyAsString());
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            post.releaseConnection();
        }
       return false;
    }
}
