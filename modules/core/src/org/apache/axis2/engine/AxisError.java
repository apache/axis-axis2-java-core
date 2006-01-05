package org.apache.axis2.engine;

public class AxisError extends RuntimeException {

	private static final long serialVersionUID = 6291062136407995920L;

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
