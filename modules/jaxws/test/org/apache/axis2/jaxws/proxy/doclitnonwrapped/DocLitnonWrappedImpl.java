/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.proxy.doclitnonwrapped;

import org.apache.axis2.jaxws.TestLogger;

import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;


@WebServiceProvider()
public class DocLitnonWrappedImpl implements Provider<String> {

	
	public DocLitnonWrappedImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

    /* (non-Javadoc)
     * @see javax.xml.ws.Provider#invoke(T)
     */
    public String invoke(String invoke_str) {
        TestLogger.logger.debug("End point called with String value =" + invoke_str);
        if (invoke_str.contains("nil")) {
            return new String("<ns2:ReturnType xsi:nil='true' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:ns2=\"http://doclitnonwrapped.proxy.test.org\"/>");
        } else {
            return new String("<ns2:ReturnType xmlns:ns2=\"http://doclitnonwrapped.proxy.test.org\"><return_str>some response</return_str></ns2:ReturnType>");
        }
    }
}
