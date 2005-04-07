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
package org.apache.wsdl;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * @author chathura@opensource.lk
 */
public interface WSDLService extends Component {
    public static final String STYLE_RPC = "rpc";
    public static final String STYLE_DOC = "doc";
    public static final String STYLE_MSG = "msg";
    /**
     * Method getEndpoints
     *
     * @return
     */
    public HashMap getEndpoints();

    /**
     * Method setEndpoints
     *
     * @param endpoints
     */
    public void setEndpoints(HashMap endpoints);

    /**
     * Will add a WSDLEndpoint object to the WOM keyed with qname;
     *
     * @param endpoint
     */
    public void setEndpoint(WSDLEndpoint endpoint);

    /**
     * Endpoint will be retrived by its qName.
     *
     * @param qName qName of the Service
     * @return WSDLService Object or will throw an WSDLProcessingException in the case of object not found.
     */
    public WSDLEndpoint getEndpoint(QName qName);

    /**
     * Method getName
     *
     * @return
     */
    public QName getName();

    /**
     * Method setName
     *
     * @param name
     */
    public void setName(QName name);

    /**
     * If the Name of the <code>WSDLService</code> is not set a
     * <code>WSDLProcessingException</code> will be thrown.
     *
     * @return Target Namespace as a <code>String</code>
     */
    public String getNamespace();

    /**
     * Method getServiceInterface
     *
     * @return
     */
    public WSDLInterface getServiceInterface();

    /**
     * Method setServiceInterface
     *
     * @param serviceInterface
     */
    public void setServiceInterface(WSDLInterface serviceInterface);
}
