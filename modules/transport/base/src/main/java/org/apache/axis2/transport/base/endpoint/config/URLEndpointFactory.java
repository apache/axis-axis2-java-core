/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.base.endpoint.config;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.base.endpoint.URLEndpoint;
import org.apache.axis2.transport.base.endpoint.URLEndpointsConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.regex.Pattern;

public class URLEndpointFactory {
    private static final Log log = LogFactory.getLog(URLEndpointFactory.class);

    public URLEndpoint create(OMElement xml) throws AxisFault {
        OMAttribute urlPatternAttr = xml.getAttribute(new QName(URLEndpointsConfiguration.URL_PATTERN));
        if (urlPatternAttr == null) {
            handleException(URLEndpointsConfiguration.URL_PATTERN +
                    " attribute is mandory for an URLEndpoint configuration");
            return null;
        }

        String pattern = urlPatternAttr.getAttributeValue();
        URLEndpoint endpoint = new URLEndpoint(Pattern.compile(pattern));

        OMElement messageBuilders = xml.getFirstChildWithName(
                new QName(URLEndpointsConfiguration.MESSAGE_BUILDERS));

        if (messageBuilders != null) {
            OMAttribute defaultBuilderAttr = messageBuilders.getAttribute(
                    new QName("defaultBuilder"));
            if (defaultBuilderAttr != null) {
                Builder builder = loadBuilder(defaultBuilderAttr.getAttributeValue());
                if (builder != null) {
                    endpoint.setDefaultBuilder(builder);
                }
            }

            Iterator it = messageBuilders.getChildrenWithName(
                    new QName(URLEndpointsConfiguration.MESSAGE_BUILDER));
            while(it.hasNext()) {
                OMElement builderElement = (OMElement) it.next();

                OMAttribute contentTypeAttr = builderElement.getAttribute(
                        new QName(URLEndpointsConfiguration.CONTENT_TYPE));
                if (contentTypeAttr == null) {
                    handleException(URLEndpointsConfiguration.CONTENT_TYPE +
                            " attribute cannot be null for URLEndpoint " +
                            "with the " + URLEndpointsConfiguration.URL_PATTERN + " : " + pattern);
                }

                OMAttribute classAttr = builderElement.getAttribute(
                        new QName(URLEndpointsConfiguration.CLASS));
                if (classAttr == null) {
                    handleException(URLEndpointsConfiguration.CLASS +
                            " attribute cannot be null for URLEndpoint " +
                            "with the " + URLEndpointsConfiguration.URL_PATTERN + " : " + pattern);
                }

                if (classAttr != null && contentTypeAttr != null) {
                    Builder builder = loadBuilder(classAttr.getAttributeValue());
                    if (builder != null) {
                        endpoint.addBuilder(contentTypeAttr.getAttributeValue(), builder);
                    }
                }
            }
        }

        Iterator paramItr = xml.getChildrenWithName(
                new QName(URLEndpointsConfiguration.PARAMETER));
        while (paramItr.hasNext()) {
            OMElement p = (OMElement) paramItr.next();
            OMAttribute paramNameAttr = p.getAttribute(new QName(URLEndpointsConfiguration.NAME));
            if (paramNameAttr == null) {
                handleException("Parameter " + URLEndpointsConfiguration.NAME + " cannot be null");
            } else {
                endpoint.addParameter(new Parameter(paramNameAttr.getAttributeValue(), p.getText()));
            }
        }

        return endpoint;
    }

    private Builder loadBuilder(String name) throws AxisFault {
        try {
            if (name != null) {
                Class c = Class.forName(name);
                Object o = c.newInstance();
                if (o instanceof Builder) {
                    return (Builder) o;
                } else {
                    handleException("Class : " + name +
                            " should be a Builder");
                }
            }
        } catch (ClassNotFoundException e) {
            handleException("Error creating builder: " + name, e);
        } catch (InstantiationException e) {
            handleException("Error initializing builder: " + name, e);
        } catch (IllegalAccessException e) {
            handleException("Error initializing builder: " + name, e);
        }

        return null;
    }

    private void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }
}
