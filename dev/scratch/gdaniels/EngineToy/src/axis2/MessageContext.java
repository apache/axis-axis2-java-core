
package axis2 ;

import axis2.description.Service;
import axis2.handlers.Chain;

import java.util.HashMap;
import java.util.Stack;
import java.util.EmptyStackException;

public class MessageContext {
    /** The transport name */
    String transport;

    Responder responder;
    AxisEngine engine;
    Message message;
    Service service;

    /** The currently executing  */
    ExecutionChain chain;

    Phase currentPhase;
    Handler next;

    HashMap properties = new HashMap();

    public boolean start() throws Exception {
        return chain.invoke(this);
    }
    public boolean resume() throws Exception {
        return chain.invoke(this);
    }

    public void setChain(ExecutionChain c) {
        chain = c;
    }

    public AxisEngine getAxisEngine() {
        return engine;
    }

    public void setAxisEngine(AxisEngine engine) {
        this.engine = engine;
    }

    public Responder getResponder() {
        return responder;
    }

    public void setResponder(Responder responder) {
        this.responder = responder;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public boolean isPropertyTrue(String name) {
        Object val = properties.get(name);
        if (val == null) return false;
        if (val instanceof Boolean) return ((Boolean)val).booleanValue();
        if (val instanceof String) return ((String)val).equalsIgnoreCase("true");
        return false;
    }

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(Phase currentPhase) {
        this.currentPhase = currentPhase;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }
}
