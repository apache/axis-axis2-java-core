package samples.ping.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.Pingable;
import org.apache.axis2.i18n.Messages;

import java.lang.reflect.Method;

public class RawXMLINOnlyMessageReceiver extends org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver
        implements MessageReceiver, Pingable {

    public int ping() throws AxisFault {
        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(MessageContext.getCurrentMessageContext());

        if(obj instanceof Pingable){
            return ((Pingable)obj).ping();
        }
        return PING_MR_LEVEL;
    }
}
