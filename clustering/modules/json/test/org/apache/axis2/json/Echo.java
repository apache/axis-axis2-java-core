package org.apache.axis2.json;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Echo {
	private static final Log log = LogFactory.getLog(Echo.class);
    public Echo() {
    }
     public OMElement echoOM(OMElement omEle) {
        return omEle;
    }
}
