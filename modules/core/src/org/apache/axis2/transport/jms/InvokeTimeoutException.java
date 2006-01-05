/*
* Copyright 2001, 2002,2004 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package org.apache.axis2.transport.jms;

/**
 * The <code>InvokeTimeoutException</code> is thrown when a method cannot
 * complete processing within the time allotted.  This occurs most often
 * when the broker has failed and the client is unable to reconnect.  This
 * may be thrown from any method within the wsclient that interacts with the
 * broker.  The timeout is defined within the environment parameter to
 * <code>createConnector</code> method in <code>JMSConnectorFactory</code>
 * The key in the table is <code>JMSConstants.INTERACT_TIMEOUT_TIME</code>
 */
public class InvokeTimeoutException extends InvokeException {
	
    private static final long serialVersionUID = -7542703412906905731L;

	public InvokeTimeoutException(String message) {
        super(message);
    }
}
