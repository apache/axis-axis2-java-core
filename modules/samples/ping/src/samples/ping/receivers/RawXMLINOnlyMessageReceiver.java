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

public class RawXMLINOnlyMessageReceiver extends AbstractInMessageReceiver
        implements MessageReceiver, Pingable {

    private Method findOperation(AxisOperation op, Class implClass) {
        String methodName = op.getName().getLocalPart();
        Method[] methods = implClass.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName) &&
                    methods[i].getParameterTypes().length == 1 &&
                    OMElement.class.getName().equals(
                            methods[i].getParameterTypes()[0].getName()) &&
                    "void".equals(methods[i].getReturnType().getName())) {
                return methods[i];
            }
        }

        return null;
    }

    /**
     * Invokes the bussiness logic invocation on the service implementation class
     *
     * @param msgContext the incoming message context
     * @throws org.apache.axis2.AxisFault on invalid method (wrong signature)
     */
    public void invokeBusinessLogic(MessageContext msgContext) throws AxisFault {
        try {

            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);

            // find the WebService method
            Class implClass = obj.getClass();

            AxisOperation op = msgContext.getAxisOperation();
            Method method = findOperation(op, implClass);

            if (method != null) {
                method.invoke(
                        obj, new Object[]{msgContext.getEnvelope().getBody().getFirstElement()});
            } else {
                throw new AxisFault(Messages.getMessage("methodDoesNotExistInOnly"));
            }

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public int ping() throws AxisFault {
        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(MessageContext.getCurrentMessageContext());

        if(obj instanceof Pingable){
            return ((Pingable)obj).ping();
        }
        return PING_MR_LEVEL;
    }
}
