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

import org.apache.commons.httpclient.URI;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * JMSURLHelper provides access to properties in the URL.
 * The URL must be of the form: "jms:/<destination>?[<property>=<key>&]*"
 */
public class JMSURLHelper {

    // application-specific JMS message properties
    private Vector appProperties;

    // the only property not in the query string
    private String destination;

    // vendor-specific properties
    private HashMap properties;

    // required properties
    private Vector requiredProperties;

    public JMSURLHelper(String url) throws Exception {
        this(new URI(url), null);
    }

    public JMSURLHelper(URI url) throws MalformedURLException {
        this(url, null);
    }

    public JMSURLHelper(URI url, String[] requiredProperties)
            throws MalformedURLException {
        properties = new HashMap();
        appProperties = new Vector();

        // the path should be something like '/SampleQ1'
        // clip the leading '/' if there is one
        destination = url.getEscapedPath();

        if (destination.startsWith("/")) {
            destination = destination.substring(1);
        }

        if ((destination == null) || (destination.trim().length() < 1)) {
            throw new java.net.MalformedURLException("Missing destination in URL");
        }

        // parse the query string and populate the properties table
        String query = url.getEscapedQuery();

        if (query != null) {
            StringTokenizer st = new StringTokenizer(query, "&;");

            while (st.hasMoreTokens()) {
                String keyValue = st.nextToken();
                int eqIndex = keyValue.indexOf("=");

                if (eqIndex > 0) {
                    String key = keyValue.substring(0, eqIndex);
                    String value = keyValue.substring(eqIndex + 1);

                    if (key.startsWith(JMSConstants._MSG_PROP_PREFIX)) {
                        key = key.substring(JMSConstants._MSG_PROP_PREFIX.length());
                        addApplicationProperty(key);
                    }

                    properties.put(key, value);
                }
            }
        }

        // set required properties
        addRequiredProperties(requiredProperties);
        validateURL();
    }

    /**
     * Adds the name of a property from the url properties that should
     * be added to the JMS message.
     */
    public void addApplicationProperty(String property) {
        if (property == null) {
            return;
        }

        if (appProperties == null) {
            appProperties = new Vector();
        }

        appProperties.addElement(property);
    }

    /**
     * Adds the name and value od the application property to the
     * JMS URL.
     */
    public void addApplicationProperty(String property, String value) {
        if (property == null) {
            return;
        }

        if (appProperties == null) {
            appProperties = new Vector();
        }

        properties.put(property, value);
        appProperties.addElement(property);
    }

    public void addRequiredProperties(String[] properties) {
        if (properties == null) {
            return;
        }

        for (int i = 0; i < properties.length; i++) {
            addRequiredProperty(properties[i]);
        }
    }

    public void addRequiredProperty(String property) {
        if (property == null) {
            return;
        }

        if (requiredProperties == null) {
            requiredProperties = new Vector();
        }

        requiredProperties.addElement(property);
    }

    /**
     * Returns a formatted URL String with the assigned properties
     */
    public String toString() {
        return getURLString();
    }

    private void validateURL() throws MalformedURLException {
        Vector required = getRequiredProperties();

        if (required == null) {
            return;
        }

        for (int i = 0; i < required.size(); i++) {
            String key = (String) required.elementAt(i);

            if (properties.get(key) == null) {
                throw new MalformedURLException();
            }
        }
    }

    /**
     * Returns a collection of properties that are defined within the
     * JMS URL to be added directly to the JMS messages.
     *
     * @return collection or null depending on presence of elements
     */
    public Vector getApplicationProperties() {
        return appProperties;
    }

    public String getDestination() {
        return destination;
    }

    public String getDomain() {
        return getPropertyValue(JMSConstants._DOMAIN);
    }

    public HashMap getProperties() {
        return properties;
    }

    public String getPropertyValue(String property) {
        return (String) properties.get(property);
    }

    public Vector getRequiredProperties() {
        return requiredProperties;
    }

    /**
     * Returns a URL formatted String. The properties of the URL may not
     * end up in the same order as the JMS URL that was originally used to
     * create this object.
     */
    public String getURLString() {
        StringBuffer text = new StringBuffer("jms:/");

        text.append(getDestination());
        text.append("?");

        Map props = (Map) properties.clone();
        boolean firstEntry = true;

        for (Iterator itr = properties.keySet().iterator(); itr.hasNext();) {
            String key = (String) itr.next();

            if (!firstEntry) {
                text.append("&");
            }

            if (appProperties.contains(key)) {
                text.append(JMSConstants._MSG_PROP_PREFIX);
            }

            text.append(key);
            text.append("=");
            text.append(props.get(key));
            firstEntry = false;
        }

        return text.toString();
    }

    public String getVendor() {
        return getPropertyValue(JMSConstants._VENDOR);
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
