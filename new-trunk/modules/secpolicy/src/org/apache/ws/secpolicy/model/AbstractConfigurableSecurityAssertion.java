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
package org.apache.ws.secpolicy.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.neethi.Assertion;

public abstract class AbstractConfigurableSecurityAssertion extends AbstractSecurityAssertion {
    
    protected ArrayList configurations = null;
    
    public void addConfiguration(Assertion assertion) {
        if (configurations == null) {
            configurations = new ArrayList();
        }
        configurations.add(assertion);
    }
    
    public List getConfigurations() {
        return configurations;
    }
    
    public Assertion getDefaultAssertion() {
        if (configurations != null) {
            return (Assertion) configurations.get(0);
        }
        return null;
    }
}
