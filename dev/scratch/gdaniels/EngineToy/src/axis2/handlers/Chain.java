package axis2.handlers;

import axis2.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Chain {
    protected String name;
    protected List handlers = new ArrayList();

    public void addHandler(Handler h) {
        handlers.add(h);
    }

    public List getHandlers() {
        return handlers;
    }
}
