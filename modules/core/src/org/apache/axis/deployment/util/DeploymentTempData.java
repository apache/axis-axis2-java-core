package org.apache.axis.deployment.util;

import java.util.ArrayList;

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
 * Author : Deepal Jayasinghe
 * Date: May 12, 2005
 * Time: 7:14:26 PM
 */
public class DeploymentTempData {

    private static DeploymentTempData deploymentTempData;

    private ArrayList INPhases ;
    private ArrayList OUTPhases ;
    private ArrayList IN_FaultPhases ;
    private ArrayList OUT_FaultPhases ;

    private DeploymentTempData() {

    }

    public static DeploymentTempData getInstance(){
        if(deploymentTempData == null ){
            deploymentTempData = new DeploymentTempData();
        }
        return deploymentTempData;
    }



    public void setINPhases(ArrayList INPhases) {
        this.INPhases = INPhases;
    }

    public void setOUTPhases(ArrayList OUTPhases) {
        this.OUTPhases = OUTPhases;
    }

    public void setIN_FaultPhases(ArrayList IN_FaultPhases) {
        this.IN_FaultPhases = IN_FaultPhases;
    }

    public void setOUT_FaultPhases(ArrayList OUT_FaultPhases) {
        this.OUT_FaultPhases = OUT_FaultPhases;
    }

    public ArrayList getINPhases() {
        return INPhases;
    }

    public ArrayList getOUTPhases() {
        return OUTPhases;
    }

    public ArrayList getIN_FaultPhases() {
        return IN_FaultPhases;
    }

    public ArrayList getOUT_FaultPhases() {
        return OUT_FaultPhases;
    }


}
