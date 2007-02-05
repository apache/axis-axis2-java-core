package org.apache.axis2.cluster.tribes;

import java.io.Serializable;

import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Member;

public class TransientTribesChannelInfo implements ChannelListener{

	private long messageCount = 0;
    private long grpCtxCreationCount = 0;
    private long srvCtxCreationCount = 0;
    private long grpCtxRemoveCount = 0;
    private long srvCtxRemoveCount = 0;
    private long updateStateCount = 0;
    
	public boolean accept(Serializable msg, Member sender) {
		return msg instanceof String;
	}

	public void messageReceived(Serializable msg, Member sender) {
		messageCount++;

		System.out.println("Tribes message " + msg);
		
		if (msg instanceof TribesCommandMessage){
			TribesCommandMessage comMsg =  (TribesCommandMessage)msg;
			
			if(comMsg.getCommandName().equals(CommandConstants.CREATE_SERVICE_GROUP_CONTEXT)){
				grpCtxCreationCount ++;
			}else if(comMsg.getCommandName().equals(CommandConstants.CREATE_SERVICE_CONTEXT)){
				srvCtxCreationCount ++;
			}else if(comMsg.getCommandName().equals(CommandConstants.REMOVE_SERVICE_GROUP_CONTEXT)){
				grpCtxRemoveCount ++;
			}else if(comMsg.getCommandName().equals(CommandConstants.REMOVE_SERVICE_CONTEXT)){
				srvCtxRemoveCount ++;
			}else if(comMsg.getCommandName().equals(CommandConstants.UPDATE_STATE)){
				updateStateCount ++;
			}
		}
	}

	public long getGrpCtxCreationCount() {
		return grpCtxCreationCount;
	}

	public long getGrpCtxRemoveCount() {
		return grpCtxRemoveCount;
	}

	public long getMessageCount() {
		return messageCount;
	}

	public long getSrvCtxCreationCount() {
		return srvCtxCreationCount;
	}

	public long getSrvCtxRemoveCount() {
		return srvCtxRemoveCount;
	}

	public long getUpdateStateCount() {
		return updateStateCount;
	}

}
