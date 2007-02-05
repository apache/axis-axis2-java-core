package org.apache.axis2.cluster.tribes;

import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;

public class TransientTribesMemberInfo implements MembershipListener {

	private List <Member> liveNodes = new ArrayList <Member>();
	private List <Member> deadNodes = new ArrayList <Member>();
    	
	
	public void memberAdded(Member member) {
		liveNodes.add(member);
		deadNodes.remove(member);
	}

	public void memberDisappeared(Member member) {
		liveNodes.remove(member);
		deadNodes.add(member);
	}
	
	public List <Member> getLiveNodes(){
		return liveNodes;
	}
	
	public List <Member> getDeadNodes(){
		return deadNodes;
	}
}
