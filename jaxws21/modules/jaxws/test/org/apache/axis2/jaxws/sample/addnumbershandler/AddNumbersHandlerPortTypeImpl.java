/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.sample.addnumbershandler;

import java.util.concurrent.Future;
import javax.jws.WebService;
import javax.jws.HandlerChain;
import javax.xml.ws.AsyncHandler;
import org.test.addnumbershandler.AddNumbersHandlerResponse;
import org.apache.axis2.jaxws.TestLogger;


@WebService(endpointInterface="org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerPortType")
@HandlerChain(file="META-INF/AddNumbersHandlers.xml", name="")
public class AddNumbersHandlerPortTypeImpl implements AddNumbersHandlerPortType {

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerPortType#addNumbersHandler(int, int)
	 */
	public int addNumbersHandler(int arg0, int arg1) throws AddNumbersHandlerFault_Exception {
        TestLogger.logger
                .debug(">> Received addNumbersHandler request for " + arg0 + " and " + arg1);
        if (arg0 == 101)
            throw new RuntimeException("blarg");
        return arg0+arg1;
	}

	public Future<?> addNumbersHandlerAsync(int arg0, int arg1, AsyncHandler<AddNumbersHandlerResponse> asyncHandler) {
        return null;
    }



	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerPortType#oneWayInt(int)
	 */
	public void oneWayInt(int arg0) {
        TestLogger.logger.debug(">> Received one-way request.");
        return;
	}

}
