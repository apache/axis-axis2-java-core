package axis2;

import axis2.om.OMElement;

/**
 * Created by IntelliJ IDEA.
 * User: Glen
 * Date: Aug 29, 2004
 * Time: 10:12:18 AM
 * To change this template use File | Settings | File Templates.
 */
public class Message {
    MessageContext context;
    OMElement root;

    public OMElement getContent() {
        return root;
    }

    public void setContent(OMElement root) {
        this.root = root;
    }

    public String toString() {
        if (root != null) {
            return root.getObjectValue().toString();
        }
        return "[Empty Message]";
    }
}
