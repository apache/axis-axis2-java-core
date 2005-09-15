package org.apache.axis2.engine;

/**
 * @author chathura@opensource.lk
 */
public class AxisError extends RuntimeException {

    public AxisError() {

    }

    /**
     * @param message Error message
     */
    public AxisError(String message) {
        super(message);
    }

    /**
     * @param message Error message
     * @param cause   Cause
     */
    public AxisError(String message, Throwable cause) {
        super(message, cause);
    }


}
