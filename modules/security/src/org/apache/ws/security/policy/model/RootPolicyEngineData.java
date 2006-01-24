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

package org.apache.ws.security.policy.model;

import java.util.ArrayList;

public class RootPolicyEngineData extends PolicyEngineData {

    private ArrayList topLevelPEDs = new ArrayList();
    
    public void addTopLevelPED(PolicyEngineData ped) {
        if(!topLevelPEDs.contains(ped)) {
            this.topLevelPEDs.add(ped);
        }
    }
    
    public ArrayList getTopLevelPEDs() {
        return this.topLevelPEDs;
    }
}
