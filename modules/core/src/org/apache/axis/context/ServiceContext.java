package org.apache.axis.context;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.PhasesInclude;
import org.apache.axis.description.PhasesIncludeImpl;
import org.apache.axis.engine.AxisFault;

public class ServiceContext  extends AbstractContext implements PhasesInclude{
    private AxisService serviceConfig;   

    public ServiceContext(AxisService serviceConfig,EngineContext engineContext) {
        super(engineContext);
        this.serviceConfig = serviceConfig;
        this.operationContextMap = new HashMap();
    }

    /**
     * @return
     */
    public AxisService getServiceConfig() {
        return serviceConfig;
    }
}
