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

import java.io.File;

import junit.framework.TestCase;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.mail.server.MailConstants;
import org.apache.axis2.transport.mail.server.MailServer;

public class UtilsMailServer {
    private static final String MAIL_TRANSPORT_ENABLED_REPO_PATH =
        Constants.TESTING_PATH + "mail-transport-enabledRepository";

    private static MailServer server;
    private static ConfigurationContext configContext;

    public static ConfigurationContext start() throws Exception {
        
        //start the mail server      
        if (server == null) {
            configContext = createNewConfigurationContext();
            MailServer server =
                new MailServer(
                    configContext,
                    MailConstants.POP_SERVER_PORT,
                    MailConstants.SMTP_SERVER_PORT);
        }
        return configContext;
    }
    public static ConfigurationContext createNewConfigurationContext() throws Exception {
        File file = new File(MAIL_TRANSPORT_ENABLED_REPO_PATH);
        TestCase.assertTrue(
            "Mail repository directory " + file.getAbsolutePath() + " does not exsist",
            file.exists());
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        ConfigurationContext configContext =
            builder.buildConfigurationContext(file.getAbsolutePath());
        return configContext;
    }
    public static void stop(){}
}
