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

package org.apache.axis2.cluster.tribes.configuration;

import org.apache.axis2.cluster.configuration.ConfigurationManager;
import org.apache.axis2.cluster.configuration.ConfigurationManagerListener;
import org.apache.neethi.Policy;

public class TribesConfigurationManager implements ConfigurationManager {

	public void addConfigurationManagerListener(ConfigurationManagerListener listener) {
		throw new UnsupportedOperationException ();
	}

	public void applyPolicy(String serviceGroupName, Policy policy) {
		throw new UnsupportedOperationException ();
	}

	public void commit() {
		throw new UnsupportedOperationException ();
	}

	public void loadServiceGroup(String serviceGroupName) {
		throw new UnsupportedOperationException ();
	}

	public void prepare() {
		throw new UnsupportedOperationException ();		
	}

	public void reloadConfiguration() {
		throw new UnsupportedOperationException ();		
	}

	public void rollback() {
		throw new UnsupportedOperationException ();		
	}

	public void unloadServiceGroup(String serviceGroupName) {
		throw new UnsupportedOperationException ();		
	}

}
