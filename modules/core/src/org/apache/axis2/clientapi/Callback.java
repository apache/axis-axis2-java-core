package org.apache.axis2.clientapi;

import org.apache.axis2.soap.SOAPEnvelope;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 9, 2005
 * Time: 8:03:41 PM
 */
public abstract class Callback {
    /**
     * Field complete
     */
    private boolean complete = false;

    /**
     * Field result
     */
    private SOAPEnvelope result;

    /**
     * Method onComplete
     *
     * @param result
     */
    public abstract void onComplete(AsyncResult result);

    /**
     * Method reportError
     *
     * @param e
     */
    public abstract void reportError(Exception e);

    /**
     * Method isComplete
     *
     * @return
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Method setComplete
     *
     * @param complete
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * Method getResult
     *
     * @return
     */
    public SOAPEnvelope getResult() {
        return result;
    }

    /**
     * Method setResult
     *
     * @param result
     */
    public void setResult(SOAPEnvelope result) {
        this.result = result;
    }
}
