/*
 * Copyright  2004 The Apache Software Foundation.
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

/*
 * Created on Sep 26, 2004
 *
 */
package org.apache.axis.om;

import org.apache.axis.om.OMException;

import java.io.PrintStream;

/**
 * @author Dasarath Weeratunge
 *
 */
public class OMNamespace extends OMNodeImpl {
	String prefix;

	protected OMNamespace(String uri, String prefix) {
		value= uri;
		this.prefix= prefix;
	}

	public boolean equals(OMNamespace ns) {
		return ((prefix == null && ns.prefix == null) || (prefix != null && prefix.equals(ns.prefix)))
			&& value.equals(ns.value);
	}

	public boolean equals(String uri, String prefix) {
		return ((prefix == null && this.prefix == null) || (prefix != null && prefix.equals(this.prefix)))
			&& value.equals(uri);
	}

	public void print(PrintStream s) {
		s.print("xmlns");
		if (prefix != null) {
			s.print(':');
			s.print(prefix);
		}
		s.print('=');
		s.print('"');
		s.print(value);
		s.print('"');
	}

	public boolean isDefaultNs() {
		return prefix == null;
	}

	public String getPrefix() {
		return prefix;
	}

	public OMNode getNextSibling() throws OMException {
		return nextSibling;
	}
}
