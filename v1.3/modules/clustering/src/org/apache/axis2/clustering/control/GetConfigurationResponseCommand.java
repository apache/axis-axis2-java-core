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
package org.apache.axis2.clustering.control;

import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringUtils;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.AxisFault;

import java.io.FileNotFoundException;
import java.util.Iterator;

/**
 *
 */
public class GetConfigurationResponseCommand extends ControlCommand {

    private String[] serviceGroups;

    public void execute(ConfigurationContext configContext) throws ClusteringFault {
        AxisConfiguration axisConfig = configContext.getAxisConfiguration();

        // Run this code only if this node is not already initialized
        if (configContext.
                getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED) == null) {
            if (serviceGroups != null) {

                // Load all the service groups that are sent by the neighbour
                for (int i = 0; i < serviceGroups.length; i++) {
                    String serviceGroup = serviceGroups[i];
                    if (axisConfig.getServiceGroup(serviceGroup) == null) {
                        //Load the service group
                        try {
                            ClusteringUtils.loadServiceGroup(serviceGroup,
                                                             configContext,
                                                             System.getProperty("axis2.work.dir")); //TODO: Introduce a constant. work dir is a temp dir.
                        } catch (FileNotFoundException ignored) {
                        } catch (Exception e) {
                            throw new ClusteringFault(e);
                        }
                    }
                }

                // Unload all service groups which were not sent by the neighbour,
                // but have been currently loaded
                for (Iterator iter = axisConfig.getServiceGroups(); iter.hasNext();) {
                    AxisServiceGroup serviceGroup = (AxisServiceGroup) iter.next();
                    boolean foundServiceGroup = false;
                    for (int i = 0; i < serviceGroups.length; i++) {
                        String serviceGroupName = serviceGroups[i];
                        if (serviceGroup.getServiceGroupName().equals(serviceGroupName)) {
                            foundServiceGroup = true;
                            break;
                        }
                    }
                    if (!foundServiceGroup) {
                        boolean mustUnloadServiceGroup = true;
                        // Verify that this service was not loaded from within a module
                        // If so, we must not unload such a service
                        for (Iterator serviceIter = serviceGroup.getServices();
                             serviceIter.hasNext();) {
                            AxisService service = (AxisService) serviceIter.next();
                            if (service.isClientSide() ||
                                service.getParameter(AxisModule.MODULE_SERVICE) != null) {
                                mustUnloadServiceGroup = false;
                                break; // Do not unload service groups containing client side services
                            }
                        }
                        if (mustUnloadServiceGroup) {
                            try {
                                axisConfig.removeServiceGroup(serviceGroup.getServiceGroupName());
                            } catch (Exception e) {
                                throw new ClusteringFault(e);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setServiceGroups(String[] serviceGroups) {
        this.serviceGroups = serviceGroups;
    }

    public String toString() {
        return "GetConfigurationResponseCommand";
    }
}
