package org.apache.axis.deployment;

import org.apache.axis.deployment.fileloader.utill.WSInfo;
import org.apache.axis.deployment.fileloader.utill.UnZipJAR;
import org.apache.axis.deployment.fileloader.utill.HDFileItem;

import java.util.Vector;
import java.io.File;

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
 *         Oct 13, 2004
 *         12:33:17 PM
 *
 */
public class DeploymentEngine {
    /**
     * This will store all the web Services to deploye
     */
    private Vector wsToDeploy = new Vector();
    /**
     * this will store all the web Services to undeploye
     */
    private Vector wsToUnDeploy = new Vector();


    public DeploymentEngine() {
    }
   /**
    *
    * @param file
    */
    public void addtowsToDeploy(HDFileItem file){
        wsToDeploy.add(file);
    }

    /**
     *
     * @param file
     */
    public void addtowstoUnDeploy(WSInfo file){
        wsToUnDeploy.add(file);
    }

    public void doDeploye(){
        //todo complete this
        if(wsToDeploy.size() >0){
            for (int i = 0; i < wsToDeploy.size(); i++) {
                HDFileItem fileItem = (HDFileItem) wsToDeploy.elementAt(i);
                UnZipJAR unZipJAR = new UnZipJAR();
                unZipJAR.listZipcontent(fileItem.getAbsolutePath());
               // System.out.println("File" + fileItem.toString());

                System.out.println("\nDeployement WS Name  "  + fileItem.getName());
            }
        }
        wsToDeploy.removeAllElements();
    }

    public void doUnDeploye(){
        //todo complete this
        if(wsToUnDeploy.size()>0){
            for (int i = 0; i < wsToUnDeploy.size(); i++) {
                WSInfo wsInfo = (WSInfo) wsToUnDeploy.elementAt(i);
                System.out.println("UnDeployement WS Name  "  + wsInfo.getFilename());
            }

        }
        wsToUnDeploy.removeAllElements();
    }


}
