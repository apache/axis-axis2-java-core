package org.apache.axis.deployment.metadata.phaserule;

import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisModule;
import org.apache.axis.description.Flow;
import org.apache.axis.deployment.metadata.ServerMetaData;
import org.apache.axis.deployment.DeploymentEngine;

import javax.xml.namespace.QName;
import java.util.Vector;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Dec 10, 2004
 *         5:27:11 PM
 */
public class PhaseResolver {
    private EngineRegistry engineRegistry ;
    AxisService axisService;

    public PhaseResolver(EngineRegistry engineRegistry, AxisService axisService ) {
        this.engineRegistry = engineRegistry;
        this.axisService = axisService;
    }

    public AxisService buildExcutionChains() throws AxisFault {
        Vector allHandlers = new Vector();
        ServerMetaData server = DeploymentEngine.getServerMetaData();
        int count = server.getModuleCount();
        QName moduleName;
        AxisModule module;
        Flow flow;
        for(int intA=0 ; intA <= count; intA ++){
            moduleName = server.getModule(intA);
            module = engineRegistry.getModule(moduleName);
            flow = module.getInFlow();
            for(int j= 0 ; j <= flow.getHandlerCount() ; j++ ){
                allHandlers.add(flow.getHandler(j));
            }
        }

        return  axisService;
    }

}
