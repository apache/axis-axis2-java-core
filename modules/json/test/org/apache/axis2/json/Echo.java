package org.apache.axis2.json;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Echo {
	private static final Log log = LogFactory.getLog(Echo.class);
    public Echo() {
    }
     public OMElement echoOM(OMElement omEle) throws AxisFault {
    	 Object object = MessageContext.getCurrentMessageContext().getProperty(Constants.Configuration.MESSAGE_TYPE);
    	 String messageType =(String)object;
    	 if (messageType.indexOf("json")<0)
    	 {
    		 throw new AxisFault("Type of the Received Message is not JSON");
    	 }
    	 return omEle;
    }
}
