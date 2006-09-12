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
package org.apache.rahas.impl;

import org.apache.rahas.TokenCanceler;
import org.apache.rahas.RahasData;
import org.apache.rahas.TrustException;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.description.Parameter;

/**
 * 
 */
public class TokenCancelerImpl implements TokenCanceler {

    private String configFile;
    private OMElement configElement;
    private String configParamName;

    /**
     * Cancel the token specified in the request.
     *
     * @param data A populated <code>RahasData</code> instance
     * @return
     * @throws org.apache.rahas.TrustException
     *
     */
    public SOAPEnvelope cancel(RahasData data) throws TrustException {
        TokenCancelerConfig config = null;
        if (this.configElement != null) {
            config = TokenCancelerConfig.load(configElement.
                    getFirstChildWithName(SCTIssuerConfig.SCT_ISSUER_CONFIG));
        }

        // Look for the file
        if (config == null && this.configFile != null) {
            config = TokenCancelerConfig.load(this.configFile);
        }

        // Look for the param
        if (config == null && this.configParamName != null) {
            Parameter param = data.getInMessageContext().getParameter(this.configParamName);
            if (param != null && param.getParameterElement() != null) {
                config = TokenCancelerConfig.load(param.getParameterElement()
                        .getFirstChildWithName(SCTIssuerConfig.SCT_ISSUER_CONFIG));
            } else {
                throw new TrustException("expectedParameterMissing",
                                         new String[]{this.configParamName});
            }
        }

        if (config == null) {
            throw new TrustException("missingConfiguration",
                                     new String[]{SCTIssuerConfig.SCT_ISSUER_CONFIG
                                             .getLocalPart()});
        }
        
        //TODO: Method implementation
        return null;
    }

    /**
     * Set the configuration file of this TokenCanceller.
     * <p/>
     * This is the text value of the &lt;configuration-file&gt; element of the
     * token-dispatcher-configuration
     *
     * @param configFile
     */
    public void setConfigurationFile(String configFile) {
        this.configFile = configFile;
    }

    /**
     * Set the configuration element of this TokenCanceller.
     * <p/>
     * This is the &lt;configuration&gt; element of the
     * token-dispatcher-configuration
     *
     * @param configElement <code>OMElement</code> representing the configuation
     */
    public void setConfigurationElement(OMElement configElement) {
        this.configElement = configElement;
    }

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
    public void setConfigurationParamName(String configParamName) {
        this.configParamName = configParamName;
    }
}
