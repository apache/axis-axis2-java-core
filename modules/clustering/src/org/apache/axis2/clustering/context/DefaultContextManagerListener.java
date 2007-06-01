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

package org.apache.axis2.clustering.context;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultContextManagerListener implements ContextManagerListener {

    private ConfigurationContext configurationContext;
    private static final Log log = LogFactory.getLog(DefaultContextManagerListener.class);

    public void contextUpdated(ContextClusteringCommand message) throws ClusteringFault {
        log.debug("Enter: DefaultContextManagerListener::contextRemoved");
        message.execute(configurationContext);
        log.debug("Exit: DefaultContextManagerListener::contextRemoved");
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }
}
