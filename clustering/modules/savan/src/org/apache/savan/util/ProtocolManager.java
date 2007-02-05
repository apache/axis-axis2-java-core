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

package org.apache.savan.util;

import org.apache.savan.SavanConstants;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.configuration.ConfigurationManager;
import org.apache.savan.configuration.Protocol;

/**
 * Utility class to extract the Protocol type from a MessageContext
 */
public class ProtocolManager {

	public static Protocol getMessageProtocol (SavanMessageContext smc) throws SavanException {
		//TODO to this depending on Protocol rules. //TODO make this algorithm efficient
		
		ConfigurationManager configurationManager = (ConfigurationManager) smc.getConfigurationContext().getProperty(SavanConstants.CONFIGURATION_MANAGER);
		if (configurationManager==null)
			throw new SavanException ("Cant find the Configuration Manager");
		
		return (Protocol) configurationManager.getProtocolMap().get("eventing");
		
	}
	
}
