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

import java.util.ArrayList;


public interface PhasesInclude {
    /**
     * When the Phases are resolved they are added to the EngineRegistry as phases at deploy time.
     * At the runtime they are used to create the ExecutionChain at the runtime whic resides in the
     * MessageContext.
     *
     * @param flow
     * @return
     */

    public ArrayList getPhases(int flow) throws AxisFault;

    public void setPhases(ArrayList phases, int flow) throws AxisFault;
}
