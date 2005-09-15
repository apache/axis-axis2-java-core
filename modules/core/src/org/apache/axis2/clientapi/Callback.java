package org.apache.axis2.clientapi;


/**
 * This Class is the abstract representation of the Callback that would be called in the completion of a 
 * Async invocation
 */
public abstract class Callback {
    /**
     * Field complete
     */
    private boolean complete = false;


    /**
     * This Method is called by Axis2 once the Async Operation is sucessfully completed and the result returns
     *
     * @param result
     */
    public abstract void onComplete(AsyncResult result);

    /**
     * This Method is called by Axis2 once the Async Operation fails and the result returns
     *
     * @param e
     */
    public abstract void reportError(Exception e);

    /**
     * This says has the Async Operation is completed or not. this could be useful for poleing 
     * with a special callback written for poleing (checking repeatedly time to time).
     * e.g.
     * <code>
     *      <pre>
     *          while(!callback.isComplete()){
     *             Thread.sleep(1000);
     *          }
     *          do whatever u need to do
     *      </pre>
     * </code>
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
}
