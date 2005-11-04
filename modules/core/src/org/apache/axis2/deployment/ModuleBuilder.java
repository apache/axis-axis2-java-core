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
 */

package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
/**
 * This class is to convert OM->ServiceDescrption , where first create OM from services.xml and
 * then populate service description by using OM
 */
public class ModuleBuilder extends DescriptionBuilder{

    private ModuleDescription module;
    public ModuleBuilder(InputStream serviceInputSteram, DeploymentEngine engine
            ,ModuleDescription module) {
        super(serviceInputSteram, engine);
        this.module = module;
    }


    public void populateModule() throws DeploymentException {
        try {
            OMElement moduleElement = buildOM();

            // Setting Module Name
            OMAttribute moduleNameAtt = moduleElement.getAttribute(
                    new QName(ATTNAME));
            if(moduleNameAtt != null){
                String moduleName = moduleNameAtt.getAttributeValue();
                if (moduleName != null && !"".equals(moduleName)) {
                    module.setName(new QName(moduleName));
                } else {
                    module.setName(new QName(getShortFileName(engine.getCurrentFileItem()
                            .getServiceName())));
                }
            }else {
                module.setName(new QName(getShortFileName(engine.getCurrentFileItem()
                        .getServiceName())));
            }

            // Setting Module Class , if it is there
            OMAttribute moduleClassAtt = moduleElement.getAttribute(
                    new QName(CLASSNAME));
            if(moduleClassAtt !=null){
                String moduleClass = moduleClassAtt.getAttributeValue();
                if(moduleClass !=null && !"".equals(moduleClass)){
                    if (engine !=null) {
                        engine.getCurrentFileItem().setModuleClass(moduleClass);
                    }
                }
            }

            //processing Paramters
            //Processing service level paramters
            Iterator itr = moduleElement.getChildrenWithName(
                    new QName(PARAMETERST));
            processParameters(itr,module,module.getParent());

            //process INFLOW
            OMElement inFlow = moduleElement.getFirstChildWithName(
                    new QName(INFLOWST));
            if(inFlow !=null){
                module.setInFlow(processFlow(inFlow,module));
            }

            OMElement outFlow = moduleElement.getFirstChildWithName(
                    new QName(OUTFLOWST));
            if(outFlow !=null){
                module.setOutFlow(processFlow(outFlow,module));
            }

            OMElement inFaultFlow = moduleElement.getFirstChildWithName(
                    new QName(IN_FAILTFLOW));
            if(inFaultFlow !=null){
                module.setFaultInFlow(processFlow(inFaultFlow,module));
            }

            OMElement outFaultFlow = moduleElement.getFirstChildWithName(
                    new QName(OUT_FAILTFLOW));
            if(outFaultFlow !=null){
                module.setFaultOutFlow(processFlow(outFaultFlow,module));
            }

            //processing Operations
            Iterator op_itr = moduleElement.getChildrenWithName(new QName(OPRATIONST));
            ArrayList operations = processOperations(op_itr);
            for (int i = 0; i < operations.size(); i++) {
                AxisOperation opeartion = (AxisOperation) operations.get(i);
                module.addOperation(opeartion);
            }


        } catch (XMLStreamException e) {
            throw new DeploymentException(e);

        }
    }


    private ArrayList processOperations(Iterator opeartinsItr) throws DeploymentException {
        ArrayList operations = new ArrayList();
        while (opeartinsItr.hasNext()) {
            OMElement operation = (OMElement) opeartinsItr.next();

            // /getting opeartion name
            OMAttribute op_name_att = operation.getAttribute(
                    new QName(ATTNAME));
            if(op_name_att == null){
                throw new DeploymentException(Messages.getMessage(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_OP
                        ,"operation name missing")));
            }
            OMAttribute op_mep_att = operation.getAttribute(
                    new QName(MEP));
            String mepURL =null;
            AxisOperation op_descrip;
            if(op_mep_att !=null){
                mepURL= op_mep_att.getAttributeValue();
            }

            if(mepURL == null){
                // assuming in-out mep
                op_descrip = new InOnlyAxisOperation();
            } else {
                try {
                    op_descrip = AxisOperationFactory.getOperetionDescription(mepURL);
                } catch (AxisFault axisFault) {
                    throw new DeploymentException(Messages.getMessage(Messages.getMessage(
                            DeploymentErrorMsgs.OPERATION_PROCESS_ERROR,axisFault.getMessage())));
                }
            }
            String opname = op_name_att.getAttributeValue();
//            AxisOperation op_descrip = new AxisOperation();
            op_descrip.setName(new QName(opname));

            //Opeartion Paramters
            Iterator paramters = operation.getChildrenWithName(
                    new QName(PARAMETERST));
            ArrayList wsamapping =   processParameters(paramters,op_descrip,module);
            op_descrip.setWsamappingList(wsamapping);


            //setting the mep of the operation


            // loading the message recivers
            OMElement receiverElement = operation.getFirstChildWithName(
                    new QName(MESSAGERECEIVER));
            if(receiverElement !=null){
                MessageReceiver messageReceiver = loadMessageReceiver(
                        engine.getCurrentFileItem().getClassLoader(),receiverElement);
                op_descrip.setMessageReceiver(messageReceiver);
            }  else {
                //setting default message reciver
                MessageReceiver msgReceiver = loadDefaultMessageReceiver();
                op_descrip.setMessageReceiver(msgReceiver);
            }
            //Process Module Refs
            Iterator modules = operation.getChildrenWithName(
                    new QName(MODULEST));
            processOperationModuleRefs(modules, op_descrip);

            //setting Operation phase
            PhasesInfo info = engine.getPhasesinfo();
            info.setOperationPhases(op_descrip);

            //adding the opeartion
            operations.add(op_descrip);
        }
        return operations;
    }


}
