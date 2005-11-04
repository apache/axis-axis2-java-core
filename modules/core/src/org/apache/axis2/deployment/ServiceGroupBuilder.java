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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;
import java.util.Iterator;
public class ServiceGroupBuilder extends DescriptionBuilder{

    private OMElement servcice;
    public ServiceGroupBuilder(OMElement servcice, DeploymentEngine engine) {
        super.engine =engine;
        this.servcice = servcice;
    }
    public void populateServiceGroup(AxisServiceGroup axisServiceGroup) throws DeploymentException {
        try {
            //Processing service level paramters
            Iterator itr = servcice.getChildrenWithName(
                    new QName(PARAMETERST));
            processParameters(itr,axisServiceGroup,axisServiceGroup.getParent());

            Iterator moduleConfigs = servcice.getChildrenWithName(new QName(MODULECONFIG));
            processServiceModuleConfig(moduleConfigs,axisServiceGroup.getParent(),axisServiceGroup);

            //processing servicewide modules which required to engage gloabbly
            Iterator moduleRefs = servcice.getChildrenWithName(
                    new QName(MODULEST));
            processModuleRefs(moduleRefs,axisServiceGroup);

            Iterator serviceitr = servcice.getChildrenWithName(new QName(SERVICE_ELEMENT));
            while (serviceitr.hasNext()) {
                OMElement service = (OMElement) serviceitr.next();

                OMAttribute serviceNameatt = service.getAttribute(
                        new QName(ATTNAME));
                String serviceName = serviceNameatt.getAttributeValue();
                if(serviceName == null){
                    throw new DeploymentException(Messages.getMessage(
                            DeploymentErrorMsgs.SEERVICE_NAME_ERROR));
                } else {
                    AxisService axisService = engine.getCurrentFileItem().getService(
                            serviceName);
                    if(axisService == null){
                        axisService = new AxisService(new QName(serviceName));
                        engine.getCurrentFileItem().addService(axisService);
                        axisService.setName(new QName(serviceName));
                    }
                    // the service that has to be deploy
                    engine.getCurrentFileItem().getDeploybleServices().add(axisService);
                    axisService.setParent(axisServiceGroup);
                    axisService.setClassLoader(engine.getCurrentFileItem().getClassLoader());
                    ServiceBuilder serviceBuilder = new ServiceBuilder(engine,axisService);
                    serviceBuilder.populateService(service);
                }
            }

        } catch (DeploymentException e) {
            throw new DeploymentException(e);
        }

    }

    protected void processServiceModuleConfig(Iterator moduleConfigs ,
                                              ParameterInclude parent, AxisServiceGroup axisService)
            throws DeploymentException {
        while (moduleConfigs.hasNext()) {
            OMElement moduleConfig = (OMElement) moduleConfigs.next();
            OMAttribute moduleName_att = moduleConfig.getAttribute(
                    new QName(ATTNAME));
            if(moduleName_att == null){
                 throw new DeploymentException(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_MODULE_CONFIG));
            } else {
                String module = moduleName_att.getAttributeValue();
                ModuleConfiguration moduleConfiguration =
                        new ModuleConfiguration(new QName(module),parent);
                Iterator paramters=  moduleConfig.getChildrenWithName(new QName(PARAMETERST));
                processParameters(paramters,moduleConfiguration,parent);
                axisService.addModuleConfig(moduleConfiguration);
            }
        }
    }

    /**
     * To get the list og modules that is requird to be engage globally
     * @param moduleRefs  <code>java.util.Iterator</code>
     * @throws DeploymentException   <code>DeploymentException</code>
     */
    protected void processModuleRefs(Iterator moduleRefs ,AxisServiceGroup axisServiceGroup)
            throws DeploymentException {
        try {
            while (moduleRefs.hasNext()) {
                OMElement moduleref = (OMElement) moduleRefs.next();
                OMAttribute moduleRefAttribute = moduleref.getAttribute(
                        new QName(REF));
                if(moduleRefAttribute !=null){
                    String refName = moduleRefAttribute.getAttributeValue();
                    if(engine.getModule(new QName(refName)) == null) {
                        throw new DeploymentException(Messages.getMessage(
                                DeploymentErrorMsgs.MODEULE_NOT_FOUND, refName));
                    } else {
                        axisServiceGroup.addModuleref(new QName(refName));
                    }
                }
            }
        }catch (AxisFault axisFault) {
            throw   new DeploymentException(axisFault);
        }
    }

}
