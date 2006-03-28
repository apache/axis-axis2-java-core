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

package org.apache.axis2.security.trust;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;

/**
 * The <code>TokenIssuer</code> 
 *
 */
public interface TokenIssuer {

    /**
     * Create the response <code>soap:Envelope</code> for the given issue
     * request.
     * 
     * @param request
     *            The contents of the <code>soap:Body</code> as an
     *            <code>OMElement</code>
     * @param inMsgCtx
     *            The incoming messagge context
     * @return The response <code>soap:Envelope</code> for the given issue
     *         request.
     * @throws TrustException
     */
    public SOAPEnvelope issue(OMElement request, MessageContext inMsgCtx)
            throws TrustException;

    /**
     * Returns the <code>wsa:Action</code> of the response
     * 
     * @param request
     *            The contents of the <code>soap:Body</code> as an
     *            <code>OMElement</code>
     * @param inMsgCtx
     *            The incoming messagge context
     * @return Returns the <code>wsa:Action</code> of the response
     * @throws TrustException
     */
    public String getResponseAction(OMElement request, MessageContext inMsgCtx)
            throws TrustException;
    
    /**
     * Set the configuration file of this TokenIssuer.
     * 
     * This is the text value of the &lt;configuration-file&gt; element of the 
     * token-dispatcher-configuration
     * @param configFile
     */
    public void setConfigurationFile(String configFile);
    
    /**
     * Set the configuration element of this TokenIssuer.
     * 
     * This is the &lt;configuration&gt; element of the 
     * token-dispatcher-configuration
     * 
     * @param configElement <code>OMElement</code> representing the configuation
     */
    public void setConfigurationElement(OMElement configElement);
    
    /**
     * Set the name of the configuration parameter.
     * 
     * If this is used then there must be a 
     * <code>org.apache.axis2.description.Parameter</code> object available in 
     * the via the messageContext when the <code>TokenIssuer</code> is called.
     * 
     * @see org.apache.axis2.description.Parameter
     * @param configParamName
     */
    public void setConfigurationParamName(String configParamName); 
    
}
