/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.description;

import org.apache.axis.impl.description.ParameterIncludeImpl;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class AxisGlobal implements ParameterInclude {
    protected ParameterInclude paramInclude;
    
    public AxisGlobal(){
        paramInclude = new ParameterIncludeImpl();
    }
    
    public int hashCode() {
        return paramInclude.hashCode();
    }

    public String toString() {
        return paramInclude.toString();
    }

    /**
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return paramInclude.getParameter(name);
    }

    public boolean equals(Object obj) {
        return paramInclude.equals(obj);
    }

    /**
     * @param param
     */
    public void addParameter(Parameter param) {
        paramInclude.addParameter(param);
    }

}
