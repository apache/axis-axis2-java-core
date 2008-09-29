package org.apache.axis2.policy.builders;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.policy.model.MTOM10Assertion;
import org.apache.axis2.policy.model.MTOM11Assertion;
import org.apache.axis2.policy.model.MTOMAssertion;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Constants;
import org.apache.neethi.builders.AssertionBuilder;

public class MTOM11AssertionBuilder implements AssertionBuilder{
    
    private static Log log = LogFactory.getLog(MTOM10AssertionBuilder.class);

    public Assertion build(OMElement element, AssertionBuilderFactory factory)
            throws IllegalArgumentException {

        MTOM11Assertion mtomAssertion = new MTOM11Assertion();

        processMTOM11Assertion(element, mtomAssertion);

        return mtomAssertion;
    }

    public QName[] getKnownElements() {
        return new QName[] { new QName(MTOM11Assertion.NS,
                MTOM11Assertion.MTOM_LN) };
    }

    private void processMTOM11Assertion(OMElement element,
            MTOM11Assertion mtomAssertion) {

        // Checking wsp:Optional attribute
        String value = element
                .getAttributeValue(Constants.Q_ELEM_OPTIONAL_ATTR);
        boolean isOptional = JavaUtils.isTrueExplicitly(value);

        mtomAssertion.setOptional(isOptional);

    }

}
