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

package org.apache.axis2.transport.http.server;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.http.Header;

public class HttpUtils {

    private HttpUtils() {
    }

    public static String getSoapAction(final AxisHttpRequest request) {
        if (request == null) {
            return null;
        }
        Header header = request.getFirstHeader(HTTPConstants.HEADER_SOAP_ACTION);
        if (header != null) {
            return header.getValue();
        } else {
            return null;
        }
    }

    public static int indexOfIngnoreCase(String str , String subStr){
        String lowerStr = str.toLowerCase();
        String lowerSubStr = subStr.toLowerCase();
        return lowerStr.indexOf(lowerSubStr);
    }

    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (str == null || suffix == null) {
            return (str == null && suffix == null);
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        int strOffset = str.length() - suffix.length();
        return str.regionMatches(true, strOffset, suffix, 0, suffix.length());
    }

    public static int lastIndexOfIgnoreCase(String str , String subStr){
        String lowerStr = str.toLowerCase();
        String lowerSubStr = subStr.toLowerCase();
        return lowerStr.lastIndexOf(lowerSubStr);
    }
}
