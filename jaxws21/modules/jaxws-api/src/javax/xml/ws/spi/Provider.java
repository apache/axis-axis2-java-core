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
package javax.xml.ws.spi;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.w3c.dom.Element;

import java.net.URL;
import java.util.List;

public abstract class Provider {

    protected Provider() {
    }

    public static Provider provider() {
        return (Provider) FactoryFinder.find(JAXWSPROVIDER_PROPERTY, DEFAULT_JAXWSPROVIDER);
    }

    public abstract ServiceDelegate createServiceDelegate(URL url, QName qname, Class class1);

    public abstract Endpoint createEndpoint(String s, Object obj);

    public abstract Endpoint createAndPublishEndpoint(String s, Object obj);

    //TODO
    public abstract EndpointReference readEndpointReference(Source eprInfoset);
    
    //TODO
    public abstract <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features);
    
    //TODO
    public abstract W3CEndpointReference createW3CEndpointReference(String address,
            QName serviceName,
            QName portName,
            List<Element> metadata,
            String wsdlDocumentLocation,
            List<Element> referenceParameters);
    
    public static final String JAXWSPROVIDER_PROPERTY = "javax.xml.ws.spi.Provider";
    private static final String DEFAULT_JAXWSPROVIDER = "org.apache.axis2.jaxws.spi.Provider";
}
