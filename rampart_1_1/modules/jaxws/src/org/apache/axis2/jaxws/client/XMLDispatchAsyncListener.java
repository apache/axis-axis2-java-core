package org.apache.axis2.jaxws.client;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.SOAPEnvelopeBlockFactory;
import org.apache.axis2.jaxws.message.factory.XMLStringBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;

/**
 * The XMLDispatchAsyncListener is an extension of the  
 * {@link org.apache.axis2.jaxws.impl.AsyncListener} class to provide 
 * proper deserialization into the target format (XML String or Source).
 */
public class XMLDispatchAsyncListener extends AsyncListener {

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
    
    protected Object getResponseValueObject(MessageContext mc) {
        Object value = null;

        Message message = mc.getMessage();
        if (mode.equals(Mode.PAYLOAD)) {
            try {
                BlockFactory factory = (BlockFactory) FactoryRegistry.getFactory(blockFactoryType);
                Block block = message.getBodyBlock(0, null, factory);
                value = block.getBusinessObject(true);
            } catch (MessageException e) {
                e.printStackTrace();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        else if (mode.equals(Mode.MESSAGE)) {
            try {
                if (blockFactoryType.equals(SOAPEnvelopeBlockFactory.class)) {
                    // This is an indication that we are in SOAPMessage Dispatch
                    // Return the SOAPMessage
                    value = message.getAsSOAPMessage();
                } 
                else {
                    OMElement messageOM = message.getAsOMElement();
                    QName soapEnvQname = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
        
                    XMLStringBlockFactory stringFactory = (XMLStringBlockFactory) FactoryRegistry.getFactory(XMLStringBlockFactory.class);
                    Block stringBlock = stringFactory.createFrom(messageOM.toString(), null, soapEnvQname);
       
                    BlockFactory factory = (BlockFactory) FactoryRegistry.getFactory(blockFactoryType);
                    Block block = factory.createFrom(stringBlock, null);

                    value = block.getBusinessObject(true);
                }
            } catch (MessageException e) {
                e.printStackTrace();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
        
        return value;
    }
}
