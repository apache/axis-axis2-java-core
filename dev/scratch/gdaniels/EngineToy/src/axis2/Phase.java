package axis2;

import java.util.ArrayList;
import java.util.List;

/**
 * A Phase is a special kind of Handler which contains other Handlers
 * and has an explicit step where it checks pre- and post-conditions.
 * Subclasses implement checkPreconditions and checkPostconditions in
 * order to do this work, and throw an appropriate Exception if the
 * MessageContext does not indicate a valid state for the phase.
 */
public class Phase {
    protected List handlers = new ArrayList();

    public void checkPreconditions(MessageContext context) throws Exception {
    }

    public void checkPostconditions(MessageContext context) throws Exception {
    }

    public int step(MessageContext context, int start) throws Exception {
        if (start == 0) {
            checkPreconditions(context);
            context.setCurrentPhase(this);
        }

        if (start == handlers.size()) {
            checkPostconditions(context);
            return 0; // finished
        }

        Handler handler = (Handler)handlers.get(start);
        if (handler.invoke(context)) {
            // Success, move on
            start++;
            return start;
        }

        return -1; // pause
    }

    public List getHandlers() {
        return handlers;
    }

    public void setHandlers(List handlers) {
        if (handlers == null)
            throw new NullPointerException();
        this.handlers = handlers;
    }

    public void addHandler(Handler handler) {
        handlers.add(handler);
    }
}
