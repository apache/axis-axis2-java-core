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

package org.apache.axis.deployment;

import org.apache.axis.context.ContextBuilder;
import org.apache.axis.context.EngineContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistryFactory;


public class EngineRegistryFactoryImpl implements EngineRegistryFactory {
    public EngineContext createEngineRegistry(String file) throws AxisFault {
        try {
            ContextBuilder builder = new ContextBuilder();
            return builder.buildEngineContext(file);
        } catch (DeploymentException e) {
            throw AxisFault.makeFault(e);
        }
    }
}
