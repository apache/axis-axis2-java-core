package org.apache.axis.client;

import org.apache.axis.om.SOAPEnvelope;

/**
 * Created by IntelliJ IDEA.
 * Author : Deepal Jayasinghe
 * Date: Apr 9, 2005
 * Time: 8:04:36 PM
 */
public class AsyncResult {
    /**
     *Field result
     */
    private SOAPEnvelope result;

    /**
     * @param result
     */
    public void setResult(SOAPEnvelope result) {
        this.result = result;
    }

    /**
     * @return SOAPEnvelope
     */
    public SOAPEnvelope getResponseEnvelope() {
        return result;
    }
}
