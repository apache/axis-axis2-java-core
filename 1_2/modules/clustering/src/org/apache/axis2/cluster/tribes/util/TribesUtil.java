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

package org.apache.axis2.cluster.tribes.util;

import org.apache.catalina.tribes.Member;

public class TribesUtil {

	public static void printMembers(Member[] members) {
		
		System.out.println("*****************PRINTING MEMBERS OF THE CURRENT TRIBES GROUP****************");

		if (members != null) {
			int length = members.length;
			for (int i = 0; i < length; i++) {
				byte[] hostBts = members[i].getHost();
				String HOST = null;
				if (hostBts != null) {
					for (int j = 0; j < hostBts.length; j++) {
						HOST = HOST == null ? ("" + hostBts[j]) : (HOST + "." + hostBts[j]);
					}
				}

				String port = "" + members[i].getPort();
				System.out.println("Member " + (i + 1) + " NAME:" + members[i].getName() + " HOST:"
						+ HOST + "  PORT:" + port);

			}
		}
	}

}
