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
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.PolicyComponent;
import org.apache.ws.security.policy.Constants;

public class UsernameToken extends Token {
    
    private List usernameTokens;
    
    private boolean useUTProfile11;

    /**
     * @return Returns the useUTProfile11.
     */
    public boolean isUseUTProfile11() {
        return useUTProfile11;
    }

    /**
     * @param useUTProfile11 The useUTProfile11 to set.
     */
    public void setUseUTProfile11(boolean useUTProfile11) {
        this.useUTProfile11 = useUTProfile11;
    }
    
    public List getOptions() {
        return usernameTokens;
    }
    
    public void addOption(UsernameToken usernameToken) {
        if (usernameTokens == null) {
            usernameTokens= new ArrayList();
        }
        usernameTokens.add(usernameToken);
    }

    public QName getName() {
        return Constants.USERNAME_TOKEN;
    }

    public PolicyComponent normalize() {
        throw new UnsupportedOperationException();
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
        throw new UnsupportedOperationException();
    }       
}
