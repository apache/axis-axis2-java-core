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
package org.apache.axis2.databinding.axis2_5758;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import org.apache.axis2.databinding.axis2_5758.client.StockQuoteServiceStub;
import org.apache.axis2.databinding.axis2_5758.client.TradePriceRequest;
import org.apache.axis2.databinding.axis2_5758.service.StockQuoteServiceImpl;
import org.apache.axis2.testutils.ClientHelper;
import org.apache.axis2.testutils.jaxws.JAXWSEndpoint;
import org.junit.ClassRule;
import org.junit.Test;

public class ServiceTest {
    @ClassRule
    public static final ClientHelper clientHelper = new ClientHelper("target/repo/client");

    @ClassRule
    public static final JAXWSEndpoint endpoint = new JAXWSEndpoint(new StockQuoteServiceImpl());

    @Test
    public void test() throws Exception {
        StockQuoteServiceStub stub = clientHelper.createStub(StockQuoteServiceStub.class, endpoint.getAddress());
        TradePriceRequest request = new TradePriceRequest();
        request.setTickerSymbol(null);
        assertThat(stub.getLastTradePrice(request).getPrice()).isNaN();
        request.setTickerSymbol("GOOG");
        assertThat(stub.getLastTradePrice(request).getPrice()).isCloseTo(100.0f, offset(0.001f));
    }
}
