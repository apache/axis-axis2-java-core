/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.transport.mail;

import org.apache.axis2.addressing.EndpointReference;
/*
 * 
 */

public class MailToInfo {
    private String emailAddress;
    private String contentDescription;
    private boolean xServicePath;

    public MailToInfo(String eprAddress) {
        //URl validation according to rfc :  http://www.ietf.org/rfc/rfc2368.txt

        int mailToIndex = eprAddress.indexOf(Constants.MAILTO+":");
        if (mailToIndex > -1) {
            eprAddress = eprAddress.substring(mailToIndex + 7);
        }
        int index = eprAddress.indexOf('?');

        if (index > -1) {
            emailAddress = eprAddress.substring(0, index);
        } else {
            emailAddress = eprAddress;
        }

        if (eprAddress.indexOf(Constants.X_SERVICE_PATH) > -1) {
            index = eprAddress.indexOf('=');
            if (index > -1) {
                xServicePath = true;
                contentDescription = eprAddress.substring(index + 1);
            }
        } else {
            contentDescription = eprAddress.substring(index + 1);

        }
    }
    public MailToInfo(EndpointReference epr) {
        this(epr.getAddress());
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    public boolean isxServicePath() {
        return xServicePath;
    }

    public void setxServicePath(boolean xServicePath) {
        this.xServicePath = xServicePath;
    }

}
