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

import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;

import java.util.ArrayList;

/**
 * Util class to implements the Phases include with delegation
 */
public class PhasesIncludeImpl implements PhasesInclude {
    private ArrayList inflow;
    private ArrayList outflow;
    private ArrayList faultflow;

    public ArrayList getPhases(int flow) throws AxisFault {
        if (flow == EngineRegistry.INFLOW) {
            return inflow;
        } else if (flow == EngineRegistry.OUTFLOW) {
            return outflow;
        } else if (flow == EngineRegistry.FAULTFLOW) {
            return faultflow;
        } else {
            throw new AxisFault("Unknown type flow ");
        }

    }

    public void setPhases(ArrayList phases, int flow) throws AxisFault {
        if (flow == EngineRegistry.INFLOW) {
            inflow = phases;
        } else if (flow == EngineRegistry.OUTFLOW) {
            outflow = phases;
        } else if (flow == EngineRegistry.FAULTFLOW) {
            faultflow = phases;
        } else {
            throw new AxisFault("Unknown type flow ");
        }
    }
}
