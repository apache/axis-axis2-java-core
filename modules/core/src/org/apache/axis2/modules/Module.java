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

package org.apache.axis2.modules;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;

/**
 * Every module provides an implementation of this class. Modules are in one of
 * three states: "available" and "initialized". All modules that the runtime
 * detects (from the system modules/ directory or from other means) are said to
 * be in the "available" state. If some service indicates a dependency on this
 * module then the module is initialized (once for the life of the system) and
 * the state changes to "initialized".
 * <p/>
 * <p/>Any module which is in the "initialized" state can be engaged as needed
 * by the engine to respond to a message. Currently module engagement is done
 * via deployment (using module.xml). In the future we may engage modules
 * programmatically by introducing an engage() method to this interface, thereby
 * allowing more dynamic scenarios.
 */
public interface Module {
    // initialize the module
    public void init(AxisConfiguration axisSystem) throws AxisFault;

    // TODO figure out how to get the engage() concept done
    // public void engage(ExecutionChain exeChain) throws AxisFault;

    // shutdown the module
    public void shutdown(AxisConfiguration axisSystem) throws AxisFault;
}
