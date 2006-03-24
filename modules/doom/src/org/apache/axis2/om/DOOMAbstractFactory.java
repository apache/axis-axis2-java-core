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

package org.apache.axis2.om;

import org.apache.axiom.om.OMFactory;
import org.apache.axis2.om.impl.dom.factory.OMDOMFactory;
import org.apache.axis2.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axis2.soap.impl.dom.soap12.SOAP12Factory;
import org.apache.ws.commons.soap.SOAPFactory;

public class DOOMAbstractFactory {

    public static OMFactory getOMFactory() {
        return new OMDOMFactory();
    }

    public static SOAPFactory getSOAP11Factory() {
        return new SOAP11Factory();
    }

    public static SOAPFactory getSOAP12Factory() {
        return new SOAP12Factory();
    }
}
