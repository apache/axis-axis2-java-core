/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.axis2.clustering.tribes;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A util for manipulating classloaders to be used while serializing & deserializing Tribes messages
 */
public class ClassLoaderUtil {

    public static ClassLoader[] getClassLoaders(ConfigurationContext configurationContext) {
        AxisConfiguration configuration = configurationContext.getAxisConfiguration();
        List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
        classLoaders.add(configuration.getSystemClassLoader());
        classLoaders.add(ClassLoaderUtil.class.getClassLoader());
        for (Iterator iter = configuration.getServiceGroups(); iter.hasNext(); ) {
            AxisServiceGroup group = (AxisServiceGroup) iter.next();
            classLoaders.add(group.getServiceGroupClassLoader());
        }
        for (Object obj : configuration.getModules().values()) {
            AxisModule module = (AxisModule) obj;
            classLoaders.add(module.getModuleClassLoader());
        }
        return classLoaders.toArray(new ClassLoader[classLoaders.size()]);
    }
}
