package org.apache.axis2.scripting;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

public class ScriptReceiverTest extends TestCase {
    
    public void testInvokeBusinessLogic() throws AxisFault {
        ScriptReceiver scriptReceiver = new ScriptReceiver();
        MessageContext inMC = TestUtils.createMockMessageContext("<a>petra</a>");
        AxisService axisServce = new AxisService();
        axisServce.setParent(new AxisServiceGroup(new AxisConfiguration()));
        axisServce.addParameter(new Parameter(ScriptReceiver.SCRIPT_ATTR, "foo.js"));
        axisServce.addParameter(new Parameter(ScriptReceiver.SCRIPT_SRC_PROP, "function invoke(inMC,outMC) {outMC.setPayloadXML(<a>petra</a>) }"));
        inMC.setAxisService(axisServce);
        scriptReceiver.invokeBusinessLogic(inMC, inMC);
        Iterator iterator = inMC.getEnvelope().getChildElements();
        iterator.next();
        assertEquals("<a>petra</a>", ((OMElement) iterator.next()).getFirstElement().toString());
    }

    public void testAxisService() throws AxisFault {
        ScriptReceiver scriptReceiver = new ScriptReceiver();
        MessageContext inMC = TestUtils.createMockMessageContext("<a>petra</a>");
        AxisService axisServce = new AxisService();
        axisServce.setParent(new AxisServiceGroup(new AxisConfiguration()));
        axisServce.addParameter(new Parameter(ScriptReceiver.SCRIPT_ATTR, "foo.js"));
        axisServce.addParameter(new Parameter(ScriptReceiver.SCRIPT_SRC_PROP, "var scope = _AxisService.getScope();function invoke(inMC,outMC) {outMC.setPayloadXML(<a>{scope}</a>) }"));
        inMC.setAxisService(axisServce);
        scriptReceiver.invokeBusinessLogic(inMC, inMC);
        Iterator iterator = inMC.getEnvelope().getChildElements();
        iterator.next();
        assertEquals("<a>request</a>", ((OMElement) iterator.next()).getFirstElement().toString());
    }

}
