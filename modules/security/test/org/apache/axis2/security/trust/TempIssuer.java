package org.apache.axis2.security.trust;

import org.apache.axis2.context.MessageContext;
import org.apache.ws.commons.om.OMElement;

public class TempIssuer implements TokenIssuer {

    public OMElement issue(OMElement request, MessageContext msgCtx) throws TrustException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

}
