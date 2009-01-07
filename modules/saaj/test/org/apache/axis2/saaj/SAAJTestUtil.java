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

import javax.xml.soap.MessageFactory;

import junit.framework.Assert;

/**
 * Utility to execute SAAJ tests.
 * It executes test cases twice: once against Sun's SAAJ implementation
 * and once against Axis2's. This allows us to cross-check the validity
 * of these tests, i.e. to check whether we are testing the right thing.
 */
public class SAAJTestUtil {
    public interface Test {
        void execute(MessageFactory mf) throws Exception;
    }
    
    private SAAJTestUtil() {}
    
    public static void execute(Test test) throws Exception {
        try {
            test.execute(new com.sun.xml.messaging.saaj.soap.MessageFactoryImpl());
        } catch (Throwable ex) {
            ex.printStackTrace();
            Assert.fail("Invalid test case; execution failed with SAAJ reference implementation: "
                    + ex.getMessage());
        }
        test.execute(new org.apache.axis2.saaj.MessageFactoryImpl());
    }
}
