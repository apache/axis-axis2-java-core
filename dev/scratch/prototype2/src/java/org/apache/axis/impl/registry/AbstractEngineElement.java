/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.impl.registry;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.registry.EngineElement;
import org.apache.axis.registry.Parameter;

import java.util.HashMap;

/**
 * This is not a abstract class so the users can wrapped it.
 *
 * @author Srinath Perera(hemapani@opensource.lk)*
 */
public class AbstractEngineElement implements EngineElement {
    private HashMap parameterMap;

    public AbstractEngineElement() {
        parameterMap = new HashMap();
    }

    public void cleanup() throws AxisFault {
    }

    public void init() throws AxisFault {
    }

    public void addParameter(Parameter param) {
        parameterMap.put(param.getName(), param);

    }

    public Parameter getParameter(String key) {
        return (Parameter) parameterMap.get(key);
    }
}
