package axis2;

import java.util.Map;

/**
 * HandlerDescriptor
 */
public class HandlerDescriptor {
    String handlerName;
    Map options;

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public Map getOptions() {
        return options;
    }

    public void setOptions(Map options) {
        this.options = options;
    }
}
