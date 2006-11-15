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

package test.soap12testing.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class SOAP12TestClient {

	private static final Log log = LogFactory.getLog(SOAP12TestClient.class);

    public String getReply(int port, String webserviceName,String testNumber) {
        String replyMessage = "";
        try {
            URL netUrl = new URL("http://localhost:"+port+"/axis2/services/"+webserviceName+"/echo");

            Socket socket =  new Socket("127.0.0.1",port);

            SOAPCreater soapCreater = new SOAPCreater();
            String requestMessage = soapCreater.getStringFromSOAPMessage(testNumber,netUrl);
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream());
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

        } catch (MalformedURLException e) {
            log.info(e.getMessage());
        } catch (IOException e) {
            log.info(e.getMessage());
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
            out.println(requestMessage);
            out.flush();
            socket.shutdownOutput();
            return socket.getInputStream();
        } catch (MalformedURLException e) {
            log.info(e.getMessage());
        } catch (IOException e) {
            log.info(e.getMessage());
        }
        return null;
    }

}
