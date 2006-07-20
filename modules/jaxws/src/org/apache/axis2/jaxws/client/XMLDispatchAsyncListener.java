package org.apache.axis2.jaxws.client;

import javax.xml.ws.Service.Mode;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.param.Parameter;
import org.apache.axis2.jaxws.param.ParameterFactory;
import org.apache.axis2.jaxws.param.ParameterUtils;

/**
 * The XMLDispatchAsyncListener is an extension of the  
 * {@link org.apache.axis2.jaxws.impl.AsyncListener} class to provide 
 * proper deserialization into the target format (XML String or Source).
 */
public class XMLDispatchAsyncListener extends AsyncListener {

    private Mode mode;
    private Class type;
    
    public XMLDispatchAsyncListener() {
        super();
    }
    
    public void setMode(Mode m) {
        mode = m;
    }
    
    public void setType(Class t) {
        type = t;
    }
    
    protected Object getResponseValueObject(MessageContext mc) {
        // FIXME: This is where the Message Model will be integrated instead of 
        // the ParameterFactory/Parameter APIs.
        SOAPEnvelope msg = (SOAPEnvelope) mc.getMessageAsOM();
        
        Parameter param = ParameterFactory.createParameter(type);
        ParameterUtils.fromEnvelope(mode, msg, param);
        return param.getValue();
    }
}
