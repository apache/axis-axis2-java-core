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

package org.apache.axis2.transport.mail.server;

import javax.mail.Address;

/**
 * This is a simple implementation to simplify the usage
 * of the Addresses.
 */
public class MailAddress extends Address {

    private static final long serialVersionUID = 3033256355495000819L;

    String mailAddy = null;

    public MailAddress(String mAddy) {
        this.mailAddy = mAddy;
    }

    public boolean equals(Object addr) {
        return this.mailAddy.equals(addr);
    }

    public String toString() {
        return this.mailAddy;
    }

    public String getType() {
        return "text/plain";
    }
}
