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

import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.ServiceGroupDescription;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.description.ModuleConfiguration;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.AxisFault;
import org.apache.axis2.i18n.Messages;

import javax.xml.namespace.QName;
import java.util.Iterator;
public class ServiceGroupBuilder extends DescriptionBuilder{

    private OMElement servcice;
    public ServiceGroupBuilder(OMElement servcice, DeploymentEngine engine) {
        super.engine =engine;
        this.servcice = servcice;
    }


    public void populateServiceGroup(ServiceGroupDescription serviceGroup) throws DeploymentException {
        try {
            //Processing service level paramters
            Iterator itr = servcice.getChildrenWithName(
                    new QName(PARAMETERST));
            processParameters(itr,serviceGroup,serviceGroup.getParent());

            Iterator moduleConfigs = servcice.getChildrenWithName(new QName(MODULECONFIG));
            processServiceModuleConfig(moduleConfigs,serviceGroup.getParent(),serviceGroup);

            //processing servicewide modules which required to engage gloabbly
            Iterator moduleRefs = servcice.getChildrenWithName(
                    new QName(MODULEST));
            processModuleRefs(moduleRefs,serviceGroup);

            Iterator serviceitr = servcice.getChildrenWithName(new QName(SERVICE_ELEMENT));
            while (serviceitr.hasNext()) {
                OMElement service = (OMElement) serviceitr.next();

                OMAttribute serviceNameatt = service.getAttribute(
                        new QName(ATTNAME));
                String serviceName = serviceNameatt.getValue();
                if(serviceName == null){
                    throw new DeploymentException("Service Name required");
                } else {
                    ServiceDescription serviceDecs = engine.getCurrentFileItem().getService(
                            new QName(serviceName));
                    if(serviceDecs == null){
                        serviceDecs = new ServiceDescription(new QName(serviceName));
                        engine.getCurrentFileItem().addService(serviceDecs);
                        serviceDecs.setName(new QName(serviceName));
                    }
                    serviceDecs.setParent(serviceGroup);
                    serviceDecs.setClassLoader(engine.getCurrentFileItem().getClassLoader());
                    ServiceBuilder serviceBuilder = new ServiceBuilder(engine,serviceDecs);
                    serviceBuilder.populateService(service);
                }
            }

        } catch (DeploymentException e) {
            throw new DeploymentException(e);
        }

    }

    protected void processServiceModuleConfig(Iterator moduleConfigs ,
                                              ParameterInclude parent, ServiceGroupDescription service)
            throws DeploymentException {
        while (moduleConfigs.hasNext()) {
            OMElement moduleConfig = (OMElement) moduleConfigs.next();
            OMAttribute moduleName_att = moduleConfig.getAttribute(
                    new QName(ATTNAME));
            if(moduleName_att == null){
                throw new DeploymentException("Invalid module configuration");
            } else {
                String module = moduleName_att.getValue();
                ModuleConfiguration moduleConfiguration =
                        new ModuleConfiguration(new QName(module),parent);
                Iterator paramters=  moduleConfig.getChildrenWithName(new QName(PARAMETERST));
                processParameters(paramters,moduleConfiguration,parent);
                service.addModuleConfig(moduleConfiguration);
            }
        }
    }

    /**
     * To get the list og modules that is requird to be engage globally
     * @param moduleRefs  <code>java.util.Iterator</code>
     * @throws DeploymentException   <code>DeploymentException</code>
     */
    protected void processModuleRefs(Iterator moduleRefs ,ServiceGroupDescription serviceGroup)
            throws DeploymentException {
        try {
            while (moduleRefs.hasNext()) {
                OMElement moduleref = (OMElement) moduleRefs.next();
                OMAttribute moduleRefAttribute = moduleref.getAttribute(
                        new QName(REF));
                if(moduleRefAttribute !=null){
                    String refName = moduleRefAttribute.getValue();
                    if(engine.getModule(new QName(refName)) == null) {
                        throw new DeploymentException(Messages.getMessage(
                                DeploymentErrorMsgs.MODEULE_NOT_FOUND, refName));
                    } else {
                        serviceGroup.addModuleref(new QName(refName));
                    }
                }
            }
        }catch (AxisFault axisFault) {
            throw   new DeploymentException(axisFault);
        }
    }

}
