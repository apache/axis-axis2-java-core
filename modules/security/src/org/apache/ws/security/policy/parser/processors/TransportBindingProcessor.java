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

package org.apache.ws.security.policy.parser.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.policy.model.Binding;
import org.apache.ws.security.policy.parser.SecurityPolicy;
import org.apache.ws.security.policy.parser.SecurityPolicyToken;
import org.apache.ws.security.policy.parser.SecurityProcessorContext;
import org.apache.ws.security.processor.TimestampProcessor;

public class TransportBindingProcessor {
    
    private Log log = LogFactory.getLog(getClass());
    
    private boolean initializedSymmetricBinding = false;
    
    private void initializeTransportBinding(SecurityPolicyToken spt)
        throws NoSuchMethodException {
        
        SecurityPolicyToken tmpSpt = SecurityPolicy.includeTimestamp.copy();
        tmpSpt.setProcessTokenMethod(this);
        spt.setChildToken(tmpSpt);

    }
    
    public Object doTransportBinding(SecurityProcessorContext spc) {
        log.debug("Processing "
                + spc.readCurrentSecurityToken().getTokenName() + ": "
                + SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        SecurityPolicyToken spt = spc.readCurrentSecurityToken();

        switch (spc.getAction()) {

        case SecurityProcessorContext.START:
            if (!initializedSymmetricBinding) {
                try {
                    initializeTransportBinding(spt);
                    initializedSymmetricBinding = true;
                } catch (NoSuchMethodException e) {
                    log.error(e.getMessage(), e);
                    return new Boolean(false);
                }
            }
            break;
        case SecurityProcessorContext.COMMIT:
            break;
        case SecurityProcessorContext.ABORT:
            break;
        }
        return new Boolean(true);
    }
    
    public Object doIncludeTimestamp(SecurityProcessorContext spc) {
        log.debug("Processing "
                + spc.readCurrentSecurityToken().getTokenName() + ": "
                + SecurityProcessorContext.ACTION_NAMES[spc.getAction()]);
        if(spc.getAction() == SecurityProcessorContext.START) {
            ((Binding)spc.readCurrentPolicyEngineData()).setIncludeTimestamp(true);
        }
        return new Boolean(true);
    }
}
