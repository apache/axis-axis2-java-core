/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.wsdl.wom;

import java.net.URI;
import java.util.HashMap;


/**
 * @author chathura@opensource.lk
 *
 */
public interface WSDLService {
    public HashMap getEndpoints();

    public void setEndpoints(HashMap endpoints);

    /**
     * Will add a WSDLEndpoint object to the WOM keyed with NCName;
     */
    public void setEndpoint(WSDLEndpoint endpoint, String nCName);

    /**
     * Endpoint will be retrived by its NCName.
     * @param nCName NCName of the Service
     * @return WSDLService Object or will throw an WSDLProcessingException in the case of object not found. 
     */
    public WSDLService getService(String nCName);

    public String getName();

    public void setName(String name);

    public URI getNamespaceURI();

    public void setNamespaceURI(URI namespaceURI);

    public WSDLInterface getServiceInterface();

    public void setServiceInterface(WSDLInterface serviceInterface);
}