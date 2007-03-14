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
	
	public static int CREATE_SERVICE_CONTEXT = 1;
	
	public static int CREATE_SERVICE_GROUP_CONTEXT = 2;

	public static int REMOVE_SERVICE_CONTEXT = 3;
	
	public static int REMOVE_SERVICE_GROUP_CONTEXT = 4;
	
	public static int UPDATE_STATE = 5;
	
	public static int UPDATE_STATE_MAP_ENTRY = 6;
	
	public static int LOAD_SERVICE_GROUP = 7;
	
	public static int UNLOAD_SERVICE_GROUP = 8;
	
	public static int APPLY_POLICY = 9;
	
	public static int RELOAD_CONFIGURATION = 10;
	
	public static int PREPARE = 11;
	
	public static int COMMIT = 12;
	
	public static int ROLLBACK = 13;
	
}
