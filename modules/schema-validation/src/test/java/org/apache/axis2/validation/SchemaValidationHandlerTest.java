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
package org.apache.axis2.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class SchemaValidationHandlerTest {

    @Test
    public void testAppendRefHintForCvcComplexType322() {
        SAXException ex = new SAXException(
                "cvc-complex-type.3.2.2: Attribute 'contentType' is not allowed to appear in element 'foo'");
        String hint = SchemaValidationHandler.appendRefHint(ex);
        assertThat(hint).contains("xs:attribute ref=");
        assertThat(hint).contains("xmime:contentType");
        assertThat(hint).contains("not imported or could not be resolved");
    }

    @Test
    public void testAppendRefHintReturnsEmptyForOtherErrors() {
        SAXException ex = new SAXException("cvc-type.3.1.3: some other validation error");
        String hint = SchemaValidationHandler.appendRefHint(ex);
        assertThat(hint).isEmpty();
    }

    @Test
    public void testAppendRefHintHandlesNullMessage() {
        SAXException ex = new SAXException((String) null);
        String hint = SchemaValidationHandler.appendRefHint(ex);
        assertThat(hint).isEmpty();
    }
}
