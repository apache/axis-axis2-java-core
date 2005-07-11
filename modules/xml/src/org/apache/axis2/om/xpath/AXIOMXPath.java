package org.apache.axis2.om.xpath;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;

public class AXIOMXPath extends BaseXPath {
    /**
     * Construct given an XPath expression string.
     *
     * @param xpathExpr the XPath expression.
     * @throws org.jaxen.JaxenException if there is a syntax error while
     *                                  parsing the expression
     */
    public AXIOMXPath(String xpathExpr) throws JaxenException {
        super(xpathExpr, new DocumentNavigator());
    }
}
