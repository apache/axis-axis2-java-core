package axis2.handlers;

import axis2.Handler;
import axis2.MessageContext;

import java.util.HashMap;

/**
 * BasicHandler, which has boilerplate implementaion.
 */
public abstract class BasicHandler implements Handler {
    protected HashMap options = new HashMap();
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOption(String name, Object value) {
        options.put(name, value);
    }

    public Object getOption(String name) {
        return options.get(name);
    }
}
