package org.apache.axis.phaseresolver.util;

import org.apache.axis.engine.Phase;
import org.apache.axis.engine.AddressingBasedDispatcher;
import org.apache.axis.engine.RequestURIBasedDispatcher;
import org.apache.axis.phaseresolver.PhaseMetadata;

import java.util.ArrayList;

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
 *
 * 
 */

/**
 * Author : Deepal Jayasinghe
 * Date: May 12, 2005
 * Time: 6:45:50 PM
 */
public class FlowBuilder {

    /**
     * This method is used to build the default sytem predefined phases, and this will be latter cahange
     * by the deployment module
     * @return
     */
    public static ArrayList getSystemINPhases(){
        ArrayList phases = new ArrayList();
        phases.add(new Phase(PhaseMetadata.PHASE_TRANSPORTIN));
        phases.add(new Phase(PhaseMetadata.PHASE_PRE_DISPATCH));
        Phase dispatch = new Phase(PhaseMetadata.PHASE_DISPATCH);
        dispatch.addHandler(new RequestURIBasedDispatcher(),0);
        dispatch.addHandler(new AddressingBasedDispatcher(),1);
        phases.add(dispatch) ;
        phases.add(new Phase(PhaseMetadata.PHASE_POST_DISPATCH));
        phases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        return phases;
    }

    public static ArrayList getOutPhases(){
        ArrayList outPhases = new ArrayList();
        outPhases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        outPhases.add(new Phase(PhaseMetadata.PHASE_MESSAGE_OUT));
        return outPhases;
    }

}
