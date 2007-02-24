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

import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;

public class TransientTribesMemberInfo implements MembershipListener {

	private List<Member> liveNodes = new ArrayList<Member>();

	private List<Member> deadNodes = new ArrayList<Member>();

	public void memberAdded(Member member) {
		liveNodes.add(member);
		deadNodes.remove(member);
	}

	public void memberDisappeared(Member member) {
		liveNodes.remove(member);
		deadNodes.add(member);
	}

	public List<Member> getLiveNodes() {
		return liveNodes;
	}

	public List<Member> getDeadNodes() {
		return deadNodes;
	}
}
