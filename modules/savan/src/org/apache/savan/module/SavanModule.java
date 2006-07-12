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
import org.apache.axis2.modules.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.configuration.ConfigurationManager;

/**
 * Savan Module class. 
 *
 */
public class SavanModule implements Module  {

	private static final Log log = LogFactory.getLog(SavanModule.class);
	
	public void engageNotify(AxisDescription axisDescription) throws AxisFault {
	}

	public void init(ConfigurationContext configContext, AxisModule module) throws AxisFault {
		ConfigurationManager configurationManager = new ConfigurationManager ();
		try {
			configurationManager.configure();
		} catch (SavanException e) {
			log.error ("Exception thrown while trying to configure the Savan module",e);
		}
		
		configContext.setProperty(SavanConstants.CONFIGURATION_MANAGER,configurationManager);
	}

	public void shutdown(ConfigurationContext configurationContext) throws AxisFault {
	}

}
