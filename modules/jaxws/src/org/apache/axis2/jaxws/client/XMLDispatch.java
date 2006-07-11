/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.client;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.AxisController;
import org.apache.axis2.jaxws.param.Parameter;
import org.apache.axis2.jaxws.param.ParameterFactory;

public class XMLDispatch<T> extends BaseDispatch<T> {

    public Class type;
    
    public XMLDispatch() {
        super();
    }
    
    public XMLDispatch(AxisController ac) {
        super(ac);
    }
    
    public OMElement createMessageFromValue(Object value) {
        type = value.getClass();
        
        // FIXME: This is where the Message Model will be integrated instead of 
        // the ParameterFactory/Parameter APIs.
        Parameter param = ParameterFactory.createParameter(type);
        param.setValue(value);
        OMElement envelope = toOM(param, 
                axisController.getServiceClient().getOptions().getSoapVersionURI());
        return envelope;
    }

    public Object getValueFromMessage(OMElement message) {
        // FIXME: This is where the Message Model will be integrated instead of 
        // the ParameterFactory/Parameter APIs.
        Parameter param = ParameterFactory.createParameter(type);
        param = fromOM(message, param, 
                axisController.getServiceClient().getOptions().getSoapVersionURI());
        return param.getValue();
    }
    
    public Class getType() {
        return type;
    }
    
    public void setType(Class c) {
        type = c;
    }
}
