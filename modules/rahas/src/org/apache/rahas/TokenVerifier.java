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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;

public interface TokenVerifier {
    
    public SOAPEnvelope veify(OMElement request, MessageContext msgCtx) throws TrustException;
    
    /**
     * Set the configuration file of this TokenVerifier.
     * 
     * This is the text value of the &lt;configuration-file&gt; element of the 
     * token-dispatcher-configuration
     * @param configFile
     */
    public void setConfigurationFile(String configFile);
    
    /**
     * Set the configuration element of this TokenVerifier.
     * 
     * This is the &lt;configuration&gt; element of the 
     * token-dispatcher-configuration
     * 
     * @param configElement <code>OMElement</code> representing the configuation
     */
    public void setConfigurationElement(String configElement);
}
