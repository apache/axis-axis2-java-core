/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.context;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.encoding.DeseializationContext;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.engine.Message;
import org.apache.axis.registry.EngineRegistry;
/**
 *  The palce where all the service specific states are kept. 
 *  All the Global states kept in the <code>EngineRegistry</code> and all the 
 *  Service states kept in the <code>MessageContext</code>. Other runtime
 *  artifacts does not keep states foward from the execution.  
 */
public class MessageContext {
    private int messageStyle = Constants.SOAP_STYLE_RPC_ENCODED;
    /**
     * @return Returns the faultOut.
     */
    public SerializationContext getFaultOut() {
        return faultOut;
    }
    /**
     * @param faultOut The faultOut to set.
     */
    public void setFaultOut(SerializationContext faultOut) {
        this.faultOut = faultOut;
    }
    /**
     * @return Returns the sourceIn.
     */
    public DeseializationContext getSourceIn() {
        return sourceIn;
    }
    /**
     * @param sourceIn The sourceIn to set.
     */
    public void setSourceIn(DeseializationContext sourceIn) {
        this.sourceIn = sourceIn;
    }
    /**
     * @return Returns the sourceOut.
     */
    public SerializationContext getSourceOut() {
        return sourceOut;
    }
    /**
     * @param sourceOut The sourceOut to set.
     */
    public void setSourceOut(SerializationContext sourceOut) {
        this.sourceOut = sourceOut;
    }
	public static String USER_NAME = "USER";
	public static String PASSWARD = "PASSWD";
	
	
    public MessageContext(EngineRegistry er){
        globalContext = new GlobalContext(er);
        sessionContext = new SimpleSessionContext();
    }
    private DeseializationContext sourceIn;
    private SerializationContext sourceOut;
    private SerializationContext faultOut;
    
    private boolean processingFault = false;
    private QName currentTansport = null; 
    private QName currentService = null;
    private QName currentOperation = null;
    private HashMap properties = new HashMap();
    private boolean useSOAPAction = true;
    private String soapAction = "";   
    private Message inMessage;
    private Message outMessage;
	/**
	 * @return Returns the soapAction.
	 */
	public String getSoapAction() {
		return soapAction;
	}
	/**
	 * @param soapAction The soapAction to set.
	 */
	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}
	/**
	 * @return Returns the useSOAPAction.
	 */
	public boolean isUseSOAPAction() {
		return useSOAPAction;
	}
	/**
	 * @param useSOAPAction The useSOAPAction to set.
	 */
	public void setUseSOAPAction(boolean useSOAPAction) {
		this.useSOAPAction = useSOAPAction;
	}
    private SessionContext sessionContext;
    private GlobalContext globalContext;
    
    
    public boolean isProcessingFault(){
        return processingFault;
    }
    public void setProcessingFault(boolean processingFault){
        this.processingFault = processingFault;
    }
    
    public void setProperty(String key,String value){
    	properties.put(key,value);
    }
    
    public String getProperty(String key){
    	return (String)properties.get(key);
    }
    /**
     * @return
     */
    public QName getCurrentTansport() {
        return currentTansport;
    }

   
    /**
     * @param name
     */
    public void setCurrentTansport(QName name) {
        currentTansport = name;
    }

    /**
     * @return
     */
    public QName getCurrentOperation() {
        return currentOperation;
    }


    /**
     * @param name
     */
    public void setCurrentOperation(QName name) {
        currentOperation = name;
    }

    /**
     * @param name
     */
    public void setCurrentService(QName name) {
        currentService = name;
    }

    /**
     * @return
     */
    public QName getCurrentService() {
        return currentService;
    }

    /**
     * @return
     */
    public GlobalContext getGlobalContext() {
        return globalContext;
    }

    /**
     * @return
     */
    public SessionContext getSessionContext() {
        return sessionContext;
    }

    /**
     * @param context
     */
    public void setGlobalContext(GlobalContext context) {
        globalContext = context;
    }

    /**
     * @param context
     */
    public void setSessionContext(SessionContext context) {
        sessionContext = context;
    }

	/**
	 * @return Returns the inMessage.
	 */
	public Message getInMessage() {
		return inMessage;
	}
	/**
	 * @param inMessage The inMessage to set.
	 */
	public void setInMessage(Message inMessage) {
		this.inMessage = inMessage;
	}
	/**
	 * @return Returns the outMessage.
	 */
	public Message getOutMessage() {
		return outMessage;
	}
	/**
	 * @param outMessage The outMessage to set.
	 */
	public void setOutMessage(Message outMessage) {
		this.outMessage = outMessage;
	}
    /**
     * @return Returns the messageStyle.
     */
    public int getMessageStyle() {
        return messageStyle;
    }
    /**
     * @param messageStyle The messageStyle to set.
     */
    public void setMessageStyle(int messageStyle) {
        this.messageStyle = messageStyle;
    }
}
