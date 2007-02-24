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

package org.apache.axis2.cluster.tribes;

public interface CommandType {
	
	public static String CREATE_SERVICE_CONTEXT = "CREATE_SERVICE_CONTEXT";
	
	public static String CREATE_SERVICE_GROUP_CONTEXT = "CREATE_SERVICE_GROUP_CONTEXT";

	public static String REMOVE_SERVICE_CONTEXT = "REMOVE_SERVICE_CONTEXT";
	
	public static String REMOVE_SERVICE_GROUP_CONTEXT = "REMOVE_SERVICE_GROUP_CONTEXT";
	
	public static String UPDATE_STATE = "UPDATE_STATE";
	
	public static String UPDATE_STATE_MAP_ENTRY = "UPDATE_STATE_MAP_ENTRY";
	
}
