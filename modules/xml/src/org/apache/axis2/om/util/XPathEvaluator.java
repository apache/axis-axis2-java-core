package org.apache.axis2.om.util;

import org.apache.axis2.om.xpath.AXIOMXPath;
import org.jaxen.SimpleNamespaceContext;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ajith
 * Date: Sep 6, 2005
 * Time: 8:02:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class XPathEvaluator {

    public List evaluateXpath(String xpathExpression, Object element, String nsURI) throws Exception{
        AXIOMXPath xpath = new AXIOMXPath(xpathExpression);
        if (nsURI!=null){
            SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
            nsContext.addNamespace(null,nsURI);
            xpath.setNamespaceContext(nsContext);
        }
        return xpath.selectNodes(element);
    }


}
