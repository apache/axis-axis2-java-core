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

package org.apache.axis2.json.streaming;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.junit.Before;

import java.io.ByteArrayOutputStream;

/**
 * Runs the same field filtering tests as
 * {@link FieldFilteringMessageFormatterTest} but with the GSON-based
 * {@link JSONStreamingMessageFormatter} as the delegate instead of
 * {@link MoshiStreamingMessageFormatter}.
 *
 * <p>This ensures behavioral parity between the two JSON formatters
 * for all field filtering features including dot-notation nested
 * filtering.</p>
 *
 * @since 2.0.1
 */
public class GsonFieldFilteringMessageFormatterTest
        extends FieldFilteringMessageFormatterTest {

    @Override
    @Before
    public void setUp() {
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        outputFormat = new OMOutputFormat();
        outputStream = new ByteArrayOutputStream();

        outMsgContext = new MessageContext();
        outMsgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, "UTF-8");

        // Use GSON delegate instead of Moshi — all inherited tests run
        // against the GSON field filtering implementation
        formatter = new FieldFilteringMessageFormatter(
            new JSONStreamingMessageFormatter());
    }
}
