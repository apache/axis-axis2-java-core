package org.apache.axis2.cluster.tribes;

import java.io.Serializable;
import java.util.Map;

import org.apache.axis2.cluster.context.ContextEvent;
import org.apache.axis2.cluster.tribes.context.ContextCommandMessage;
import org.apache.axis2.cluster.tribes.context.ContextListenerEventType;
import org.apache.axis2.cluster.tribes.context.ContextType;
import org.apache.axis2.cluster.tribes.context.ContextUpdateEntryCommandMessage;
import org.apache.axis2.cluster.tribes.context.ContextUpdater;
import org.apache.axis2.cluster.tribes.context.TribesContextManager;
import org.apache.catalina.tribes.Member;


public class ChannelListener implements org.apache.catalina.tribes.ChannelListener {

	ContextUpdater updater = null;
	TribesContextManager contextManager = null;
	
	public void setContextManager(TribesContextManager contextManager) {
		this.contextManager = contextManager;
	}

	public void setUpdater(ContextUpdater updater) {
		this.updater = updater;
	}

	public boolean accept(Serializable msg, Member sender) {
		return true;
	}

	public void messageReceived(Serializable msg, Member sender) {

		if (!(msg instanceof ContextCommandMessage)) {
			return;
		}

		ContextCommandMessage comMsg = (ContextCommandMessage) msg;

		// TODO make sure to remove from the duplicate lists when remove is
		// requested for both service group and service contexts
		// TODO fix this to support scopes other than SOAP Session.

		if (comMsg.getCommandName().equals(CommandType.CREATE_SERVICE_GROUP_CONTEXT)) {

			// add to the duplicate list to prevent cyclic replication
			contextManager.addToDuplicateServiceGroupContexts(comMsg.getContextId());
			updater.addServiceGroupContext(comMsg.getContextId());

			ContextEvent event = new ContextEvent();
			event.setContextType(ContextType.SERVICE_GROUP_CONTEXT);
			event.setContextID(comMsg.getContextId());
			event.setParentContextID(comMsg.getParentId());
			event.setDescriptionID(comMsg.getAxisDescriptionName());

			contextManager.notifyListeners(event, ContextListenerEventType.ADD_CONTEXT);

		} else if (comMsg.getCommandName().equals(CommandType.CREATE_SERVICE_CONTEXT)) {

			// add to the duplicate list to prevent cyclic replication
			contextManager.addToDuplicateServiceContexts(comMsg.getParentId()
					+ comMsg.getContextId());
			updater.addServiceContext(comMsg.getParentId(), comMsg.getContextId());

			ContextEvent event = new ContextEvent();
			event.setContextType(ContextType.SERVICE_CONTEXT);
			event.setContextID(comMsg.getContextId());
			event.setParentContextID(comMsg.getParentId());
			event.setDescriptionID(comMsg.getAxisDescriptionName());

			contextManager.notifyListeners(event, ContextListenerEventType.ADD_CONTEXT);

		} else if (comMsg.getCommandName().equals(CommandType.UPDATE_STATE)) {

			if (comMsg.getContextType() == ContextType.SERVICE_GROUP_CONTEXT) {

				ContextEvent event = new ContextEvent();
				event.setContextType(ContextType.SERVICE_GROUP_CONTEXT);
				event.setContextID(comMsg.getContextId());
				event.setParentContextID(comMsg.getParentId());
				event.setDescriptionID(comMsg.getAxisDescriptionName());

				contextManager.notifyListeners(event, ContextListenerEventType.UPDATE_CONTEXT);

			} else if (comMsg.getContextType() == ContextType.SERVICE_CONTEXT) {

				ContextEvent event = new ContextEvent();
				event.setContextType(ContextType.SERVICE_CONTEXT);
				event.setContextID(comMsg.getContextId());
				event.setParentContextID(comMsg.getParentId());
				event.setDescriptionID(comMsg.getAxisDescriptionName());

				contextManager.notifyListeners(event, ContextListenerEventType.UPDATE_CONTEXT);

			}

		} else if (comMsg.getCommandName().equals(CommandType.UPDATE_STATE_MAP_ENTRY)) {

			ContextUpdateEntryCommandMessage mapEntryMsg = (ContextUpdateEntryCommandMessage) comMsg;
			if (mapEntryMsg.getCtxType() == ContextUpdateEntryCommandMessage.SERVICE_GROUP_CONTEXT) {
				Map props = updater.getServiceGroupProps(comMsg.getContextId());
				if (mapEntryMsg.getOperation() == ContextUpdateEntryCommandMessage.ADD_OR_UPDATE_ENTRY) {
					props.put(mapEntryMsg.getKey(), mapEntryMsg.getValue());
				} else {
					props.remove(mapEntryMsg.getKey());
				}
			} else if (mapEntryMsg.getCtxType() == ContextUpdateEntryCommandMessage.SERVICE_CONTEXT) {
				Map props = updater.getServiceProps(comMsg.getParentId(), comMsg.getContextId());
				if (mapEntryMsg.getOperation() == ContextUpdateEntryCommandMessage.ADD_OR_UPDATE_ENTRY) {
					props.put(mapEntryMsg.getKey(), mapEntryMsg.getValue());
				} else {
					props.remove(mapEntryMsg.getKey());
				}
			}
		}
	}
	
}
