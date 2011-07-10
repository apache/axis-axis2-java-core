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

package server;

import org.apache.axis2.jaxws.TestLogger;

public class EchoServiceImpl extends EchoServiceSkeleton {

    public  server.EchoStringResponse echoString(server.EchoString input) {
        TestLogger.logger
                .debug(">> Entering method [EchoStringResponse EchoServiceSkeleton.echoString(EchoString)]");
        TestLogger.logger.debug(">> Endpoint received input [" + input.getInput() + "]");
        TestLogger.logger.debug(">> Returning string [ECHO:" + input.getInput() + "]");
        TestLogger.logger
                .debug("<< Done with method [EchoStringResponse EchoServiceSkeleton.echoString(EchoString)]");
        if (input.getInput().equals("THROW EXCEPTION")) {
            throw new RuntimeException("test exception");
        } else {
            EchoStringResponse output = new EchoStringResponse();
            output.setEchoStringReturn("ECHO:" + input.getInput());
            return output;
        }
    }
}
    