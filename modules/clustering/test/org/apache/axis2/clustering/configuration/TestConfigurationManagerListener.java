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

package org.apache.axis2.clustering.configuration;

import java.util.ArrayList;
import org.apache.axis2.cluster.configuration.ConfigurationEvent;
import org.apache.axis2.cluster.configuration.ConfigurationManagerListener;
import org.apache.axis2.engine.AxisConfiguration;

public class TestConfigurationManagerListener implements ConfigurationManagerListener {

	ArrayList eventList = null;
	private AxisConfiguration axisConfiguration = null;
	
	public TestConfigurationManagerListener () {
		eventList = new ArrayList ();
	}
	
	public void clearEventList () {
		eventList.clear();
	}
	
	public ArrayList getEventList() {
		return eventList;
	}

	public void commitCalled(ConfigurationEvent event) {
		eventList.add(event);
	}

	public void configurationReloaded(ConfigurationEvent event) {
		eventList.add(event);
	}

	public void policyApplied(ConfigurationEvent event) {
		eventList.add(event);
	}

	public void prepareCalled(ConfigurationEvent event) {
		eventList.add(event);
	}

	public void rollbackCalled(ConfigurationEvent event) {
		eventList.add(event);
	}

	public void serviceGroupLoaded(ConfigurationEvent event) {
		eventList.add(event);
	}

	public void serviceGroupUnloaded(ConfigurationEvent event) {
		eventList.add(event);
	}

	public void setAxisConfiguration(AxisConfiguration axisConfiguration) {
		this.axisConfiguration = axisConfiguration;
	}
	
}
