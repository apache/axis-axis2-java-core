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
package org.apache.axis2.jaxws.sample.addnumbershandler;

import java.util.concurrent.Future;
import javax.jws.WebService;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.xml.ws.AsyncHandler;
import org.test.addnumbershandler.AddNumbersHandlerResponse;
import org.apache.axis2.jaxws.TestLogger;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;


@WebService(endpointInterface="org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerPortType")
@HandlerChain(file = "AddNumbersHandlers.xml", name = "")
public class AddNumbersHandlerPortTypeImpl implements AddNumbersHandlerPortType {

    private WebServiceContext ctx;
    
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.sample.addnumbershandler.AddNumbersHandlerPortType#addNumbersHandler(int, int)
	 */
	public int addNumbersHandler(int arg0, int arg1) throws AddNumbersHandlerFault_Exception {
        
	    HandlerTracker tracker = HandlerTracker.getHandlerTracker(AddNumbersProtocolHandler.class);
	    if (!tracker.isCalled(HandlerTracker.Methods.HANDLE_MESSAGE)) {
	        throw new RuntimeException("handleMessage() was not called on the handler");
	    }
	    /* FIXME: getHeaders() is currently not called
	    if (!tracker.isCalled(HandlerTracker.Methods.GET_HEADERS)) {
	        throw new RuntimeException("getHeaders() was not called on the handler");
	    }
	    */
	    
        // for these properties tests to always pass, an inbound server-side handler must "put" them
        MessageContext mc = ctx.getMessageContext();
        String propKey1 = "AddNumbersLogicalHandlerInboundAppScopedProperty";
        String propKey2 = "AddNumbersLogicalHandlerInboundHandlerScopedProperty";
        String value = (String)mc.get(propKey1);
        if (value == null)
            throw new RuntimeException("Property value for key \"" + propKey1 + "\" was null, but is APPLICATION scoped and should be accessible by the endpoint");
        if (mc.containsKey(propKey2))  // instead of "get", use "containsKey" to be a little more robust in testing
            throw new RuntimeException("MessageContext.containsKey reported true for key \"" + propKey2 + "\" was not null.  This property is HANDLER scoped and should not be accessible by the endpoint");
        TestLogger.logger
                .debug(">> Received addNumbersHandler request for " + arg0 + " and " + arg1);
        if (arg0 == 101)
            throw new RuntimeException("Got value 101.  AddNumbersHandlerPortTypeImpl.addNumbersHandler method is correctly throwing this exception as part of testing");
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
    
    @Resource
    public void setCtx(WebServiceContext ctx) {
        this.ctx = ctx;
    }

}
