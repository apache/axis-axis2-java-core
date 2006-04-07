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

package org.apache.axis2.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.axis2.description.PolicyInclude;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyWriter;
import org.apache.ws.policy.util.StAXPolicyWriter;

public class PolicyUtil {
    
	public static void writePolicy(PolicyInclude policy, OutputStream out) {
		if (policy != null) {
			Policy pl = policy.getEffectivePolicy();
			if (pl != null) {
				PolicyWriter write = PolicyFactory
						.getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);
				write.writePolicy(pl, out);
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				PrintWriter write = new PrintWriter(out);
				write.write("<policy>no policy found</policy>");
				write.flush();
				write.close();
			}
		} else {
			PrintWriter write = new PrintWriter(out);
			write.write("<policy>no policy found</policy>");
			write.flush();
			write.close();
		}
	}

	public static String getPolicyAsString(Policy policy) {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		StAXPolicyWriter pwtr = (StAXPolicyWriter) PolicyFactory
				.getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);
		
		pwtr.writePolicy(policy, baos);
		return getSafeString(baos.toString());
	}
	
	private static String getSafeString(String unsafeString) {
		StringBuffer sbuf = new StringBuffer();
		
		char[] chars = unsafeString.toCharArray();
		
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			
			switch (c) {
				case '\\' :
					sbuf.append('\\'); sbuf.append('\\');
					break;
				case '"' :
					sbuf.append('\\'); sbuf.append('"');
					break;
				case '\n':
					sbuf.append('\\'); sbuf.append('n'); 
					break;
				case '\r':
					sbuf.append('\\'); sbuf.append('r'); 
					break;
				default :
					sbuf.append(c);					
			}			
		}
		
		return sbuf.toString();
	}
}
