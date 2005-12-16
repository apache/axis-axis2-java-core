/*
* Copyright 2001, 2002,2004 The Apache Software Foundation.
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


package org.apache.axis2.transport.jms;

import org.apache.axis2.util.Loader;

import java.util.HashMap;

/**
 * Discovery class used to locate vendor adapters.  Switch the default
 * JNDI-based implementation by using the
 * org.apache.axis2.transport.jms.JMSVendorAdapter system property
 */
public class JMSVendorAdapterFactory {
    private static HashMap s_adapters = new HashMap();
    private static Loader loader = new Loader();
    private final static String VENDOR_PKG = "org.apache.axis2.transport.jms";

    public static final JMSVendorAdapter getJMSVendorAdapter() throws Exception {
        return (JMSVendorAdapter) Loader.loadClass(VENDOR_PKG + ".JNDIVendorAdapter").newInstance();
    }

    public static final JMSVendorAdapter getJMSVendorAdapter(String vendorId) {

        // check to see if the adapter has already been instantiated
        if (s_adapters.containsKey(vendorId)) {
            return (JMSVendorAdapter) s_adapters.get(vendorId);
        }

        // create a new instance
        JMSVendorAdapter adapter = null;

        try {
            Class vendorClass = Class.forName(getVendorAdapterClassname(vendorId));

            adapter = (JMSVendorAdapter) vendorClass.newInstance();
        } catch (Exception e) {
            return null;
        }

        synchronized (s_adapters) {
            if (s_adapters.containsKey(vendorId)) {
                return (JMSVendorAdapter) s_adapters.get(vendorId);
            }

            if (adapter != null) {
                s_adapters.put(vendorId, adapter);
            }
        }

        return adapter;
    }

    private static String getVendorAdapterClassname(String vendorId) {
        StringBuffer sb = new StringBuffer(VENDOR_PKG).append(".");

        sb.append(vendorId);
        sb.append("VendorAdapter");

        return sb.toString();
    }
}
