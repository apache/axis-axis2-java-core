package org.apache.axis2.cluster.tribes;

import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.tribes.context.ContextCommandMessage;
import org.apache.axis2.cluster.tribes.context.TribesContextManager;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChannelSender {

	private Channel channel;
	
    private static final Log log = LogFactory.getLog(ChannelSender.class);
    
	public void send(ContextCommandMessage msg) throws ClusteringFault {
		Member[] group = channel.getMembers();
		log.debug("Group size " + group.length);
		// send the message

		for (int i=0;i<group.length;i++) {
			printMember(group[i]);
		}

		try {
			channel.send(group, msg, 0);
		} catch (ChannelException e) {
			log.error("" + msg, e);
			String message = "Error sending command message : " + msg;
			throw new ClusteringFault (message, e);
		}
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	private void printMember(Member member) {
		member.getUniqueId();
		log.debug("\n===============================");
		log.debug("Member Name " + member.getName());
		log.debug("Member Host" + member.getHost());
		log.debug("Member Payload" + member.getPayload());
		log.debug("===============================\n");
	}
}
