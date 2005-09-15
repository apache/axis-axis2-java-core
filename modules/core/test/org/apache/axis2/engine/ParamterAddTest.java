package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.description.*;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;
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

/**
 * Author: Deepal Jayasinghe
 * Date: Aug 31, 2005
 * Time: 4:28:03 PM
 */

/**
 * To chcek locked is working corrcetly
 */

public class ParamterAddTest extends TestCase {

    private AxisConfiguration reg = new AxisConfigurationImpl();
    public void testAddParamterServiceLockedAtAxisConfig(){
        try {
            Parameter para = new ParameterImpl();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);
            reg.addParameter(para);

            ServiceDescription service = new ServiceDescription(new QName("Service1"));
            reg.addService(service);
//            service.setParent(reg);
            service.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");
        } catch (AxisFault axisFault) {

        }
    }

     public void testAddParamterModuleLockedAtAxisConfig(){
        try {
            Parameter para = new ParameterImpl();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);
            reg.addParameter(para);
            ModuleDescription module = new ModuleDescription(new QName("Service1"));
            module.setParent(reg);
            module.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");
        } catch (AxisFault axisFault) {

        }
    }

     public void testAddParamterOpeartionlockedByAxisConfig(){
        try {
            Parameter para = new ParameterImpl();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);
            reg.addParameter(para);

            ServiceDescription service = new ServiceDescription(new QName("Service1"));
            reg.addService(service);
          //  service.setParent(reg);

            OperationDescription opertion = new OperationDescription();
            opertion.setParent(service);
            opertion.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");


        } catch (AxisFault axisFault) {

        }
    }

     public void testAddParamterOpeartionLockebyService(){
        try {
            Parameter para = new ParameterImpl();
            para.setValue(null);
            para.setName("PARA_NAME");
            para.setLocked(true);

            ServiceDescription service = new ServiceDescription(new QName("Service1"));
//            service.setParent(reg);
            reg.addService(service);
            service.addParameter(para);

            OperationDescription opertion = new OperationDescription();
            opertion.setParent(service);
            opertion.addParameter(para);
            fail("This should fails with Parmter is locked can not overide");
        } catch (AxisFault axisFault) {

        }
    }


}
