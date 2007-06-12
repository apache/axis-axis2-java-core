/*
* $HeadURL$
* $Revision$
* $Date$
*
* ====================================================================
*
*  Copyright 1999-2004 The Apache Software Foundation
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation.  For more
* information on the Apache Software Foundation, please see
* <http://www.apache.org/>.
*/
package org.apache.axis2.transport.http.server;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.Parameter;
import org.apache.http.Header;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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

    /**
     * Returns the ip address to be used for the replyto epr
     * CAUTION:
     * This will go through all the available network interfaces and will try to return an ip address.
     * First this will try to get the first IP which is not loopback address (127.0.0.1). If none is found
     * then this will return this will return 127.0.0.1.
     * This will <b>not<b> consider IPv6 addresses.
     * <p/>
     * TODO:
     * - Improve this logic to genaralize it a bit more
     * - Obtain the ip to be used here from the Call API
     *
     * @return Returns String.
     * @throws SocketException
     */
    public static String getIpAddress() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        String address = "127.0.0.1";

        while (e.hasMoreElements()) {
            NetworkInterface netface = (NetworkInterface) e.nextElement();
            Enumeration addresses = netface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress ip = (InetAddress) addresses.nextElement();
                if (!ip.isLoopbackAddress() && isIP(ip.getHostAddress())) {
                    return ip.getHostAddress();
                }
            }
        }

        return address;
    }

    /**
     * First check whether the hostname parameter is there in AxisConfiguration (axis2.xml) ,
     * if it is there then this will retun that as the host name , o.w will return the IP address.
     */
    public static String getIpAddress(AxisConfiguration axisConfiguration) throws SocketException {
        if(axisConfiguration!=null){
            Parameter param = axisConfiguration.getParameter(TransportListener.HOST_ADDRESS);
            if (param != null) {
                String  hostAddress = ((String) param.getValue()).trim();
                if(hostAddress!=null){
                    return hostAddress;
                }
            }
        }
        return getIpAddress();
    }

    private static boolean isIP(String hostAddress) {
        return hostAddress.split("[.]").length == 4;
    }
}
