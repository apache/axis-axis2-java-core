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
package org.apache.axis.description;

import java.util.ArrayList;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.AxisConfiguration;

/**
 * Util class to implements the Phases include with delegation
 */
public class PhasesIncludeImpl implements PhasesInclude {
    /**
     * Field inflow
     */
    private ArrayList inflow;

    /**
     * Field outflow
     */
    private ArrayList outflow;

    /**
     * Field faultInflow
     */
    private ArrayList faultInflow;

    private ArrayList faultOutflow;

    /**
     * Method getPhases
     *
     * @param flow
     * @return
     * @throws AxisFault
     */
    public ArrayList getPhases(int flow) throws AxisFault {
        if (flow == AxisConfiguration.INFLOW) {
            return inflow;
        } else if (flow == AxisConfiguration.OUTFLOW) {
            return outflow;
        } else if (flow == AxisConfiguration.FAULT_IN_FLOW) {
            return faultInflow;
        } else if (flow == AxisConfiguration.FAULT_OUT_FLOW) {
            return faultOutflow;
        } else {
            throw new AxisFault("Unknown type flow ");
        }
    }

    /**
         * Method setPhases
         *
         * @param phases
         * @param flow
         * @throws AxisFault
         */
    public void setPhases(ArrayList phases, int flow) throws AxisFault {
        if (flow == AxisConfiguration.INFLOW) {
            inflow = phases;
        } else if (flow == AxisConfiguration.OUTFLOW) {
            outflow = phases;
        } else if (flow == AxisConfiguration.FAULT_IN_FLOW) {
            faultInflow = phases;
        }else if (flow == AxisConfiguration.FAULT_OUT_FLOW) {
            faultOutflow = phases;
        } else {
            throw new AxisFault("Unknown type flow ");
        }
    }
}
