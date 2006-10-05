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

import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.PolicyComponent;

public abstract class AbstractSecurityAssertion implements Assertion {

    private boolean isOptional;
    
    private boolean normalized = false;

    public boolean isOptional() {
        return isOptional;
    }
    
    public void setOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }

    public short getType() {
        return Constants.TYPE_ASSERTION;
    }    
    
    public boolean equal(PolicyComponent policyComponent) {
        throw new UnsupportedOperationException();
    }
    
    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }
    
    public boolean isNormalized() {
        return true;
    }

    public PolicyComponent normalize() {
        
        /*
         * TODO: Handling the isOptional:TRUE case
         */
        return this;
    }    
}
