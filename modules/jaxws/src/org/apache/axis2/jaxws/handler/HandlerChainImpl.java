package org.apache.axis2.jaxws.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerChain;
import javax.xml.ws.handler.HandlerInfo;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.axis2.jaxws.handler.soap.SOAPMessageContextImpl;
import org.apache.axis2.jaxws.handler.MessageContextImpl;

import org.apache.axis2.jaxws.utils.ClassUtils;

public class HandlerChainImpl extends ArrayList implements HandlerChain {

	private String[] roles;
	
	private int falseIndex = -1;
	
	protected List<HandlerInfo> handlerInfos = new ArrayList<HandlerInfo>();
	
	public static final String JAXRPC_METHOD_INFO = "jaxrpc.method.info";
	
	private HandlerInfo getHandlerInfo(int index){
		return handlerInfos.get(index);
	}
	
	private Handler getHandlerInstance(int index) {
		return (Handler)get(index);
	}
	
	private Handler newHandler(HandlerInfo handlerInfo){
		try{
			Handler handler = (Handler)handlerInfo.getHandlerClass().newInstance();
			handler.init(handlerInfo);
			return handler;
		} catch (Exception ex){
			throw new WebServiceException("No Jax-RPC handler" + handlerInfo.getHandlerClass().toString(), ex);
		}
	}
	
	private void preInvoke(SOAPMessageContext msgContext){
		// EMPTY FUNCTION FOR NOW, MAY USE LATER
	}
	
	private void postInvoke(SOAPMessageContext msgContext) {
		SOAPMessage message = msgContext.getMessage();
		ArrayList oldList =  (ArrayList)msgContext.getProperty(JAXRPC_METHOD_INFO);
		if (oldList != null) {
			if (!Arrays.equals(oldList.toArray(), getMessageInfo(message).toArray())) {
				throw new RuntimeException("invocationArgumentsModified");
			}
		}
	}
	
	public HandlerChainImpl(){
		
	}
	
	public HandlerChainImpl(List<HandlerInfo> handlerInfos){
		this.handlerInfos = handlerInfos;
		for(int i = 0; i < handlerInfos.size(); i++){
			add(newHandler(getHandlerInfo(i)));
		}
	}
	
	public void addNewHandler(String className, Map config){
		try {
			HandlerInfo handlerInfo = new HandlerInfo(ClassUtils.forName(className), config, null);
			handlerInfos.add(handlerInfo);
			add(newHandler(handlerInfo));
		} catch(Exception ex){
			throw new WebServiceException("No Jax-RPC handler"+className, ex);
		}
	}
	
	public ArrayList getMessageInfo(SOAPMessage message) {
		ArrayList list = new ArrayList();
		try {
			if(message == null || message.getSOAPPart() == null)
				return list;
			SOAPEnvelope env = message.getSOAPPart().getEnvelope();
			SOAPBody body = env.getBody();
			java.util.Iterator it = body.getChildElements();
			SOAPElement operation = (SOAPElement)it.next();
			list.add(operation.getElementName().toString());
			for (java.util.Iterator i = operation.getChildElements(); i.hasNext();) {
				SOAPElement elt = (SOAPElement)i.next();
				list.add(elt.getElementName().toString());
			}
		} catch(Exception e){
			
		}
		return list;
	}
	
	public boolean handleRequest(MessageContext _context) throws WebServiceException {
		SOAPMessageContextImpl actx = (SOAPMessageContextImpl)_context;
		actx.setRoles(getRoles());
		SOAPMessageContext context = (SOAPMessageContext)_context;
		preInvoke(context);
		try {
			for (int i = 0; i < size(); i++) {
				Handler currentHandler = getHandlerInstance(i);
				try {
					if (currentHandler.handleRequest(context) == false) {
						falseIndex = i;
						return false;
					}
				}catch (javax.xml.ws.soap.SOAPFaultException sfe) {
					falseIndex = i;
					throw sfe;
				}
			}
			return true;
		}finally {
			postInvoke(context);
		}
	}

	public boolean handleResponse(MessageContext context)
			throws WebServiceException {
		SOAPMessageContextImpl scontext = (SOAPMessageContextImpl)context;
		preInvoke(scontext);
		try {
			int endIdx = size() - 1;
			if (falseIndex != -1) {
				endIdx = falseIndex;
			}
			for (int i = endIdx; i >= 0; i--) {
				if (getHandlerInstance(i).handleResponse(context) == false) {
					return false;
				}
			}
			return true;
		} finally {
			postInvoke(scontext);
		}
	}

	public boolean handleFault(MessageContext _context) throws WebServiceException {
		SOAPMessageContextImpl context = (SOAPMessageContextImpl)_context;
		preInvoke(context);
		try {
			int endIdx = size() - 1;
			if (falseIndex != -1) {
				endIdx = falseIndex;
			}
			for (int i = endIdx; i >= 0; i--) {
				if (getHandlerInstance(i).handleFault(context) == false) {
					return false;
				}
			}
			return true;
		} finally{
			postInvoke(context);
		}
	}

	public void init(Map config) throws WebServiceException {
		// DO SOMETHING WITH THIS
	}

	public void destroy() throws WebServiceException {
		int endIdx = size() - 1;
		if (falseIndex != -1) {
			endIdx = falseIndex;
		}
		for (int i = endIdx; i >= 0; i--) {
			getHandlerInstance(i).destroy();
		}
		falseIndex = -1;
		clear();
	}

	public void setRoles(String[] soapActorNames) {
		if(soapActorNames != null){
			// use clone for cheap array copy
			roles = (String[])soapActorNames.clone();
		}
	}

	public String[] getRoles() {
		return roles;
	}

}
