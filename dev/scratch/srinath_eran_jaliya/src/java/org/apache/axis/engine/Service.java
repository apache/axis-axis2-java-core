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

package org.apache.axis.engine;

import javax.xml.namespace.QName;

import org.apache.axis.CommonExecutor;
import org.apache.axis.registry.FlowInclude;
import org.apache.axis.registry.ModuleInclude;
import org.apache.axis.registry.NamedEngineElement;
import org.apache.axis.registry.TypeMappingInclude;

/**
 * Runtime representation of the WSDL Service
 */

public interface Service extends FlowInclude,NamedEngineElement,
    TypeMappingInclude,CommonExecutor,ModuleInclude{
    public Operation getOperation(QName index);
    public void addOperation(Operation op);
    
    public ClassLoader getClassLoader();
}
