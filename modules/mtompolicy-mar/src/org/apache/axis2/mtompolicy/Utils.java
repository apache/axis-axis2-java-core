package org.apache.axis2.mtompolicy;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.policy.model.MTOMAssertion;
import org.apache.axis2.util.PolicyUtil;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

public class Utils {
    
    public static MTOMAssertion getMTOMAssertion (AxisDescription axisDescription) {
        
        if (axisDescription == null) {
            return null;
        }
        
        ArrayList policyList = new ArrayList();
        policyList.addAll(axisDescription.getPolicySubject().getAttachedPolicyComponents());
            
        Policy policy = PolicyUtil.getMergedPolicy(policyList, axisDescription);
        
        if (policy == null) {
            return null;
        }
            
        List<Assertion> list = (List<Assertion>)policy.getAlternatives().next();
            
        for (Assertion assertion : list) {
            if (assertion instanceof MTOMAssertion) {
               return (MTOMAssertion)assertion;
            }
        }
        
        return null;
        
    }

}
