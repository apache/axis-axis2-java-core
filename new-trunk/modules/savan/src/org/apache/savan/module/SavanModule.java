/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.savan.module;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.modules.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.configuration.ConfigurationManager;
import org.apache.savan.storage.SubscriberStore;

/**
 * Savan Module class. 
 *
 */
public class SavanModule implements Module  {

	private static final Log log = LogFactory.getLog(SavanModule.class);
	
	public void engageNotify(AxisDescription axisDescription) throws AxisFault {
		//adding a subscriber store to the description
		
		if (axisDescription instanceof AxisService) { //TODO remove this restriction

			//TODO set a suitable SubscriberStore for the service.
			
		}
		
	}

	public void init(ConfigurationContext configContext, AxisModule module) throws AxisFault {
		ConfigurationManager configurationManager = new ConfigurationManager ();
		try {
			ClassLoader moduleClassLoader = module.getModuleClassLoader();
			configurationManager.configure(moduleClassLoader);
		} catch (SavanException e) {
			log.error ("Exception thrown while trying to configure the Savan module",e);
		}
		
		configContext.setProperty(SavanConstants.CONFIGURATION_MANAGER,configurationManager);
	}

	public void shutdown(ConfigurationContext configurationContext) throws AxisFault {
	}

    public void applyPolicy(Policy policy, AxisDescription axisDescription) throws AxisFault {
        // TODO
    }

    public boolean canSupportAssertion(Assertion assertion) {
        // TODO 
        return true;
    }
    
    

}
