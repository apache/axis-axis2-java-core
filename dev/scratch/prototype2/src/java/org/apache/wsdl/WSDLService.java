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
package org.apache.wsdl;

import javax.xml.namespace.QName;
import java.util.HashMap;


/**
 * @author chathura@opensource.lk
 *
 */
public interface WSDLService extends Component{
    public HashMap getEndpoints();

    public void setEndpoints(HashMap endpoints);

    /**
     * Will add a WSDLEndpoint object to the WOM keyed with qname;
     */
    public void setEndpoint(WSDLEndpoint endpoint);

    /**
     * Endpoint will be retrived by its qName.
     * @param qName qName of the Service
     * @return WSDLService Object or will throw an WSDLProcessingException in the case of object not found. 
     */
    public WSDLService getEndpoint(QName qName);

    public QName getName();

    public void setName(QName name);

    /**
     * If the Name of the <code>WSDLService</code> is not set a 
     * <code>WSDLProcessingException</code> will be thrown.
     * @return Target Namespace as a <code>String</code>
     */
    public String getNamespace();

    public WSDLInterface getServiceInterface();

    public void setServiceInterface(WSDLInterface serviceInterface);
}