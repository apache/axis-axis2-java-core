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

package org.apache.axis2.transport.base.endpoint;

import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLEndpoint {
     private Pattern urlPattern;

    private Map<String, Builder> messageBuilders = new HashMap<String, Builder>();

    private List<Parameter> parameters = new ArrayList<Parameter>();

    private Builder defaultBuilder = new SOAPBuilder();

    public URLEndpoint(Pattern urlPattern) {
        this.urlPattern = urlPattern;
    }

    public boolean isMatching(String url) {
        Matcher matcher = urlPattern.matcher(url);
        return matcher.matches();
    }

    public void addBuilder(String name, Builder builder) {
        messageBuilders.put(name, builder);
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public Parameter getParameter(String name) {
        for (Parameter p : parameters) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setDefaultBuilder(Builder defaultBuilder) {
        this.defaultBuilder = defaultBuilder;
    }

    public Builder getBuilder(String contentType) {
        Builder builder = messageBuilders.get(contentType);
        if (builder == null) {
            return defaultBuilder;
        }

        return builder;
    }

    public void setParameters(MessageContext msgCtx) {
        for (Parameter p : parameters) {
            msgCtx.setProperty(p.getName(), p.getValue());
        }
    }
}
