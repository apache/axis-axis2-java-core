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
 *
 *  Runtime state of the engine
 */
package org.apache.axis2.mail;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.mail.server.MailConstants;
import org.apache.axis2.transport.mail.server.MailServer;

import java.io.File;

public class UtilsMailServer {
    private static final String MAIL_TRANSPORT_SERVER_ENABLED_REPO_PATH =
        Constants.TESTING_PATH + "mail-transport-server-enabledRepository";
    private static final String MAIL_TRANSPORT_CLIENT_ENABLED_REPO_PATH =
            Constants.TESTING_PATH + "mail-transport-client-enabledRepository";

    private static MailServer server;
    private static ConfigurationContext SERVER_CONFIG_CONTEXT;
    private static ConfigurationContext CLIENT_CONFIG_CONTEXT;
    private static int runningServerCount = 0;

    public synchronized static ConfigurationContext start() throws Exception {
        
        //start the mail server      
        if (runningServerCount == 0) {
            SERVER_CONFIG_CONTEXT = createServerConfigurationContext();
            server =
                new MailServer(
            SERVER_CONFIG_CONTEXT,
                    MailConstants.POP_SERVER_PORT,
                    MailConstants.SMTP_SERVER_PORT);
        }
        runningServerCount++;
        return SERVER_CONFIG_CONTEXT;
    }
    public static ConfigurationContext createServerConfigurationContext() throws Exception {
        if(SERVER_CONFIG_CONTEXT == null){
            File file = new File(MAIL_TRANSPORT_SERVER_ENABLED_REPO_PATH);
            TestCase.assertTrue(
                "Mail repository directory " + file.getAbsolutePath() + " does not exsist",
                file.exists());
            ConfigurationContextFactory builder = new ConfigurationContextFactory();
            SERVER_CONFIG_CONTEXT =
                builder.buildConfigurationContext(file.getAbsolutePath());
        }
        return SERVER_CONFIG_CONTEXT;
    }
    
    
    public static ConfigurationContext createClientConfigurationContext() throws Exception {
        if(CLIENT_CONFIG_CONTEXT == null){
            File file = new File(MAIL_TRANSPORT_CLIENT_ENABLED_REPO_PATH);
            TestCase.assertTrue(
                "Mail repository directory " + file.getAbsolutePath() + " does not exsist",
                file.exists());
            ConfigurationContextFactory builder = new ConfigurationContextFactory();
            CLIENT_CONFIG_CONTEXT =
                builder.buildConfigurationContext(file.getAbsolutePath());
        }
        return CLIENT_CONFIG_CONTEXT;
    }

    public static synchronized void stop() throws AxisFault{
        runningServerCount--;
        if(runningServerCount == 0){
            server.stop();
        }
    }
}
