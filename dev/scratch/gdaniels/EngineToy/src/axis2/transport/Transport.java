package axis2.transport;

import axis2.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * A Transport contains sender side and receiver side
 */
public class Transport {
    List sendHandlers = new ArrayList();
    List receiveHandlers = new ArrayList();

    public List getSendHandlers() {
        return sendHandlers;
    }

    public void addSendHandler(Handler handler) {
        sendHandlers.add(handler);
    }

    public List getReceiveHandlers() {
        return receiveHandlers;
    }

    public void addReceiveHandler(Handler handler) {
        receiveHandlers.add(handler);
    }
}
