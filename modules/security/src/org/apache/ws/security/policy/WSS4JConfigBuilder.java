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
package org.apache.ws.security.policy;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.ws.security.policy.model.AsymmetricBinding;
import org.apache.ws.security.policy.model.Binding;
import org.apache.ws.security.policy.model.PolicyEngineData;
import org.apache.ws.security.policy.model.SymmetricBinding;
import org.apache.ws.security.policy.model.Wss10;
import org.apache.ws.security.policy.model.Wss11;

public class WSS4JConfigBuilder {
    
    public static void build(ArrayList topLevelPeds) throws WSSPolicyException {
        Iterator topLevelPEDIterator = topLevelPeds.iterator();
        WSS4JConfig config = new WSS4JConfig();
        while (topLevelPEDIterator.hasNext()) {
            PolicyEngineData ped = (PolicyEngineData) topLevelPEDIterator.next();
            if(ped instanceof Binding) {
                if(ped instanceof SymmetricBinding) {
                    processSymmetricPolicyBinding((SymmetricBinding)ped, config);
                } else {
                    processAsymmetricPolicyBinding((AsymmetricBinding)ped, config);
                }
            } else if(ped instanceof Wss10) {
                processWSS10((Wss10)ped, config);
            } else if(ped instanceof Wss11) {
                processWSS11((Wss11)ped, config);
            }
        }
    }
    

    private static void processSymmetricPolicyBinding(SymmetricBinding symmbinding, WSS4JConfig config) {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }
    
    private static void processWSS10(Wss10 wss10, WSS4JConfig config) {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }
    private static void processAsymmetricPolicyBinding(AsymmetricBinding binding, WSS4JConfig config) {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }
    
    private static void processWSS11(Wss11 wss11, WSS4JConfig config) {
       if(wss11.isRequireSignatureConfirmation()) {
           config.getInflowConfiguration().setEnableSignatureConfirmation(true);
           config.getOutflowConfiguration().setEnableSignatureConfirmation(true);
       }
    }
    
}
