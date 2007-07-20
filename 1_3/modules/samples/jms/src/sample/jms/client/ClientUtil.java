/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package sample.jms.client;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;

public class ClientUtil {

    public static ConfigurationContext createConfigurationContext(String repository) throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                repository,
        		repository + "/conf/axis2.xml");
        return configContext;
    }
    

    private static  AxisService createBaseService(QName serviceName) 
		throws AxisFault {

		AxisService service = new AxisService(serviceName.getLocalPart());
		return service;
    }


    public static AxisService createOutInService(QName serviceName, QName opName)
    	
    	throws AxisFault {
		AxisService service = createBaseService(serviceName);
    	AxisOperation axisOp = new OutInAxisOperation(opName);
    	service.addOperation(axisOp);
    	return service;
    }     
    
	public static OMElement createPayload(QName serviceName, QName operationName) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = fac.createOMNamespace("http://localhost/axis2/services/"
						+ serviceName.getLocalPart(), "my");
		OMElement method = fac.createOMElement(operationName.getLocalPart(), omNs);
		OMElement value = fac.createOMElement("myValue", omNs);
		value.addChild(fac.createOMText(value, "Isaac Asimov, The Foundation Trilogy"));
		method.addChild(value);
		return method;
	}
}
