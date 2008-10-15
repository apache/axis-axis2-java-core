package org.apache.axis2.mtompolicy;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.modules.Module;
import org.apache.axis2.policy.model.MTOMAssertion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

public class MTOMPolicy implements Module {

    /*
     * setup for logging
     */
    private static final Log log = LogFactory.getLog(MTOMPolicy.class);

    public void applyPolicy(Policy policy, AxisDescription axisDescription)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("applyPolicy() called on MTOMPolicy module");
        }

    }

    public boolean canSupportAssertion(Assertion assertion) {

        if (log.isDebugEnabled()) {
            log.debug("canSupportAssertion called on MTOMPolicy module with "
                    + assertion.getName().toString() + " assertion");
        }

        if (assertion instanceof MTOMAssertion) {
            return true;
        }

        return false;
    }

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("MTOMPolicy module has been engaged to "
                    + axisDescription.getClass().getName());
        }

        boolean isOptional = false;

        MTOMAssertion mtomAssertion = Utils.getMTOMAssertion(axisDescription);

        if (mtomAssertion == null) {
            return;
        }

        if (isOptional) {
            axisDescription
                    .addParameter("enableMTOM", Constants.VALUE_OPTIONAL);
        } else {
            axisDescription.addParameter("enableMTOM", Constants.VALUE_TRUE);
        }

    }

    public void init(ConfigurationContext configContext, AxisModule module)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("init() called on MTOMPolicy module");
        }

    }

    public void shutdown(ConfigurationContext configurationContext)
            throws AxisFault {
        if (log.isDebugEnabled()) {
            log.debug("shutdown() called on MTOMPolicy module");
        }

    }

}
