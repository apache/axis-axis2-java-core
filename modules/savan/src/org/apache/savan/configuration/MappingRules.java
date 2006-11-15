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

import java.util.ArrayList;

/**
 *Encapsulates a date of a set of Mapping-Rules as defined by Savan configurations. 
 *(probably from a savan-config.xml file).
 */
public class MappingRules {

	private ArrayList actionMap = null;
	private ArrayList SOAPActionMap = null;
	
	public MappingRules () {
		actionMap = new ArrayList ();
		SOAPActionMap = new ArrayList ();
	}
	
	public void addAction (String action) {
		actionMap.add(action);
	}
	
	public boolean isActionPresent (String action) {
		return actionMap.contains(action);
	}
	
	public void addSOAPAction (String SOAPAction) {
		SOAPActionMap.add(SOAPAction);
	}
	
	public boolean isSOAPActionPresent (String SOAPAction) {
		return SOAPActionMap.contains(SOAPAction);
	}
	
}
