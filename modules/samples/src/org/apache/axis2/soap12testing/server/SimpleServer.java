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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.SimpleHTTPServer;

import java.io.File;
import java.net.ServerSocket;

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
           // SimpleHTTPServer reciver = new SimpleHTTPServer("./target/Repository", serverSoc);
            SimpleHTTPServer reciver = new SimpleHTTPServer("D:\\Projects\\LSF\\Axis2\\Axis1.0\\modules\\samples\\target\\Repository", serverSoc);
            Thread thread = new Thread(reciver);
            thread.setDaemon(true);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
