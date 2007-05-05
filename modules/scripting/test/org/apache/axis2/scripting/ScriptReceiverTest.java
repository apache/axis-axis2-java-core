package org.apache.axis2.scripting;

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;

public class ScriptReceiverTest extends TestCase {
    
    public void testInvokeBusinessLogic() throws AxisFault {
        ScriptReceiver scriptReceiver = new ScriptReceiver();
        MessageContext inMC = TestUtils.createMockMessageContext("<a>petra</a>");
        AxisService axisService = inMC.getAxisService();
        axisService.addParameter(new Parameter(ScriptReceiver.SCRIPT_ATTR, "foo.js"));
        axisService.addParameter(new Parameter(ScriptReceiver.SCRIPT_SRC_PROP,
                                               "function invoke(inMC,outMC) " +
                                                       "{outMC.setPayloadXML(<a>petra</a>) }"));
        inMC.setAxisService(axisService);
        scriptReceiver.invokeBusinessLogic(inMC, inMC);
        Iterator iterator = inMC.getEnvelope().getChildElements();
        iterator.next();
        assertEquals("<a>petra</a>", ((OMElement) iterator.next()).getFirstElement().toString());
    }

    public void testAxisService() throws AxisFault {
        ScriptReceiver scriptReceiver = new ScriptReceiver();
        MessageContext inMC = TestUtils.createMockMessageContext("<a>petra</a>");
        AxisService axisService = inMC.getAxisService();
        axisService.addParameter(new Parameter(ScriptReceiver.SCRIPT_ATTR, "foo.js"));
        axisService.addParameter(ScriptReceiver.SCRIPT_SRC_PROP,
                                "var scope = _AxisService.getScope();function invoke(inMC,outMC) " +
                                        "{outMC.setPayloadXML(<a>{scope}</a>) }");
        scriptReceiver.invokeBusinessLogic(inMC, inMC);
        Iterator iterator = inMC.getEnvelope().getChildElements();
        iterator.next();
        assertEquals("<a>request</a>", ((OMElement) iterator.next()).getFirstElement().toString());
    }

}
