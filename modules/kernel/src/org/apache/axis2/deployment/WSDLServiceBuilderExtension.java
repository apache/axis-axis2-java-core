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

package org.apache.axis2.deployment;

import java.util.Iterator;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * The class WSDLServiceBuilderExtension is a ServiceBuilderExtension which
 * facilitate to generate AxisServices based on WSDL 1.1 and WSDL 2.0 documents.
 * </p>
 * 
 * <p>
 * Axis2 ServiceDeployer use this extension.
 * </p>
 * 
 * @since 1.7.0
 */
public class WSDLServiceBuilderExtension extends AbstractServiceBuilderExtension {

    private static Log log = LogFactory.getLog(WSDLServiceBuilderExtension.class);

    public Map<String, AxisService> buildAxisServices(DeploymentFileData deploymentFileData)
            throws DeploymentException {
        ArchiveReader archiveReader = new ArchiveReader();
        Map<String, AxisService> wsdlservices = archiveReader.processWSDLs(deploymentFileData);
        if (wsdlservices != null && wsdlservices.size() > 0) {
            for (AxisService service : wsdlservices.values()) {
                Iterator<AxisOperation> operations = service.getOperations();
                while (operations.hasNext()) {
                    AxisOperation axisOperation = operations.next();
                    try {
                        getConfigurationContext().getAxisConfiguration().getPhasesInfo()
                                .setOperationPhases(axisOperation);
                    } catch (AxisFault e) {
                        throw new DeploymentException(e);
                    }
                }
            }
        }
        return wsdlservices;
    }
}
