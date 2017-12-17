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
package org.apache.axis2.webapp;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.Cookie;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.sourceforge.jwebunit.junit.WebTester;

public class Axis2WebTester extends WebTester implements TestRule {
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setBaseUrl("http://localhost:" + System.getProperty("jetty.httpPort", "8080") + "/axis2");
                base.evaluate();
            }
        };
    }

    public String getSessionId() {
        List<?> cookies = getTestingEngine().getCookies();
        for (Iterator<?> i = cookies.iterator(); i.hasNext();) {
            Cookie cookie = (Cookie)i.next();
            if (cookie.getName().equals("JSESSIONID")) {
                return cookie.getValue();
            }
        }
        String path = getTestingEngine().getPageURL().getPath();
        int idx = path.lastIndexOf(";jsessionid=");
        return idx == -1 ? null : path.substring(idx+12);
    }
}
