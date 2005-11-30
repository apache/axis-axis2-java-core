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

package sample.amazon.search;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.OutInAxisOperation;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * This class implements main() method and sendMsg() methods,
 * check the availability of License key
 * Builds call objects and set it's properties,
 * Also keep few important variables as static and protected those frequently need by other classes
 *
 * @auther Gayan Asanka  (gayan@opensource.lk)
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
     * License key
     */
    protected static String amazonkey;

    /**
     * properties file to store the license key
     */
    protected static Properties prop;

    /**
     * maximum results per page
     */
    protected static String maxResults = String.valueOf(2);

    /**
     * when this is set, thread sends a new request
     */
    protected static boolean doSearch = false;

    public static void main(String[] args) {
        GUIHandler gui;
        Thread linkThread, guiThread;
        LinkFollower page;
        File propertyFile;

        page = new LinkFollower(); //this object used to build the linkfollower thread
        LinkFollower.showURL = false;
        gui = new GUIHandler();

        /*check the license key, if it is not there, ask user to enter the key*/

        prop = new Properties();
        try {
            String workingDir = System.getProperty("user.dir");
            propertyFile = new File(workingDir + File.separator + "samples" +
                    File.separator +
                    "key.properties");
            propertyFile.createNewFile();
            prop.load(new FileInputStream(propertyFile));
            amazonkey = prop.getProperty("amazonKey");
            //System.out.println("key is " + amazonkey);
            if (amazonkey == null) {
                gui.setKey();
            }
            prop = null;     //Useless from now onwards

        } catch (IOException e) {
            e.printStackTrace();
        }
        gui.buildFrame();  //GUI is built and desplayed

        linkThread = new Thread(page);
        guiThread = new Thread(gui);

        guiThread.start();
        linkThread.start();
        guiThread.run();    //To listen to the GUI and send messages
        linkThread.run();   //To detect hyperLink actions and open Simple Web Browser
    }

    /**
     * constructor
     */
    public AsynchronousClient() {

    }

    /**
     * Method sendMsg()
     * build the Call Object
     * Set the URL :  To use TCP monitor comment the line where url is hard coded
     * and uncomment the following line
     * Get built the messageContext
     * Invoke the sending operation
     *
     * @throws AxisFault
     */
    public static void sendMsg() throws AxisFault {
        search.trim();
        prevSearch = search;
        Call call = new Call();
        URL url = null;
        try {
            url =
                    new URL("http",
                            "soap.amazon.com",
                            "/onca/soap?Service=AlexaWebInfoService");

            /** Uncomment the folowing to use TCP Monitor, and comment the above */

            //url = new URL("http", "localhost",8080, "/onca/soap?Service=AlexaWebInfoService");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Options options = new Options();
        call.setClientOptions(options);
        options.setTo(
                new EndpointReference(url.toString()));
        MessageContext requestContext = ClientUtil.getMessageContext();
        try {
            options.setTransportInfo(Constants.TRANSPORT_HTTP,
                    Constants.TRANSPORT_HTTP,
                    false);

            requestContext.getEnvelope().serializeAndConsume(
                    XMLOutputFactory.newInstance()
                            .createXMLStreamWriter
                            (System.out));
            QName opName = new QName("urn:GoogleSearch", "doGoogleSearch");
            AxisOperation opdesc = new OutInAxisOperation(opName);
            call.invokeNonBlocking(opdesc,
                    requestContext,
                    new ClientCallbackHandler());
        } catch (AxisFault e1) {
            e1.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
}





