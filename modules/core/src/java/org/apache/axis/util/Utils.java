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
package org.apache.axis.util;

import java.util.ArrayList;

import org.apache.axis.Constants;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.Flow;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.Phase;

public class Utils {

    public static void addHandler(Flow flow, Handler handler) {
        HandlerMetadata hmd = new HandlerMetadata();
        hmd.setHandler(handler);
        flow.addHandler(hmd);
    }
    public static void addPhasesToServiceFromFlow(AxisService service, String phaseName, Flow flow, int flowtype) throws AxisFault {
         ArrayList faultchain = new ArrayList();
         Phase p = new Phase(Constants.PHASE_SERVICE);
         faultchain.add(p);
         addHandlers(flow, p);
         service.setPhases(faultchain, flowtype);
     }
    public static void createExecutionChains(AxisService service) throws AxisFault {
        addPhasesToServiceFromFlow(
            service,
            Constants.PHASE_SERVICE,
            service.getInFlow(),
            EngineRegistry.INFLOW);
        addPhasesToServiceFromFlow(
            service,
            Constants.PHASE_SERVICE,
            service.getOutFlow(),
            EngineRegistry.OUTFLOW);
        addPhasesToServiceFromFlow(
            service,
            Constants.PHASE_SERVICE,
            service.getFaultFlow(),
            EngineRegistry.FAULTFLOW);
    }
    public static void addHandlers(Flow flow, Phase phase) throws AxisFault {
            if (flow != null) {
                int handlerCount = flow.getHandlerCount();
                for (int i = 0; i < handlerCount; i++) {
                    phase.addHandler(flow.getHandler(i).getHandler());
                }
            }
        }

}
