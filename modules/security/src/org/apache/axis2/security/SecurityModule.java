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

package org.apache.axis2.security;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.modules.Module;

public class SecurityModule implements Module {

    /* (non-Javadoc)
     * @see org.apache.axis2.modules.Module#engageNotify(org.apache.axis2.description.AxisDescription)
     */
    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.modules.Module#shutdown(org.apache.axis2.engine.AxisConfiguration)
     */
    public void shutdown(AxisConfiguration axisSystem) throws AxisFault {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.modules.Module#init(org.apache.axis2.context.ConfigurationContext, org.apache.axis2.description.AxisModule)
     */
    public void init(ConfigurationContext configContext, AxisModule module) throws AxisFault {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

}
