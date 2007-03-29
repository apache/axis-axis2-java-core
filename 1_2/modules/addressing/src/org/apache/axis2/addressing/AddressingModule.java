/*
 * Copyright 2007 The Apache Software Foundation.
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

package org.apache.axis2.addressing;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.modules.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

import java.util.List;

public class AddressingModule implements Module {

    private static final Log log = LogFactory.getLog(AddressingModule.class);

    public void init(ConfigurationContext arg0, AxisModule arg1)
            throws AxisFault {
        AxisConfiguration axisConfig = arg0.getAxisConfiguration();
        ModuleConfiguration moduleConfig = axisConfig.getModuleConfig(Constants.MODULE_ADDRESSING);

        if (moduleConfig != null) {
            List list = moduleConfig.getParameters();
            for (int i = 0, size = list.size(); i < size; i++) {
                Parameter param = (Parameter) list.get(i);
                arg1.addParameter(param);

                if (log.isTraceEnabled()) {
                    log.trace("init: Addressing config -" + param);
                }
            }

            initHandlers(arg1);
        }
    }

    public void engageNotify(AxisDescription arg0) throws AxisFault {
        // TODO Auto-generated method stub

    }

    public boolean canSupportAssertion(Assertion arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void applyPolicy(Policy arg0, AxisDescription arg1) throws AxisFault {
        // TODO Auto-generated method stub

    }

    public void shutdown(ConfigurationContext arg0) throws AxisFault {
        // TODO Auto-generated method stub

    }

    //This method calls the init method of the handlers that we want to initialize.
    //Currently only the handlers in the OutFlow and FaultOutFlow are initialized
    //by this code. If handlers in other flows need to be initialized then code will
    //need to be added to do so.
    private void initHandlers(AxisModule axisModule) {
        Flow flow = axisModule.getOutFlow();
        if (log.isTraceEnabled()) {
            log.trace("initHandlers: Initializing handlers in out flow.");
        }
        for (int i = 0, size = flow.getHandlerCount(); i < size; i++) {
            HandlerDescription description = flow.getHandler(i);
            Handler handler = description.getHandler();
            handler.init(description);
        }

        flow = axisModule.getFaultOutFlow();
        if (log.isTraceEnabled()) {
            log.trace("initHandlers: Initializing handlers in fault out flow.");
        }
        for (int i = 0, size = flow.getHandlerCount(); i < size; i++) {
            HandlerDescription description = flow.getHandler(i);
            Handler handler = description.getHandler();
            handler.init(description);
        }
    }
}
