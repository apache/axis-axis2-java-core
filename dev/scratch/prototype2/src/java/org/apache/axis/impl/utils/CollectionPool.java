/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis.impl.utils;

import java.util.HashMap;
import java.util.Stack;

public class CollectionPool {
	private static Stack hashMaps = new Stack();
	public static final HashMap createHashMap(int size) {
		if (hashMaps.isEmpty()) {
			return new HashMap(size);
		} else {
			return (HashMap) hashMaps.pop();
		}
	}
	public static final HashMap returnHashMap(HashMap map) {
		map.clear();
		return (HashMap) hashMaps.push(map);
	}

}
