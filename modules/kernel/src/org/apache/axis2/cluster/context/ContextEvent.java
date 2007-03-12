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

package org.apache.axis2.cluster.context;

public class ContextEvent {
	String contextID;
	String parentContextID;
	int contextType;
	String descriptionID = null;
	

	public int getContextType() {
		return contextType;
	}
	public void setContextType(int contextType) {
		this.contextType = contextType;
	}
	public String getContextID() {
		return contextID;
	}
	public void setContextID(String contextID) {
		this.contextID = contextID;
	}
	public String getDescriptionID() {
		return descriptionID;
	}
	public void setDescriptionID(String descriptionID) {
		this.descriptionID = descriptionID;
	}
	public String getParentContextID() {
		return parentContextID;
	}
	public void setParentContextID(String parentContextID) {
		this.parentContextID = parentContextID;
	}

	
	
}
