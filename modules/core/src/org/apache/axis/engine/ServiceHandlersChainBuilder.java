
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
 *  Runtime state of the engine
 */
package org.apache.axis.engine;

import java.util.Collection;
import java.util.Iterator;

import org.apache.axis.context.EngineContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisModule;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.axis.modules.Module;

public class ServiceHandlersChainBuilder extends AbstractHandler {

    /* (non-Javadoc)
     * @see org.apache.axis.engine.Handler#invoke(org.apache.axis.context.MessageContext)
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        ServiceContext serviceContext = msgContext.getServiceContext();
        if (serviceContext != null) {
            // let add the Handlers
            ExecutionChain chain = msgContext.getExecutionChain();
            
            EngineContext engineContext = msgContext.getEngineContext();
            engineContext.addService(serviceContext);
            
            chain.addPhases(serviceContext.getPhases(EngineConfiguration.INFLOW));
            
            //TODO check had the modules changes after the deployment time handler 
            //resolution and if that is the case recalculate the Handler Chain

            //let the each module chain the execution chain if so wished
            EngineConfiguration engConfig = engineContext.getEngineConfig();
            AxisGlobal axisGlobal = engConfig.getGlobal();
            Collection modules = axisGlobal.getModules();
            AxisModule axisModule = null;
            for(Iterator it = modules.iterator();it.hasNext();axisModule = (AxisModule)it.next()){
                Module module = axisModule.getModule();
                module.engage(chain);
            }
            
            modules = serviceContext.getServiceConfig().getModules();
            for(Iterator it = modules.iterator();it.hasNext();axisModule = (AxisModule)it.next()){
                Module module = axisModule.getModule();
                module.engage(chain);
            }
            
        } else {
            throw new AxisFault("Service " + serviceContext.getName() + " is not found");
        }

    }

}
