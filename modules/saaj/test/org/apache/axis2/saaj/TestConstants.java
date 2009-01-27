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

package org.apache.axis2.saaj;

public class TestConstants {
    public static final String MTOM_TEST_MESSAGE_FILE =
            System.getProperty("basedir", ".") + "/test-resources/message.bin";
    public static final String MTOM_TEST_MESSAGE_CONTENT_TYPE =
            "multipart/related; " +
            "boundary=\"MIMEBoundaryurn:uuid:F02ECC18873CFB73E211412748909307\"; " +
            "type=\"application/xop+xml\"; " +
            "start=\"<0.urn:uuid:F02ECC18873CFB73E211412748909308@apache.org>\"; " +
            "start-info=\"text/xml\"; " +
            "charset=UTF-8;" +
            "action=\"mtomSample\"";
    
    private TestConstants() {}
}
