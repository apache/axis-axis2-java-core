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

package org.apache.axis.impl.engine;

import org.apache.axis.engine.Operation;
import org.apache.axis.engine.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class OperationImpl extends AbstractContainer implements Operation{
    private Log log = LogFactory.getLog(getClass());   
    private QName name;
    private Service service;
    
    /**
     * Each Operatrion must have a associated Service. The  service need to be specified 
     * at the initialization of the Operation.  
     * @param name
     * @param service
     */
    public OperationImpl(QName name,Service service){
        this.name = name;
        this.service = service;
    }
    public QName getName() {
        return name;
    }
}
