package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.phaseresolver.PhaseMetadata;

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
 * Author: Deepal Jayasinghe
 * Date: Sep 13, 2005
 * Time: 4:34:27 PM
 */
public class DispatchingTest extends TestCase {

    AxisConfiguration ar;
    String repo ="./test-resources/deployment/dispatch_repo";



    public void testDispatch() throws Exception {
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        ar = builder.buildConfigurationContext(repo).getAxisConfiguration();
        ArrayList list = ar.getInPhasesUptoAndIncludingPostDispatch();
        for (int i = 0; i < list.size(); i++) {
            Phase phase = (Phase) list.get(i);
            if(PhaseMetadata.PHASE_DISPATCH.equals(phase.getPhaseName())){
                assertEquals(3,phase.getHandlerCount());
                ArrayList handler = phase.getHandlers();
                for (int j = 0; j < handler.size(); j++) {
                    Handler handler1 = (Handler) handler.get(j);
                    switch(j){
                        case 0: {
                           assertEquals(handler1.getHandlerDesc().getName().
                                   getLocalPart(),"AddressingBasedDispatcher");
                            break;
                        }
                        case  1 : {
                           assertEquals(handler1.getHandlerDesc().getName().
                                   getLocalPart(),"SOAPActionBasedDispatcher");
                            break;
                        }
                        case 2 : {
                            assertEquals(handler1.getHandlerDesc().getName().
                                   getLocalPart(),"SOAPMessageBodyBasedDispatcher");
                            break;
                        }
                    }
                }
            }
        }
    }
}
