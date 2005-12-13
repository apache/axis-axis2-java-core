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

package sample.google.search;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.OutInAxisOperation;
import sample.google.common.util.PropertyLoader;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

public class AsynchronousClient {

    /**
     * Query parameter
     */
    protected String search = "";

    /**
     * Query parameters sent with the last request
     */
    protected String prevSearch = "";

    /**
     * Have to increase and set the start index when asking for more results
     */
    protected int StartIndex = 0;

    /**
     * License key
     */
    protected String key;

    /**
     * maximum results per page
     */
    protected String maxResults = String.valueOf(10);


    private GUIHandler gui;


    public static void main(String[] args) {
        new AsynchronousClient();
    }

    public AsynchronousClient() {

        this.key = PropertyLoader.getGoogleKey();
        LinkFollower page = new LinkFollower();
        LinkFollower.showURL = false;
        gui = new GUIHandler(this);
        gui.buildFrame();

        Thread linkThread = new Thread(page);
        linkThread.start();
        linkThread.run();
    }


    public synchronized void sendMsg() throws AxisFault {
        search.trim();
        prevSearch = search;
        Call call = new Call();
        URL url = null;
        try {
            url = new URL("http", "api.google.com", "/search/beta2");
            //  url = new URL("http://127.0.0.1:8080/axis2/services/axisversion/viewVersion");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(0);
        }

        Options options = new Options();
        call.setClientOptions(options);
        options.setTo(
                new EndpointReference(url.toString()));

        MessageContext requestContext = ClientUtil.getMessageContext(this);
        try {
            options.setListenerTransportProtocol(Constants.TRANSPORT_HTTP);
            QName opName = new QName("urn:GoogleSearch", "doGoogleSearch");
            AxisOperation opdesc = new OutInAxisOperation();
            opdesc.setName(opName);
            //   AxisOperation opdesc = new AxisOperation(new QName("viewVersion"));
            call.invokeNonBlocking(opdesc,
                    requestContext,
                    new ClientCallbackHandler(this.gui));

        } catch (AxisFault e1) {
            e1.printStackTrace();
        }
    }

    public String getSearch() {
        return search;
    }

    public String getPrevSearch() {
        return prevSearch;
    }

    public int getStartIndex() {
        return StartIndex;
    }

    public String getKey() {
        return key;
    }

    public String getMaxResults() {
        return maxResults;
    }


    public void setSearch(String search) {
        this.search = search;
    }

    public void setPrevSearch(String prevSearch) {
        this.prevSearch = prevSearch;
    }

    public void setStartIndex(int startIndex) {
        StartIndex = startIndex;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMaxResults(String maxResults) {
        this.maxResults = maxResults;
    }


}



