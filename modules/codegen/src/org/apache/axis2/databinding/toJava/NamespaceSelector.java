/*
 * Copyright 2002,2004 The Apache Software Foundation.
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
package org.apache.axis2.databinding.toJava;

/**
   This class is used within the context of a FactorySpec to express
   namespaces that should be either included and/or excluded from source
   code generation. The ability to include/exclude specific namespaces from
   wsdl2java generation allows certain namespaces to be mapped to custom
   bean classes, have wsdl-generated stubs/skeletons declared to pass those
   types, and not have the wsdl2java process generate classes which would 
   conflict with the externally developed custom beans.

   @author Jim Stafford (jim.stafford@raba.com)
*/
public class NamespaceSelector {
    private String namespace_ = "";
    
    public NamespaceSelector() {}
    public NamespaceSelector(String namespace) {
        namespace_ = namespace;
    }
    
    public void setNamespace(String value) {
        namespace_ = value;
    }
    
    public String getNamespace() {
        return namespace_;
    }
    
    public String toString() {
        if (namespace_ != null) {
            return "namespace=" + namespace_; 
        }
        else {
            return "";
        }
    }

    public boolean equals(Object value) {
        boolean isEqual = false;
        if (value == null) {
            isEqual = false;
        }
        else if (value instanceof String) {
            isEqual = ((String)value).equals(namespace_);
        }
        else if (value instanceof NamespaceSelector) {
            isEqual = ((NamespaceSelector)value).namespace_.equals(namespace_);
        }
        return isEqual;
    }
}
