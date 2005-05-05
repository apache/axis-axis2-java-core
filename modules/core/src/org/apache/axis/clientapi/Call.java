package org.apache.axis.clientapi;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.miheaders.RelatesTo;
import org.apache.axis.addressing.om.MessageInformationHeadersCollection;
import org.apache.axis.context.BasicOperationContext;
import org.apache.axis.context.EngineContext;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.AxisGlobal;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.AxisTransportIn;
import org.apache.axis.description.AxisTransportOut;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineConfiguration;
import org.apache.axis.engine.EngineConfigurationImpl;
import org.apache.axis.engine.MessageSender;
import org.apache.axis.om.OMException;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.transport.TransportReceiver;
import org.apache.axis.transport.TransportSender;
import org.apache.wsdl.WSDLDescription;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 9, 2005
 * Time: 8:00:08 PM
 */
public class Call {

    private MessageInformationHeadersCollection messageInfoHeaders;

    private HashMap properties;

    private String senderTransport = Constants.TRANSPORT_HTTP;
    private String Listenertransport = Constants.TRANSPORT_HTTP;

    private EngineContext engineContext;

    private boolean useSeparateListener = false;

    private AxisService callbackService;
    private CallbackReceiver callbackReceiver;
    private AxisOperation axisOperation;
    private ListenerManager listenerManager;

    public Call() throws AxisFault {
        this(new EngineContext(new EngineConfigurationImpl(new AxisGlobal())));
        try {
            //find the deployment mechanism , create
            //a EngineContext .. if the conf file not found
            //deafult one is used
            properties = new HashMap();
            EngineConfiguration registry = engineContext.getEngineConfig();

            //This is a hack, initialize the transports for the client side 
            AxisTransportOut httpTransportOut =
                new AxisTransportOut(new QName(Constants.TRANSPORT_HTTP));
            Class className = Class.forName("org.apache.axis.transport.http.HTTPTransportSender");
            httpTransportOut.setSender((TransportSender) className.newInstance());
            registry.addTransportOut(httpTransportOut);

            AxisTransportIn axisTr = new AxisTransportIn(new QName(Constants.TRANSPORT_HTTP));
            className = Class.forName("org.apache.axis.transport.http.HTTPTransportReceiver");
            axisTr.setReciver((TransportReceiver) className.newInstance());
            registry.addTransportIn(axisTr);

            AxisTransportOut mailTransportOut =
                new AxisTransportOut(new QName(Constants.TRANSPORT_MAIL));
            className = Class.forName("org.apache.axis.transport.mail.MailTransportSender");
            mailTransportOut.setSender((TransportSender) className.newInstance());
            registry.addTransportIn(new AxisTransportIn(new QName(Constants.TRANSPORT_MAIL)));
            registry.addTransportOut(mailTransportOut);

            messageInfoHeaders = new MessageInformationHeadersCollection();
        } catch (ClassNotFoundException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new AxisFault(e.getMessage(), e);
        }
    }

    public Call(WSDLDescription wsdlDesc, EngineContext engineContext) {
        messageInfoHeaders = new MessageInformationHeadersCollection();
        this.properties = new HashMap();
        this.engineContext = engineContext;
        callbackReceiver = new CallbackReceiver();
        listenerManager = new ListenerManager(engineContext);
        if (wsdlDesc != null) {

        }
    }

    public Call(EngineContext engineContext) {
        this(null, engineContext);
    }

    public void sendReceiveAsync(SOAPEnvelope env, final Callback callback) throws AxisFault {
        initializeOperation();

        EngineConfiguration registry = engineContext.getEngineConfig();
        if (Constants.TRANSPORT_MAIL.equals(senderTransport)) {
            throw new AxisFault("This invocation support only for bi-directional transport");
        }
        try {
            MessageSender sender = new MessageSender(engineContext);

            final AxisTransportIn transportIn = registry.getTransportIn(new QName(senderTransport));
            final AxisTransportOut transportOut =
                registry.getTransportOut(new QName(senderTransport));

            final MessageContext msgctx =
                new MessageContext(
                    engineContext,
                    properties,
                    null,
                    transportIn,
                    transportOut);
                    

            msgctx.setEnvelope(env);

            if (useSeparateListener) {
                messageInfoHeaders.setMessageId(String.valueOf(System.currentTimeMillis()));
                callbackReceiver.addCallback(messageInfoHeaders.getMessageId(), callback);
                messageInfoHeaders.setReplyTo(
                listenerManager.replyToEPR(
                        callbackService.getName().getLocalPart()
                            + "/"
                            + axisOperation.getName().getLocalPart()));
                axisOperation.findMEPContext(msgctx, false);
            }

            msgctx.setMessageInformationHeaders(messageInfoHeaders);

            sender.send(msgctx);

            //TODO start the server
            if (!useSeparateListener) {
                Runnable newThread = new Runnable() {
                    public void run() {
                        try {
                            MessageContext response = new MessageContext(msgctx);
                            response.setServerSide(false);

                            TransportReceiver receiver = response.getTransportIn().getReciever();
                            receiver.invoke(response);
                            SOAPEnvelope resenvelope = response.getEnvelope();
                            AsyncResult asyncResult = new AsyncResult();
                            asyncResult.setResult(resenvelope);
                            callback.onComplete(asyncResult);
                        } catch (AxisFault e) {
                            callback.reportError(e);
                        }

                    }
                };
                (new Thread(newThread)).start();
            }

        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
    }

    public SOAPEnvelope sendReceiveSync(SOAPEnvelope env) throws AxisFault {
        initializeOperation();

        EngineConfiguration registry = engineContext.getEngineConfig();
        if (Constants.TRANSPORT_MAIL.equals(senderTransport)) {
            throw new AxisFault("This invocation support only for bi-directional transport");
        }
        try {
            MessageSender sender = new MessageSender(engineContext);

            AxisTransportIn transportIn = registry.getTransportIn(new QName(senderTransport));
            AxisTransportOut transportOut = registry.getTransportOut(new QName(senderTransport));

            MessageContext msgctx =
                new MessageContext(
                    engineContext,
                    properties,
                    null,
                    transportIn,
                    transportOut,
                    new BasicOperationContext(axisOperation, null));
            msgctx.setEnvelope(env);
            msgctx.setMessageInformationHeaders(messageInfoHeaders);

            sender.send(msgctx);

            MessageContext response = new MessageContext(msgctx);
            response.setServerSide(false);

            TransportReceiver receiver = response.getTransportIn().getReciever();
            receiver.invoke(response);
            SOAPEnvelope resenvelope = response.getEnvelope();

            // TODO if the resenvelope is a SOAPFault then throw an exception
            return resenvelope;
        } catch (OMException e) {
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
    }

    public void setTransport(String transport) throws AxisFault {
        if ((Constants.TRANSPORT_HTTP.equals(transport)
            || Constants.TRANSPORT_MAIL.equals(transport)
            || Constants.TRANSPORT_TCP.equals(transport))) {
            this.senderTransport = transport;
        } else {
            throw new AxisFault("Selected transport dose not suppot ( " + transport + " )");
        }
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void close() {
        listenerManager.stopAServer();
    }

 

    /**
     * @param action
     */
    public void setAction(String action) {
        messageInfoHeaders.setAction(action);
    }

    /**
     * @param faultTo
     */
    public void setFaultTo(EndpointReference faultTo) {
        messageInfoHeaders.setFaultTo(faultTo);
    }

    /**
     * @param from
     */
    public void setFrom(EndpointReference from) {
        messageInfoHeaders.setFrom(from);
    }

    /**
     * @param messageId
     */
    public void setMessageId(String messageId) {
        messageInfoHeaders.setMessageId(messageId);
    }

    /**
     * @param relatesTo
     */

    public void setRelatesTo(RelatesTo relatesTo) {
        messageInfoHeaders.setRelatesTo(relatesTo);
    }

    /**
     * @param replyTo
     */
    public void setReplyTo(EndpointReference replyTo) {
        messageInfoHeaders.setReplyTo(replyTo);
    }

    /**
     * @param to
     */
    public void setTo(EndpointReference to) {
        messageInfoHeaders.setTo(to);
    }

    /**
     * todo
     * inoder to have asyn support for tansport , it shoud call this method
     *
     * @param Listenertransport
     * @param useSeparateListener
     * @throws AxisFault
     */
    public void setListenerTransport(String Listenertransport, boolean useSeparateListener)
        throws AxisFault {
        if ((Constants.TRANSPORT_HTTP.equals(Listenertransport)
            || Constants.TRANSPORT_MAIL.equals(Listenertransport)
            || Constants.TRANSPORT_TCP.equals(Listenertransport))) {
            this.Listenertransport = Listenertransport;
            this.useSeparateListener = useSeparateListener;
        } else {
            throw new AxisFault("Selected transport dose not suppot ( " + senderTransport + " )");
        }
    }

    /**
     * @param name
     */
    public void setOperationName(QName name) {
        axisOperation = new AxisOperation(name);
        messageInfoHeaders.setAction(axisOperation.getName().getLocalPart());
    }
    
    private void initializeOperation() throws AxisFault{
        if (axisOperation == null) {
             throw new AxisFault("Operation Name must be specified");
         } 
         
         if(callbackService == null){
             callbackService = new AxisService(new QName("CallBackService"));
         } 
        callbackService.addOperation(axisOperation);
        axisOperation.setMessageReciever(callbackReceiver);
        listenerManager.makeSureStarted();
        listenerManager.getEngineContext().addService(new ServiceContext(callbackService, null));
    }
}
