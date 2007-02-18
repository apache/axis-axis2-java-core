package org.apache.axis2.jaxws.client.dispatch;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

/**
 * The XMLDispatchAsyncListener is an extension of the  
 * {@link org.apache.axis2.jaxws.client.async.AsyncResponse} class to provide 
 * proper deserialization into the target format (XML String or Source).
 */
public class XMLDispatchAsyncListener extends AsyncResponse {

    private Mode mode;
    private Class type;
    private Class blockFactoryType;
    
    public XMLDispatchAsyncListener() {
        super();
    }
    
    public void setMode(Mode m) {
        mode = m;
    }
    
    public void setType(Class t) {
        type = t;
    }
    
    public void setBlockFactoryType(Class t) {
        blockFactoryType = t;
    }
    
    public Object getResponseValueObject(MessageContext mc) {
        return XMLDispatch.getValue(mc.getMessage(), mode, blockFactoryType);
    }
    
    public Throwable getFaultResponse(MessageContext mc) {
        return BaseDispatch.getFaultResponse(mc);
    }
}
