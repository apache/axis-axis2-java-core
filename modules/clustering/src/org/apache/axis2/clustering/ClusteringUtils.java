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
package org.apache.axis2.clustering;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

/**
 *
 */
public class ClusteringUtils {

    private static final Log log = LogFactory.getLog(ClusteringUtils.class);

    public static boolean isURL(String location) {
        try {
            new URL(location);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static void loadServiceGroup(String serviceGroupName,
                                        ConfigurationContext configCtx,
                                        String tempDirectory) throws Exception {
        if (!serviceGroupName.endsWith(".aar")) {
            serviceGroupName += ".aar";
        }
        try {
            File serviceArchive;
            String axis2Repo = System.getProperty(Constants.AXIS2_REPO);
            if (isURL(axis2Repo)) {
                DataHandler dh = new DataHandler(new URL(axis2Repo + "services/" + serviceGroupName));
                String tempDir =
                        tempDirectory + File.separator +
                        (System.currentTimeMillis() + new Random().nextDouble());
                new File(tempDir).mkdirs();
                serviceArchive = new File(tempDir + File.separator + serviceGroupName);
                FileOutputStream out = new FileOutputStream(serviceArchive);
                dh.writeTo(out);
                out.close();
            } else {
                serviceArchive = new File(axis2Repo + File.separator + "services" +
                                          File.separator + serviceGroupName);
            }
            AxisServiceGroup asGroup =
                    DeploymentEngine.loadServiceGroup(serviceArchive, configCtx);
            configCtx.getAxisConfiguration().addServiceGroup(asGroup);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }
}
