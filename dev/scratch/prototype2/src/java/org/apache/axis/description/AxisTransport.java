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

import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.impl.description.ParameterIncludeImpl;
import org.apache.axis.impl.description.PhasesIncludeImpl;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class AxisTransport implements ParameterInclude,PhasesInclude {
    protected ParameterInclude paramInclude;
    protected PhasesInclude phasesInclude;
    protected QName name; 
    
    public AxisTransport(){
        paramInclude = new ParameterIncludeImpl();
        phasesInclude = new PhasesIncludeImpl();
    }
    
    public Parameter getParameter(String name) {
        return paramInclude.getParameter(name);
    }

    public void addParameter(Parameter param) {
        paramInclude.addParameter(param);
    }

    /**
     * @param flow
     * @return
     * @throws AxisFault
     */
    public ArrayList getPhases(int flow) throws AxisFault {
        return phasesInclude.getPhases(flow);
    }

    /**
     * @param phases
     * @param flow
     * @throws AxisFault
     */
    public void setPhases(ArrayList phases, int flow) throws AxisFault {
        phasesInclude.setPhases(phases, flow);
    }

    /**
     * @return
     */
    public QName getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(QName name) {
        this.name = name;
    }

}
