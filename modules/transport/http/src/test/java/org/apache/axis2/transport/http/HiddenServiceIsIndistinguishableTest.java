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
package org.apache.axis2.transport.http;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;

/**
 * Hiding a service with {@code exposeServiceMetadata=false} is meant to leave it
 * indistinguishable from one that was never deployed. Answering the query routes with
 * 403 for hidden and 404 for absent defeated that: probing {@code ?wsdl} across
 * candidate names told an anonymous caller which ones existed.
 *
 * <p>RFC 9110 section 15.5.4 explicitly permits answering 404 to conceal a forbidden
 * resource's existence, which is what these tests require.
 */
public class HiddenServiceIsIndistinguishableTest extends TestCase {

    private static final String BASE = "http://localhost:8080/axis2/services/";

    private ConfigurationContext configContext;

    @Override
    protected void setUp() throws Exception {
        configContext = ConfigurationContextFactory.createEmptyConfigurationContext();
        AxisService hidden = new AxisService("Hidden");
        hidden.addParameter(AxisService.EXPOSE_SERVICE_METADATA, "false");
        configContext.getAxisConfiguration().addService(hidden);
    }

    private HttpServletResponse ask(String serviceName, String query) throws Exception {
        String url = BASE + serviceName;
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        when(req.getRequestURL()).thenReturn(new StringBuffer(url));
        when(req.getQueryString()).thenReturn(query);
        new ListingAgent(configContext).processListService(req, res);
        return res;
    }

    /** The two answers must be the same, status and message alike. */
    public void testHiddenServiceAnswersExactlyAsAnAbsentOneDoes() throws Exception {
        for (String query : new String[] {"wsdl", "wsdl2", "xsd", "policy"}) {
            HttpServletResponse hidden = ask("Hidden", query);
            verify(hidden).sendError(HttpServletResponse.SC_NOT_FOUND, BASE + "Hidden");
            verify(hidden, never()).sendError(HttpServletResponse.SC_FORBIDDEN);

            HttpServletResponse absent = ask("NeverDeployed", query);
            verify(absent).sendError(HttpServletResponse.SC_NOT_FOUND, BASE + "NeverDeployed");
        }
    }

    /** The oracle also has to be closed for a service that is merely not there. */
    public void testNoRouteEverAnswers403() throws Exception {
        for (String query : new String[] {"wsdl", "wsdl2", "xsd", "policy"}) {
            verify(ask("Hidden", query), never())
                    .sendError(HttpServletResponse.SC_FORBIDDEN);
            verify(ask("NeverDeployed", query), never())
                    .sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
