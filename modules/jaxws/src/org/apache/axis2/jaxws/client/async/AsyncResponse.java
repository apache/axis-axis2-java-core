package org.apache.axis2.jaxws.client.async;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.util.ThreadContextMigratorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AsyncResponse extends Callback implements Response {

    private static final Log log = LogFactory.getLog(AsyncResponse.class);
    
    private boolean done;
    private boolean cancelled;
    private Object responseObj;
    private MessageContext response;
    private Map<String, Object> responseContext;
    private Throwable fault;
    
    //-------------------------------------
    // org.apache.axis2.client.async.Callback APIs
    //-------------------------------------
    @Override
    public void onComplete(AsyncResult result) {
        try {
            org.apache.axis2.context.MessageContext axisResponse = result.getResponseMessageContext();
            response = new MessageContext(axisResponse);
            
            //REVIEW: Are we on the final thread of execution here or does this get handed off to the executor?
            // TODO: Remove workaround for WS-Addressing running in thin client (non-server) environment
            try {
                ThreadContextMigratorUtil.performMigrationToThread(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisResponse);
            }
            catch (Throwable t) {
                fault = t;
                
                if (log.isDebugEnabled()) {
                    log.debug("JAX-WS AxisCallback caught throwable from ThreadContextMigratorUtil " + t);
                    log.debug("...caused by " + t.getCause());
                }
                t.printStackTrace();
            }
        } catch (MessageException e) {
            fault = e;
            e.printStackTrace();
        }
        
        done = true;
    }

    @Override
    public void onError(Exception e) {
        fault = e;
    }
    
    //-------------------------------------
    // javax.xml.ws.Response APIs
    //-------------------------------------
    
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public Object get() throws InterruptedException, ExecutionException {
        if (hasFault()) {
            throw new ExecutionException(fault);
        }
        if (response == null) {
            WebServiceException wse = new WebServiceException("null response");
            throw new ExecutionException(wse);
        }
        
        // TODO: Check the type of the object to make sure it corresponds with
        // the parameterized generic type.
        if (responseObj == null) {
            responseObj = getResponseValueObject(response);
        }
        
        return responseObj;
    }

    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isDone() {
        return done;
    }

    public Map getContext() {
        return responseContext;
    }
    
    public boolean hasFault() {
        if (fault != null)
            return true;
        else
            return false;
    }
    
    /**
     * A default implementation of this method that returns the contents
     * of the message in the form of an XML String.  Subclasses should override
     * this to convert the response message into whatever format they require.
     * @param msg
     */
    protected Object getResponseValueObject(MessageContext mc) {
        try {
            Message msg = mc.getMessage();
            OMElement om = msg.getAsOMElement();
            return om.toString();
        } catch (MessageException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }

}
