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

package org.apache.axis2.soap12testing.client;

import org.apache.axis2.soap.impl.llom.soap12.SOAP12Constants;
import org.apache.axis2.transport.http.HTTPConstants;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.*;
import java.io.*;
import java.util.Iterator;

public class SOAP12TestClient {
    public String getReply(int port, String webserviceName,String testNumber) {
        String replyMessage = "";
        try {
            URL netUrl = new URL("http://localhost:"+port+"/axis2/services/"+webserviceName+"/echo");

            Socket socket =  new Socket("127.0.0.1",port);

//            HttpURLConnection connection = (HttpURLConnection) netUrl.openConnection();
//            connection.setDoOutput(true);

            SOAPCreater soapCreater = new SOAPCreater();
            String requestMessage = soapCreater.getStringFromSOAPMessage(testNumber,netUrl);
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream());
//                              connection.getOutputStream());
            out.println(requestMessage);
            out.flush();
            out.close();

            BufferedReader reader = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String response = reader.readLine();
            while( null != response ) {
                sb.append(response.trim());
                response = reader.readLine();
            }
            replyMessage = sb.toString();
            socket.close();
//            connection.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return replyMessage;
    }

    public InputStream getRelpy(int port,String webserviceName,String testNumber) {
        try {
            URL netUrl = new URL("http://localhost:"+port+"/axis2/services/"+webserviceName+"/echo");
            Socket socket =  new Socket("127.0.0.1",port);
            SOAPCreater soapCreater = new SOAPCreater();
            String requestMessage = soapCreater.getStringFromSOAPMessage(testNumber,netUrl);
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream());
            System.out.println("Message: " + requestMessage);
            out.println(requestMessage);
            out.flush();
            return socket.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}