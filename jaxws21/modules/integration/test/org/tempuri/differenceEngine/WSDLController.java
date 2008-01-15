package org.tempuri.differenceEngine;

import org.custommonkey.xmlunit.ComparisonController;
import org.custommonkey.xmlunit.Difference;

public class WSDLController implements ComparisonController {   
    public WSDLController () {
    }

    /**
     * Determine whether a Difference that this listener has been notified of
     *  should halt further XML comparison. This implementation halts 
     *  if the Difference is not recoverable.
     * @param afterDifference the last Difference passed to <code>differenceFound</code>
     * @return false if the difference is recoverable, otherwise return true
     */
    public boolean haltComparison(Difference afterDifference) {
        if (afterDifference.isRecoverable()) {
            return false;
        }
        
        return true;
    }
}
