package org.apache.axis2.description;

import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

public class RobustOutOnlyAxisOperation extends OutInAxisOperation {
    public RobustOutOnlyAxisOperation() {
        super();
        setMessageExchangePattern(WSDLConstants.MEP_URI_ROBUST_OUT_ONLY);
    }

    public RobustOutOnlyAxisOperation(QName name) {
        super(name);
        setMessageExchangePattern(WSDLConstants.MEP_URI_ROBUST_OUT_ONLY);
    }
}
