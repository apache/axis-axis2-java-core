package axis2;

/**
 * Created by IntelliJ IDEA.
 * User: Glen
 * Date: Oct 19, 2004
 * Time: 1:11:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExecutionFrame {
    Handler hander;
    Object context;

    public ExecutionFrame(Handler hander, Object context) {
        this.hander = hander;
        this.context = context;
    }

    public ExecutionFrame() {
    }

    public Handler getHander() {
        return hander;
    }

    public void setHander(Handler hander) {
        this.hander = hander;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }
}
