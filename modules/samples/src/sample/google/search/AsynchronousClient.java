
package sample.google.search;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.OperationDescription;
import org.apache.axis.engine.AxisFault;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

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


public class AsynchronousClient {

    /**
         * Query parameter
         */
        protected static String search = "";

        /**
         * Query parameters sent with the last request
         */
        protected static String prevSearch = "";

        /**
         * Have to increase and set the start index when asking for more results
         */
        protected static int StartIndex = 0;

        /**
         * License key
         */
        protected static String key;

        /** properties file to store the license key*/
        protected static Properties prop;

         /** maximum results per page */
        protected static String maxResults = String.valueOf(2);

        /**when this is set, thread sends a new request */
        protected static boolean doSearch = false;





    public static void main(String[] args) {

        LinkFollower page = new LinkFollower();
        LinkFollower.showURL = false;


        GUIHandler gui = new GUIHandler();
        System.out.println(search);
        prop = new Properties();

        Class clazz = new Object().getClass();
        InputStream stream = clazz.getResourceAsStream("/sample/google/search/key.properties");




        try {
            prop.load(stream);
            key = prop.getProperty("Key");
            if (key==null) {
                gui.setKey();
            }
        } catch (IOException e) {
           e.printStackTrace();
        }

        gui.buildFrame();
        Thread linkThread = new Thread(page);
        Thread guiThread = new Thread(gui);

        guiThread.start();
        linkThread.start();
        guiThread.run();
        linkThread.run();

    }

    public AsynchronousClient() {

    }


    public static void sendMsg() throws AxisFault {

        search.trim();
        prevSearch = search;
        Call call = new Call();
        URL url = null;
        try {
            url = new URL("http", "api.google.com", "/search/beta2");
            //url = new URL("http", "localhost",8080, "/search/beta2");
        } catch (MalformedURLException e) {

            e.printStackTrace();
            System.exit(0);
        }

        call.setTo(new EndpointReference(AddressingConstants.WSA_TO, url.toString()));


        MessageContext requestContext = ClientUtil.getMessageContext();
        try {
            call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
            QName opName = new QName("urn:GoogleSearch", "doGoogleSearch");
            OperationDescription opdesc = new OperationDescription(opName);
            call.invokeNonBlocking(opdesc, requestContext, new ClientCallbackHandler());
        } catch (AxisFault e1) {
            e1.printStackTrace();
        }
    }


}



