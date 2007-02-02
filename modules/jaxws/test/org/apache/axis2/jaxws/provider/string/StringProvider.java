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
package org.apache.axis2.jaxws.provider.string;

import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

@WebServiceProvider()
public class StringProvider implements Provider<String> {
    
    public String invoke(String text) {
        System.out.println("StringProvider invoke received the message [" + text + "]");
        if (text != null && text.contains("throwWebServiceException")) {
            throw new WebServiceException("provider");
        } else {
            // Echo the input
            return text;
        }
    }
}
