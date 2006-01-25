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

import org.apache.axis2.security.handler.config.InflowConfiguration;
import org.apache.axis2.security.handler.config.OutflowConfiguration;
import org.apache.ws.security.policy.model.Binding;

public class WSS4JConfig {

    private OutflowConfiguration outflowConfiguration = new OutflowConfiguration();
    private InflowConfiguration inflowConfiguration = new InflowConfiguration();
    
    Binding binding;
    boolean signature;
    boolean encryption;
    
    /**
     * @return Returns the inflowConfiguration.
     */
    public InflowConfiguration getInflowConfiguration() {
        return inflowConfiguration;
    }
//    /**
//     * @param inflowConfiguration The inflowConfiguration to set.
//     */
//    public void setInflowConfiguration(InflowConfiguration inflowConfiguration) {
//        this.inflowConfiguration = inflowConfiguration;
//    }
    /**
     * @return Returns the outflowConfiguration.
     */
    public OutflowConfiguration getOutflowConfiguration() {
        return outflowConfiguration;
    }
//    /**
//     * @param outflowConfiguration The outflowConfiguration to set.
//     */
//    public void setOutflowConfiguration(OutflowConfiguration outflowConfiguration) {
//        this.outflowConfiguration = outflowConfiguration;
//    }
    
}
