package org.apache.axis.om.storage;

import org.apache.axis.om.OMTableModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class NodeRow extends Row implements Node {
    protected Object parent;
    protected short nodeType;
    private Object nextSibling;
    protected boolean done;
    protected Document parentDocument;


    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }

    public void setNextSibling(Object nextSibling) {
        this.nextSibling = nextSibling;
    }

    public Node getNextSibling() {

        while ((nextSibling == null) && (!done)) {
           ((OMTableModel)parentDocument).proceedTheParser();
        }

        return (Node) nextSibling;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }


    //Anything here  ????
}


