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

package org.apache.rahas;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.OMElement;

public interface TokenCanceler {

    /**
     * Cancel the token specified in the request.
     *
     * @param data A populated <code>RahasData</code> instance
     * @return Response SOAPEnveloper
     * @throws TrustException
     */
    SOAPEnvelope cancel(RahasData data) throws TrustException;

    /**
     * Set the configuration file of this TokenCanceller.
     * <p/>
     * This is the text value of the &lt;configuration-file&gt; element of the
     * token-dispatcher-configuration
     *
     * @param configFile
     */
    void setConfigurationFile(String configFile);

    /**
     * Set the configuration element of this TokenCanceller.
     * <p/>
     * This is the &lt;configuration&gt; element of the
     * token-dispatcher-configuration
     *
     * @param configElement <code>OMElement</code> representing the configuation
     */
    void setConfigurationElement(OMElement configElement);

    /**
     * Set the name of the configuration parameter.
     * <p/>
     * If this is used then there must be a
     * <code>org.apache.axis2.description.Parameter</code> object available in
     * the via the messageContext when the <code>TokenIssuer</code> is called.
     *
     * @param configParamName
     * @see org.apache.axis2.description.Parameter
     */
    void setConfigurationParamName(String configParamName);

    /**
     * Returns the <code>wsa:Action</code> of the response.
     *
     * @param data A populated <code>RahasData</code> instance
     * @return Returns the <code>wsa:Action</code> of the response
     * @throws TrustException
     */
    String getResponseAction(RahasData data) throws TrustException;
}
