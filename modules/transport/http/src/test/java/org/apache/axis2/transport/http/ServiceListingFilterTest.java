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
import static org.mockito.Mockito.when;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;

/**
 * The service listing is filtered before it reaches the request attributes rather
 * than only in the view, so that a service the operator withheld cannot be rendered
 * by any other consumer of the model.
 *
 * <p>Two separate controls withhold a service: the {@code hiddenService} parameter,
 * and {@code exposeServiceMetadata}. The standalone transport's lister applies both,
 * and so does this one.
 */
public class ServiceListingFilterTest extends TestCase {

    private ConfigurationContext configContext;

    @Override
    protected void setUp() throws Exception {
        configContext = ConfigurationContextFactory.createEmptyConfigurationContext();

        configContext.getAxisConfiguration().addService(new AxisService("Visible"));

        AxisService notExposed = new AxisService("NotExposed");
        notExposed.addParameter(AxisService.EXPOSE_SERVICE_METADATA, "false");
        configContext.getAxisConfiguration().addService(notExposed);

        AxisService hidden = new AxisService("HiddenParam");
        hidden.addParameter(Constants.HIDDEN_SERVICE_PARAM_NAME, "true");
        configContext.getAxisConfiguration().addService(hidden);
    }

    @SuppressWarnings("unchecked")
    private Map<String, AxisService> listedServices() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        when(req.getRequestURL())
                .thenReturn(new StringBuffer("http://localhost:8080/axis2/services/"));
        final Map<String, AxisService>[] captured = new Map[1];
        // renderView needs a container, so capture the model the JSP would receive.
        org.mockito.Mockito.doAnswer(invocation -> {
            if ("sortedServices".equals(invocation.getArgument(0))) {
                captured[0] = (Map<String, AxisService>) invocation.getArgument(1);
            }
            return null;
        }).when(req).setAttribute(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
        try {
            new ListingAgent(configContext).processListServices(req, res);
        } catch (Exception rendering) {
            // The view cannot render without a container; the model is what matters.
        }
        assertNotNull("the listing model should have been populated", captured[0]);
        return captured[0];
    }

    public void testAnExposedServiceIsListed() throws Exception {
        assertTrue(listedServices().containsKey("Visible"));
    }

    public void testAServiceWithMetadataExposureDisabledIsNotListed() throws Exception {
        assertFalse("the listing names the service, its EPR and its operations",
                listedServices().containsKey("NotExposed"));
    }

    /**
     * The hiddenService parameter is a separate control, and the servlet listing has
     * to apply it in the model too -- not only in listServices.jsp.
     */
    public void testAServiceHiddenByParameterIsNotListed() throws Exception {
        assertFalse(listedServices().containsKey("HiddenParam"));
    }

    /** Nothing null reaches the model, since the view would throw on it. */
    public void testNoNullServiceReachesTheModel() throws Exception {
        for (AxisService service : listedServices().values()) {
            assertNotNull("a null service would break rendering the page", service);
        }
    }
}
