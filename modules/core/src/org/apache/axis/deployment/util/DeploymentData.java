package org.apache.axis.deployment.util;

import org.apache.axis.description.OperationDescription;
import org.apache.axis.engine.Phase;
import org.apache.axis.phaseresolver.PhaseMetadata;

import javax.xml.namespace.QName;
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
public class DeploymentData {

    private static DeploymentData deploymentData;

    private ArrayList modules ;

    private ArrayList INPhases;
    private ArrayList OUTPhases;
    private ArrayList IN_FaultPhases;
    private ArrayList OUT_FaultPhases;

    private ArrayList oprationINPhases;
    private ArrayList oprationOUTPhases;
    private ArrayList oprationIN_FaultPhases;
    private ArrayList oprationOUT_FaultPhases;

    private DeploymentData() {
      modules = new ArrayList();
    }

    public static DeploymentData getInstance() {
        if (deploymentData == null) {
            deploymentData = new DeploymentData();
        }
        return deploymentData;
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

    private ArrayList getOperationInPhases() {
        oprationINPhases = new ArrayList();
        for (int i = 0; i < INPhases.size(); i++) {
            String phaseName = (String) INPhases.get(i);
            if (PhaseMetadata.PHASE_TRANSPORTIN.equals(phaseName) ||
                    PhaseMetadata.PHASE_PRE_DISPATCH.equals(phaseName) ||
                    PhaseMetadata.PHASE_DISPATCH.equals(phaseName) ||
                    PhaseMetadata.PHASE_POST_DISPATCH.equals(phaseName)) {
                continue;
            } else {
                oprationINPhases.add(new Phase(phaseName));
            }
        }
        return oprationINPhases;
    }

    private ArrayList getOperationOutPhases() {
        oprationOUTPhases = new ArrayList();
        for (int i = 0; i < OUTPhases.size(); i++) {
            String phaseName = (String) OUTPhases.get(i);
            if (PhaseMetadata.PHASE_TRANSPORT_OUT.equals(phaseName)) {
                continue;
            } else {
                oprationOUTPhases.add(new Phase(phaseName));
            }
        }
        return oprationOUTPhases;
    }

    private ArrayList getOperationInFaultPhases() {
        oprationIN_FaultPhases = new ArrayList();
        for (int i = 0; i < IN_FaultPhases.size(); i++) {
            String phaseName = (String) IN_FaultPhases.get(i);
            oprationIN_FaultPhases.add(new Phase(phaseName));
        }
        return oprationIN_FaultPhases;
    }

    private ArrayList getOperationOutFaultPhases() {
        oprationOUT_FaultPhases = new ArrayList();
        for (int i = 0; i < OUT_FaultPhases.size(); i++) {
            String phaseName = (String) OUT_FaultPhases.get(i);
            oprationOUT_FaultPhases.add(new Phase(phaseName));
        }
        return oprationOUT_FaultPhases;
    }

    public void setOperationPhases(OperationDescription operation) {
        if (operation != null) {
            operation.setRemainingPhasesInFlow(getOperationInPhases());
            operation.setPhasesOutFlow(getOperationOutPhases());
            operation.setPhasesInFaultFlow(getOperationInFaultPhases());
            operation.setPhasesOutFaultFlow(getOperationOutFaultPhases());
        }
    }

    public void addModule(QName moduleName){
        modules.add(moduleName);
    }

    public ArrayList getModules(){
        return modules;
    }

}
