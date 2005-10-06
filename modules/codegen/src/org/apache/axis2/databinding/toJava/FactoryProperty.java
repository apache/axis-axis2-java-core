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
    This is a general purpose class that allows one to pass name/value
    properties to the JavaGeneratorFactory.
     
    @author Jim Stafford (jim.stafford@raba.com)
*/
public class FactoryProperty {
    private String name_;
    private String value_;

    public FactoryProperty() {
    }

    public String getName() {
        return name_;
    }

    public String getValue() {
        return value_;
    }

    public void setName(String string) {
        name_ = string;
    }

    public void setValue(String string) {
        value_ = string;
    }

    public String toString() {
        return name_ + "=" + value_;
    }
    
    public boolean equals(Object rhs) {
        if (rhs == null) {
            return false;
        }
        else if (rhs instanceof String) {
            return ((String)rhs).equals(name_);
        }
        else if (rhs instanceof FactoryProperty) {
            return ((FactoryProperty)rhs).equals(name_);
        }
        else {
            return false;
        }
    }

}
