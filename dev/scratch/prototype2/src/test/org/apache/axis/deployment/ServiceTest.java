package org.apache.axis.deployment;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axis.deployment.metadata.phaserule.PhaseException;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Nov 19, 2004
 *         4:47:32 PM
 *
 */
public class ServiceTest extends TestCase {
   EngineRegistry er = null;
    public void testparseService1() throws PhaseException ,DeploymentException, AxisFault, XMLStreamException{
        String filename = "./target/test-resources" ;
        DeploymentEngine deploymentEngine = new DeploymentEngine(filename);
        er = deploymentEngine.start();
        try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        /*for (int i = 0; i < 150000; i++) {
            try {

            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
          //  System.out.println("Numer is eqaul :" + i);
        }  */
    }


  /*  public static void main(String args []){
       String filename = "./target/test-resources" ;
        DeploymentEngine deploymentEngine = new DeploymentEngine(filename);
        EngineRegistry er = null;
        try {
            er = deploymentEngine.start();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (PhaseException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (DeploymentException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }  */
}
