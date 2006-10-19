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


package org.apache.axis2.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;

/**
 * Class TransportListener
 */
public interface TransportListener {

    public static final String PARAM_PORT = "port";
    public static final String HOST_ADDRESS="hostname";

    void init(ConfigurationContext axisConf, TransportInDescription transprtIn)
            throws AxisFault;

    void start() throws AxisFault;

    void stop() throws AxisFault;

    /**
     * @deprecated Transport listener can expose more than EPRs. So this method should return an array of EPRs.
     * Deprecating this method for now and please use getEPRsForServices instead.
     * @param serviceName
     * @param ip
     * @throws AxisFault
     */
    EndpointReference getEPRForService(String serviceName, String ip) throws AxisFault;

    public EndpointReference[] getEPRsForService(String serviceName, String ip) throws AxisFault;
}
