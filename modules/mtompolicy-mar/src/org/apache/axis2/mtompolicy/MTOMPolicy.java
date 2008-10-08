package org.apache.axis2.mtompolicy;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.modules.Module;
import org.apache.axis2.policy.model.MTOMAssertion;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

public class MTOMPolicy implements Module{

    public void applyPolicy(Policy policy, AxisDescription axisDescription)
            throws AxisFault {
        // TODO Auto-generated method stub
        
    }

    public boolean canSupportAssertion(Assertion assertion) {
        
        if (assertion instanceof MTOMAssertion) {
            return true;
        }

        return false;
    }

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
        
        boolean isOptional = false;
            
        MTOMAssertion mtomAssertion = Utils.getMTOMAssertion(axisDescription);

        if (mtomAssertion == null) {
            return;
        }
        
        if ( isOptional) {
            axisDescription.addParameter("enableMTOM", Constants.VALUE_OPTIONAL);
        } else {
            axisDescription.addParameter("enableMTOM", Constants.VALUE_TRUE);
        }
                
                          
    }

    public void init(ConfigurationContext configContext, AxisModule module)
            throws AxisFault {
        // nothing to do here yet
        
    }

    public void shutdown(ConfigurationContext configurationContext)
            throws AxisFault {
        // nothing to do here yet
        
    }

}
