package org.apache.axis2.jaxws.client;

import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientUtils {
    
    private static Log log = LogFactory.getLog(ClientUtils.class);    
    
    /**
     * Determines what the SOAPAction value should be for a given MessageContext.  
     * 
     * @param ctx - The MessageContext for the request
     * @return A string with the calculated SOAPAction
     */
    public static String findSOAPAction(MessageContext ctx) {
        OperationDescription op = ctx.getOperationDescription();
        Boolean useSoapAction = (Boolean) ctx.getProperties().get(BindingProvider.SOAPACTION_USE_PROPERTY);
        if(useSoapAction != null && useSoapAction.booleanValue()) {
            // If SOAPAction use hasn't been disabled by the client, then first
            // look in the context properties.
            String action = (String) ctx.getProperties().get(BindingProvider.SOAPACTION_URI_PROPERTY);
            if (action != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting soap action from JAX-WS request context.  Action [" + action + "]");
                }
                return action;
            }
            
            // If we didn't find anything in the context props, then we need to 
            // check the OperationDescrition to see if one was configured in the WSDL.
            if (op != null) {
                action = op.getAction();
                if (action != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Setting soap action from operation description.  Action [" + action + "]");
                    }
                    return action;
                }                
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot set the soap action.  No operation description was found.");
                }
            }
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("Soap action usage was disabled");
            }
        }
        
        return null;
    }

}
