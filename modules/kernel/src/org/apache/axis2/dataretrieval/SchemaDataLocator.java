/*
* Copyright 2007 The Apache Software Foundation.
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

package org.apache.axis2.dataretrieval;

/**
 * Axis 2 Data Locator responsibles for retrieving Schema metadata.
 * The class is created as model for schema specific data locator; and also
 * easier for any future implementation schema specific data retrieval logic.
 *
 */

public class SchemaDataLocator  extends BaseAxisDataLocator  implements AxisDataLocator {
	
	protected SchemaDataLocator() {

	}

	/**
	 * Constructor
	 */
	protected SchemaDataLocator(ServiceData[] data) {
		dataList = data;
	}
	
}
