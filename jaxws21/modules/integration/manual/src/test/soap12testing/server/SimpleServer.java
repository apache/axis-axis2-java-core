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

package test.soap12testing.server;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import test.soap12testing.client.MessageComparator;

import java.io.File;

public class SimpleServer {
    private int port;
    private static final Log log = LogFactory.getLog(SimpleServer.class);

    public SimpleServer() {
        this.port = 8008;
    }

    public SimpleServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            File file = new File(MessageComparator.TEST_MAIN_DIR + "target/Repository");
            if (!file.exists()) {
                throw new AxisFault(file.getAbsolutePath() + " File does not exist");
            }
            ConfigurationContext configctx =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                            file.getAbsolutePath(), file.getAbsolutePath() + "/axis2.xml");
            SimpleHTTPServer receiver = new SimpleHTTPServer(configctx, port);
            receiver.start();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}
