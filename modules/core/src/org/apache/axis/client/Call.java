package org.apache.axis.client;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.EngineContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.Constants;

import java.util.HashMap;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 9, 2005
 * Time: 8:00:08 PM
 */
public class Call {

    private EndpointReference to;
    private EndpointReference from;
    private EndpointReference replyTo;
    private EndpointReference [] relatesTo;

    private HashMap properties;

    private String transport;

    private String SOAPAction;

    private EngineContext engineContext;

    public Call(){
        //find the deployment mechanism , create
        //a EngineContext .. if the conf file not found
        //deafult one is used
        properties = new HashMap();
        this.engineContext = new EngineContext();
    }

    public Call(InputStream in){
        properties = new HashMap();
        this.engineContext = new EngineContext();
    }

    public Call(File inFile) throws AxisFault {
        try {
            InputStream in =new FileInputStream(inFile);
            properties = new HashMap();
            this.engineContext = new EngineContext();
        } catch (FileNotFoundException e) {
            throw new AxisFault("FileNotFound " + e.getMessage());
        }
    }

    public Call(EngineContext engineContext){
        this.properties =new HashMap();
        this.engineContext = engineContext;
    }

    public SOAPEnvelope sendReciveAsync(SOAPEnvelope env,Callback callback) throws AxisFault {
        MessageContext msgctx = new MessageContext(null);     //TODO try to pass old message context
        //todo to complete this MessageContext has to be complete
        return null;
    }

    public SOAPEnvelope sendReciveSync(SOAPEnvelope env) throws AxisFault {
        MessageContext msgctx = new MessageContext(null);     //TODO try to pass old message context
        //todo to complete this MessageContext has to be complete
        return null;

    }

    public EndpointReference getTo() {
        return to;
    }

    public void setTo(EndpointReference to) {
        this.to = to;
    }

    public EndpointReference getFrom() {
        return from;
    }

    public void setFrom(EndpointReference from) {
        this.from = from;
    }

    public EndpointReference getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(EndpointReference replyTo) {
        this.replyTo = replyTo;
    }

    public EndpointReference[] getRelatesTo() {
        return relatesTo;
    }

    public void setRelatesTo(EndpointReference[] relatesTo) {
        this.relatesTo = relatesTo;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) throws AxisFault {
        if ((Constants.TRANSPORT_HTTP.equals(transport)
                || Constants.TRANSPORT_MAIL.equals(transport)
                || Constants.TRANSPORT_TCP.equals(transport))) {
            this.transport = transport;
        } else {
            throw new AxisFault("Selected transport dose not suppot ( " + transport + " )");
        }
    }

    public String getSOAPAction() {
        return SOAPAction;
    }

    public void setSOAPAction(String SOAPAction) throws AxisFault {
        if(SOAPAction.trim() == ""){
            throw new AxisFault("SOAP action can not be an empty value");
        } else {
            this.SOAPAction = SOAPAction;
        }
    }

    public void addProperty(String key , Object value){
        properties.put(key,value);
    }

    public Object getProperty(String key){
        return properties.get(key);
    }

}
