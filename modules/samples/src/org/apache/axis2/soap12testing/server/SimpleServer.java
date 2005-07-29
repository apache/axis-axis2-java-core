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

package org.apache.axis2.soap12testing.server;

import java.io.File;
import java.net.ServerSocket;

import org.apache.axis2.AxisFault;
import org.apache.axis2.soap12testing.client.MessageComparator;
import org.apache.axis2.transport.http.SimpleHTTPServer;

public class SimpleServer {
    private int port;

    public SimpleServer() {
        this.port = 8008;
    }

    public SimpleServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            ServerSocket serverSoc = null;
            serverSoc = new ServerSocket(port);
            File file = new File(MessageComparator.TEST_MAIN_DIR+ "target/Repository");
            if(!file.exists()){
                throw new AxisFault(file.getAbsolutePath() + " File does not exisits");
            }
            SimpleHTTPServer reciver = new SimpleHTTPServer(file.getAbsolutePath(), serverSoc);
            Thread thread = new Thread(reciver);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
