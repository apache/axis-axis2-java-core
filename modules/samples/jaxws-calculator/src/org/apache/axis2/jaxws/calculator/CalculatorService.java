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

package org.apache.axis2.jaxws.calculator;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

@WebService(serviceName = "Calculator",
            endpointInterface = "org.apache.axis2.jaxws.calculator.Calculator",
            targetNamespace = "http://calculator.jaxws.axis2.apache.org")
public class CalculatorService implements Calculator {

    @Resource
    private WebServiceContext context;

    /**
     * @return
     *     returns javax.xml.ws.WebServiceContext
     */
    public WebServiceContext getContext() {
        return context;
    }

    /**
     * @param value1
     * @param value2
     * @return
     *     returns int
     */
    public int add(int value1, int value2) {
        System.out.println("User Principal: " + context.getUserPrincipal());
        System.out.println("value1: " + value1 + " value2: " + value2);
        return value1 + value2;
    }
}