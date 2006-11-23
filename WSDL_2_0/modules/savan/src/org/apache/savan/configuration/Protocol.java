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

package org.apache.savan.configuration;

import org.apache.savan.util.UtilFactory;

/**
 *Encapsulates a date of a Protocol as defined by Savan configurations. 
 *(probably from a savan-config.xml file).
 */
public class Protocol {

	private String name;
	private UtilFactory utilFactory;
	private MappingRules mappingRules;
	private String defaultSubscriber;
	private String defaultFilter;
	
	public String getDefaultFilter() {
		return defaultFilter;
	}

	public String getDefaultSubscriber() {
		return defaultSubscriber;
	}

	public void setDefaultFilter(String defaultFilter) {
		this.defaultFilter = defaultFilter;
	}

	public void setDefaultSubscriber(String defaultSubscriber) {
		this.defaultSubscriber = defaultSubscriber;
	}

	public String getName() {
		return name;
	}
	
	public UtilFactory getUtilFactory() {
		return utilFactory;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUtilFactory(UtilFactory utilFactory) {
		this.utilFactory = utilFactory;
	}

	public MappingRules getMappingRules() {
		return mappingRules;
	}

	public void setMappingRules(MappingRules mappingRule) {
		this.mappingRules = mappingRule;
	}
}
