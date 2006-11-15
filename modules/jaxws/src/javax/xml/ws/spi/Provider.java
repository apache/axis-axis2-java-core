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

package javax.xml.ws.spi;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import java.net.URL;

public abstract class Provider {

    protected Provider() {
    }

    public static Provider provider() {
        return (Provider) FactoryFinder.find("javax.xml.ws.spi.Provider", DEFAULT_JAXWSPROVIDER);
    }

    public abstract ServiceDelegate createServiceDelegate(URL url, QName qname, Class class1);

    public abstract Endpoint createEndpoint(String s, Object obj);

    public abstract Endpoint createAndPublishEndpoint(String s, Object obj);

    public static final String JAXWSPROVIDER_PROPERTY = "javax.xml.ws.spi.Provider";
    private static final String DEFAULT_JAXWSPROVIDER = "org.apache.axis2.jaxws.spi.Provider";
}
