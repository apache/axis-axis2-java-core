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
package org.apache.axis2.schema.axis2_5771;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Test;

public class IgnoreUnexpectedTest {
    private void testValue(String value, CabinType expected) {
        Logger logger = Logger.getLogger(CabinType.Factory.class.getName());
        Handler handler = mock(Handler.class);
        logger.addHandler(handler);
        try {
            assertThat(CabinType.Factory.fromValue(value)).isSameAs(expected);
            if (expected == null) {
                verify(handler).publish(any(LogRecord.class));
            } else {
                verifyZeroInteractions(handler);
            }
        } finally {
            logger.removeHandler(handler);
        }
    }

    @Test
    public void testUnexpectedValue() {
        testValue("A", null);
    }

    @Test
    public void testExpectedValue() {
        testValue("C", CabinType.C);
    }
}
