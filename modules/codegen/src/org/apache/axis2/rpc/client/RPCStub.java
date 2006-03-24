/*
* Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.rpc.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.client.Stub;

public class RPCStub extends Stub {
    /**
     * TODO: Make this method non-static and fix the xsl's that generate code to "extend" RPCStub when appropriate.
     *
     * @param factory
     * @param env
     * @param methodNamespaceURI
     * @param methodName
     * @param paramNames
     * @param values
     */
    public static void setValueRPC(OMFactory factory,
                                   SOAPEnvelope env,
                                   String methodNamespaceURI,
                                   String methodName,
                                   String[] paramNames,
                                   Object[] values) {
        SOAPBody body = env.getBody();

        OMNamespace methodNamespace = factory.createOMNamespace(methodNamespaceURI,
                "ns1");
        OMElement elt = factory.createOMElement(methodName, methodNamespace);
        if (paramNames != null) {
            //find the relevant object here, convert it and add it to the elt
            for (int i = 0; i < paramNames.length; i++) {
                String paramName = paramNames[i];
                Object value = values[i];
                elt.addChild(StubSupporter.createRPCMappedElement(paramName,
                        factory.createOMNamespace("", null), //empty namespace
                        value,
                        factory));
            }
        }
        body.addChild(elt);
    }
}
