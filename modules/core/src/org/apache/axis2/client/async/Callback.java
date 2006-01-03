package org.apache.axis2.client.async;

/**
 * This Class is the abstract representation of a callback and is called at the completion of an
 * asynchronous invocation.
 */
public abstract class Callback {

    /**
     * Field complete
     */
    private boolean complete = false;

    /**
     * This method is invoked by Axis Engine once the asynchronous operation has completed sucessfully.
     *
     * @param result
     */
    public abstract void onComplete(AsyncResult result);

    /**
     * This method is called by Axis Engine if the asynchronous operation fails.
     *
     * @param e
     */
    public abstract void onError(Exception e);

    /**
     * Returns true if the asynchronous operation has completed, false otherwise. Typically this is
     * used for polling. e.g.
     * <code>
     * <pre>
     *          while(!callback.isComplete()){
     *             Thread.sleep(1000);
     *          }
     *          do whatever u need to do
     *      </pre>
     * </code>
     *
     * @return boolean
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
}
