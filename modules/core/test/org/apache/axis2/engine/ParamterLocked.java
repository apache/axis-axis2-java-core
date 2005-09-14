package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.AxisFault;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
 * Date: Sep 1, 2005
 * Time: 9:49:19 AM
 */
public class ParamterLocked extends TestCase {

    AxisConfiguration ar;
    String repo ="./test-resources/deployment/ParaLockedRepo";



    protected void setUp() throws Exception {
        ConfigurationContextFactory builder = new ConfigurationContextFactory();
        ar = builder.buildConfigurationContext(repo).getAxisConfiguration();
    }

    public void testOveride_Non_locked_Para_Service(){
        try {
            assertNotNull(ar);
            ServiceDescription service = new ServiceDescription();
            ar.addService(service);
//            service.setParent(ar);
            InputStream in = new FileInputStream(repo + "/service_overide_non_locked_para.xml");
            ServiceBuilder sbuilder = new ServiceBuilder(in,null,service);
            sbuilder.populateService();
            assertNotNull(sbuilder);

        }catch (FileNotFoundException e) {
            fail("This can not fail with this FileNotFoundException " + e) ;
        } catch (DeploymentException e) {
            fail("This can not fail with this DeploymentException " + e) ;
        } catch (AxisFault axisFault) {
            fail("This can not fail with this AxisFault " + axisFault) ;
        }
    }

     public void testOveride_locked_Para_Service(){
        try {
            assertNotNull(ar);
            ServiceDescription service = new ServiceDescription();
            ar.addService(service);
//            service.setParent(ar);
            InputStream in = new FileInputStream(repo + "/service_overide_locked_para.xml");
            ServiceBuilder sbuilder = new ServiceBuilder(in,null,service);
            sbuilder.populateService();
            assertNotNull(sbuilder);
            fail("Parmter is locked can not overide" ) ;
        }catch (FileNotFoundException e) {
            fail("This can not fail with this FileNotFoundException " + e) ;
        } catch (DeploymentException e) {

        } catch (AxisFault axisFault) {
            fail("This can not fail with this AxisFault " + axisFault) ;
        }
     }

    public void testOveride_locked_Para_Operation(){
        try {
            assertNotNull(ar);
            ServiceDescription service = new ServiceDescription();
            ar.addService(service);
//            service.setParent(ar);
            InputStream in = new FileInputStream(repo + "/op_overide_global_para.xml");
            ServiceBuilder sbuilder = new ServiceBuilder(in,null,service);
            sbuilder.populateService();
            assertNotNull(sbuilder);
            fail("Parmter is locked can not overide" ) ;
        }catch (FileNotFoundException e) {
            fail("This can not fail with this FileNotFoundException " + e) ;
        } catch (DeploymentException e) {

        } catch (AxisFault axisFault) {
            fail("This can not fail with this AxisFault " + axisFault) ;
        }
    }

    public void testOveride_Service_locked_Para_Operation(){
        try {
            assertNotNull(ar);
            ServiceDescription service = new ServiceDescription();
            ar.addService(service);
//            service.setParent(ar);
            InputStream in = new FileInputStream(repo + "/Op_overide_Service_para.xml");
            ServiceBuilder sbuilder = new ServiceBuilder(in,null,service);
            sbuilder.populateService();
            fail("Parmter is locked can not overide" ) ;
        }catch (FileNotFoundException e) {
            fail("This can not fail with this FileNotFoundException " + e) ;
        } catch (DeploymentException e) {

        } catch (AxisFault axisFault) {
            fail("This can not fail with this AxisFault " + axisFault) ;
        }
    }

     public void testOveride_Non_locked_Para_Module(){
        try {
            assertNotNull(ar);
            ModuleDescription module = new ModuleDescription();
            module.setParent(ar);
            InputStream in = new FileInputStream(repo + "/module_overide_global_non_locked_para.xml");
            ModuleBuilder mbuilder = new ModuleBuilder(in,null,module);
            mbuilder.populateModule();
        }catch (FileNotFoundException e) {
            fail("This can not fail with this FileNotFoundException " + e) ;
        } catch (DeploymentException e) {

        }
     }

     public void testOveride_locked_Para_Module(){
        try {
            assertNotNull(ar);
            ModuleDescription module = new ModuleDescription();
            module.setParent(ar);
            InputStream in = new FileInputStream(repo + "/module_overide_locked_para.xml");
            ModuleBuilder mbuilder = new ModuleBuilder(in,null,module);
            mbuilder.populateModule();
            fail("Parmter is locked can not overide" ) ;
        }catch (FileNotFoundException e) {
            fail("This can not fail with this FileNotFoundException " + e) ;
        } catch (DeploymentException e) {

        }
     }
}
