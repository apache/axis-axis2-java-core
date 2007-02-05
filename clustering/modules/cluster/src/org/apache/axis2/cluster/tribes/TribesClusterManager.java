package org.apache.axis2.cluster.tribes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TribesClusterManager implements ClusterManager, ChannelListener {

	private static final Log log = LogFactory.getLog(TribesClusterManager.class);	
	private ConfigurationContext configContext;
	private Channel channel;
	private ContextManager ctxManager;
	private long timeout = 1000L; // this should be configured in the axis2.xml
	private Map<String, List> orphanedServiceCtxs = new HashMap <String, List>();
	private Map<String, String> duplicateServiceGrps = new HashMap <String, String>();
	private Map<String, String> duplicateServiceCtxs = new HashMap <String, String>();
	
	public void init(ConfigurationContext context) {
		log.debug("initializing tibes");
		
		this.configContext = context;
		
		TransientTribesChannelInfo channelInfo = new TransientTribesChannelInfo();
		TransientTribesMemberInfo memberInfo = new TransientTribesMemberInfo();
		
		configContext.setProperty("MEMBER_INFO", memberInfo);
		configContext.setProperty("CHANNEL_INFO", channelInfo);
		
		channel = new GroupChannel();
		try {
			channel.addChannelListener(this);
			channel.addChannelListener(channelInfo);
			channel.addMembershipListener(memberInfo);
			channel.start(Channel.DEFAULT);
			ctxManager = new ContextManager(channel,timeout,context.getAxisConfiguration().getSystemClassLoader());
		} catch (ChannelException e) {
			log.error("Error starting Tribes channel", e);
		}
		
		registerTribesInfoService(configContext);
	}	
	
	
	private void registerTribesInfoService(ConfigurationContext configContext2) {				
		try {
			AxisService service = AxisService.createService(
					"org.apache.axis2.cluster.tribes.TribesInfoWebService",
					configContext.getAxisConfiguration(), RPCMessageReceiver.class);
			
			configContext.getAxisConfiguration().addService(service);
		} catch (AxisFault e) {
			log.error("Unable to create Tribes info web service",e);
		}		
	}

	public void addContext(String contextId, String parentContextId, AbstractContext context) {
		TribesCommandMessage comMsg = null;
		
		// The ServiceContex does not define a contextId
		// therefore the service name is used
		if (context instanceof ServiceContext){
			
			if (duplicateServiceCtxs.get(parentContextId + contextId) != null){
				return; // this is a duplicate replication request
			}
			
			if(ctxManager.getServiceGroupProps(parentContextId) != null){	
				ctxManager.addServiceContext(parentContextId, contextId);
				comMsg = new TribesCommandMessage(CommandConstants.CREATE_SERVICE_CONTEXT,
						parentContextId,contextId,contextId);
				send(comMsg);
			}else{
				// put in the queue until the service group context is created with an id
				comMsg = new TribesCommandMessage(CommandConstants.CREATE_SERVICE_CONTEXT,
						parentContextId,contextId,contextId);
				
				AxisServiceGroup serviceGroupDesc = ((ServiceContext)context).getServiceGroupContext().getDescription();
				List<TribesCommandMessage> list = (List)orphanedServiceCtxs.get(serviceGroupDesc.getServiceGroupName());
				if (list == null){
					list = new ArrayList <TribesCommandMessage>();
					orphanedServiceCtxs.put(serviceGroupDesc.getServiceGroupName(), list);
				}
				list.add(comMsg);				
			}
		}else if (context instanceof ServiceGroupContext){
			
			if (duplicateServiceGrps.get(contextId) != null){
				return; // this is a duplicate replication request
			}		
			
			
			ServiceGroupContext srvGrpCtx = (ServiceGroupContext)context; 
			
			// The new serialization code sets the service group name as it's id initially
			if(srvGrpCtx.getId().equals(srvGrpCtx.getDescription().getServiceGroupName())){
				return;
			}
			
			ctxManager.addServiceGroupContext(contextId);
			comMsg = new TribesCommandMessage(CommandConstants.CREATE_SERVICE_GROUP_CONTEXT,
                    "",contextId,srvGrpCtx.getDescription().getServiceGroupName());
			
			send(comMsg);
			
			// now iterate through the list of service contexts and replicate them
			List<TribesCommandMessage> list = orphanedServiceCtxs.get(srvGrpCtx.getDescription().getServiceGroupName());
			for(TribesCommandMessage command : list){				
				ctxManager.addServiceContext(contextId, command.getContextId());
				command.setParentId(contextId);
				send(command);				
			}
			
			orphanedServiceCtxs.remove(srvGrpCtx.getDescription().getServiceGroupName());
		}		
	    	
	}
     
	/*
	public void addProperty(AbstractContext ctx,String contextId, String parentId, String propertyName, Object propertyValue) {
        if (ctx instanceof ServiceContext){
			ctxManager.addPropToServiceContext(parentId, contextId, propertyName, propertyValue);
		}else{
			ctxManager.addPropToServiceGroupContext(contextId, propertyName, propertyValue);
		}
	}*/

	public void removeContext(String contextId, String parentContextId, AbstractContext context) {
		TribesCommandMessage comMsg = null;
		if (context instanceof ServiceContext){
			ctxManager.removeServiceContext(parentContextId, contextId);
			comMsg = new TribesCommandMessage(CommandConstants.CREATE_SERVICE_GROUP_CONTEXT,
		                                      parentContextId,contextId, contextId);
		}else if (context instanceof ServiceGroupContext){
	     	ctxManager.removeServiceGroupContext(contextId);
	     	comMsg = new TribesCommandMessage(CommandConstants.REMOVE_SERVICE_GROUP_CONTEXT,
		                                      "",contextId,
		                                      ((ServiceGroupContext)context).getDescription().getServiceGroupName());
		}
		
		send(comMsg);
	}

	/*
	public void removeProperty(AbstractContext ctx, String contextId, String parentId, String propertyName) {
		if (ctx instanceof ServiceContext){
			ctxManager.removePropFromServiceContext(parentId, contextId, propertyName);			
		}else{
			ctxManager.removePropFromServiceGroupContext(contextId, propertyName);
		}
	}*/
	
	public void updateState(ServiceContext ctx) {
		
		String parentId = ctx.getServiceGroupContext().getId();
		String contextId = ctx.getAxisService().getName();
		Map props = ctx.getProperties();

		List<TribesMapEntryMessage> mapEntryMsgs = ctxManager.updateStateOnServiceContext(parentId, contextId, props);

		for(TribesMapEntryMessage msg : mapEntryMsgs){
			send(msg);
		}
		
		Map serviceGrpProps = ctx.getServiceGroupContext().getProperties();
		mapEntryMsgs = ctxManager.updateStateOnServiceGroupContext(parentId,serviceGrpProps);		

		for(TribesMapEntryMessage msg : mapEntryMsgs){
			send(msg);
		}
		
		TribesCommandMessage comMsg = new TribesCommandMessage(
				CommandConstants.UPDATE_STATE, parentId, contextId, contextId);

		send(comMsg);
	}

	public boolean accept(Serializable msg, Member sender) {
		//return msg instanceof TribesCommandMessage;
		return true;
	}

	public void messageReceived(Serializable msg, Member sender) {
			
		if (!(msg instanceof TribesCommandMessage)){
			return;
		}
		
		System.out.println("msg received " + msg);
				
		TribesCommandMessage comMsg =  (TribesCommandMessage)msg;
		
		// TODO make sure to remove from the duplicate lists when remove is requested for both service group and service contexts
		
		if(comMsg.getCommandName().equals(CommandConstants.CREATE_SERVICE_GROUP_CONTEXT)){
			// add to the duplicate list to prevent cyclic replication			
			duplicateServiceGrps.put(comMsg.getContextId(),comMsg.getContextId());
			ctxManager.addServiceGroupContext(comMsg.getContextId());
			AxisServiceGroup axisServiceGroup = configContext.getAxisConfiguration().getServiceGroup(comMsg.getAxisDescriptionName());
			ServiceGroupContext ctx = new ServiceGroupContext(configContext,axisServiceGroup);
			ctx.setId(comMsg.getContextId());
			configContext.registerServiceGroupContext(ctx);
			
		}else if(comMsg.getCommandName().equals(CommandConstants.CREATE_SERVICE_CONTEXT)){
			try {
				// add to the duplicate list to prevent cyclic replication			
				duplicateServiceCtxs.put(comMsg.getParentId() + comMsg.getContextId(), comMsg.getContextId());
				ctxManager.addServiceContext(comMsg.getParentId(), comMsg.getContextId());
				AxisService axisService = configContext.getAxisConfiguration().getService(comMsg.getContextId());
				ServiceGroupContext srvGrpCtx = configContext.getServiceGroupContext(comMsg.getParentId(),null);
				// This will create service context if one is not available
				srvGrpCtx.getServiceContext(axisService);
			} catch (AxisFault e) {
				log.error("Unable to find the service " + comMsg, e);
			}
			
		}else if(comMsg.getCommandName().equals(CommandConstants.UPDATE_STATE)){
			ServiceGroupContext srvGrpCtx = configContext.getServiceGroupContext(comMsg.getParentId(),null);
			Map props = ctxManager.getServiceGroupProps(comMsg.getParentId());
			Map tempProps = new HashMap();
			tempProps.putAll(props);
			
			if (props != null){
				System.out.println(props);
				srvGrpCtx.setProperties(tempProps);
			}
			
			try {
				AxisService axisService = configContext.getAxisConfiguration().getService(comMsg.getContextId());
				ServiceContext srvCtx = srvGrpCtx.getServiceContext(axisService);
				Map srvProps = ctxManager.getServiceProps(comMsg.getParentId(),comMsg.getContextId());
				Map tempSrvProps = new HashMap();
				tempSrvProps.putAll(srvProps);
				
				if (props != null){
					srvCtx.setProperties(tempSrvProps);
				}	
			} catch (AxisFault e) {
				log.error("Unable to find the service " + comMsg, e);
			}
			
			
		}else if(comMsg.getCommandName().equals(CommandConstants.UPDATE_STATE_MAP_ENTRY)){
			TribesMapEntryMessage mapEntryMsg = (TribesMapEntryMessage)comMsg;
			if (mapEntryMsg.getCtxType() == TribesMapEntryMessage.SERVICE_GROUP_CONTEXT){
				Map props = ctxManager.getServiceGroupProps(comMsg.getContextId());
				if(mapEntryMsg.getOperation() == TribesMapEntryMessage.ADD_OR_UPDATE_ENTRY){
					props.put(mapEntryMsg.getKey(),mapEntryMsg.getValue());
				}else{
					props.remove(mapEntryMsg.getKey());
				}
			}else if (mapEntryMsg.getCtxType() == TribesMapEntryMessage.SERVICE_GROUP_CONTEXT){
				Map props = ctxManager.getServiceProps(comMsg.getParentId(),comMsg.getContextId());
				if(mapEntryMsg.getOperation() == TribesMapEntryMessage.ADD_OR_UPDATE_ENTRY){
					props.put(mapEntryMsg.getKey(),mapEntryMsg.getValue());
				}else{
					props.remove(mapEntryMsg.getKey());
				}
			}
		}
	}
	
	private void send(TribesCommandMessage msg){
		Member[] group = channel.getMembers();
		log.debug("Group size " + group.length);
        //send the message
		
		for (Member member : group){
			printMember(member);
		}
		
		
        try {
			channel.send(group,msg,0);
		} catch (ChannelException e) {
			log.error("Error sending command message : " + msg,e);
		}
	}

	private void printMember(Member member){
		member.getUniqueId();
		log.debug("\n===============================");
		log.debug("Member Name " + member.getName());
		log.debug("Member Host" + member.getHost());
		log.debug("Member Payload" + member.getPayload());
		log.debug("===============================\n");
	}
}
