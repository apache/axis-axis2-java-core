/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.marshaller.impl.alt;

import org.apache.axis2.jaxws.description.ParameterDescription;


/**
 * A PDElement object holds a ParameterDescription (Param) and 
 * the "Element" value.
 * Characteristics of the "Element" value.
 *    * The Element value is ready for marshalling or is the result of unmarshalling.
 *    * The Element value represents the element rendering.  Thus it is either
 *      a JAXBElement or has the @XmlRootElement annotation.  (i.e. it is never a 
 *      java.lang.String)
 *    * The Element value is not a JAX-WS object. (i.e. it is not a holder or exception)
 */
public class PDElement {
    private ParameterDescription param;
    private Object value;
    
    public PDElement(ParameterDescription param, Object value) {
        super();
        this.param = param;
        this.value = value;
    }

    public ParameterDescription getParam() {
        return param;
    }

    public Object getElementValue() {
        return value;
    }
    
}
