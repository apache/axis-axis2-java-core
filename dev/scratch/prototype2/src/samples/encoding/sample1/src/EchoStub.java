import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.testUtils.ArrayTypeEncoder;
import org.apache.axis.testUtils.ObjectToOMBuilder;

public class EchoStub {
    private OMFactory fac;
    private OMNamespace ns =
        fac.createOMNamespace("http://apache.ws.apache.org/samples", "samples");
    private OMNamespace arrayNs =
        fac.createOMNamespace(
            OMConstants.ARRAY_ITEM_NSURI,
            OMConstants.ARRAY_ITEM_NS_PREFIX);
    OMNamespace targetNs = fac.createOMNamespace("http://axis.apache.org", "s");
    public EchoStub() {
        fac = OMFactory.newInstance();
    }
    public EchoStruct[] echoEchoStructArray(EchoStruct[] in) throws Exception {
        OMElement returnelement = fac.createOMElement("param1", ns);
        EchoStructEncoder encoder = new EchoStructEncoder();
        ArrayTypeEncoder arrayEncoder = new ArrayTypeEncoder(in, encoder);

        ObjectToOMBuilder builder =
            new ObjectToOMBuilder(returnelement, arrayEncoder);

        returnelement.setBuilder(builder);
        returnelement.declareNamespace(arrayNs);
        returnelement.declareNamespace(targetNs);

        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMElement responseMethodName =
            fac.createOMElement("echoEchoStructArray", ns);
        envelope.getBody().addChild(responseMethodName);
        responseMethodName.addChild(returnelement);

        EndpointReference targetEPR =
            new EndpointReference(
                AddressingConstants.WSA_TO,
                "http://127.0.0.1:8080/axis2/services/echo");
        Call call = new Call();
        call.setTo(targetEPR);
        SOAPEnvelope responseEnv = call.sendReceive(envelope);

        SOAPBody body = responseEnv.getBody();
        if (body.hasFault()) {
            throw body.getFault().getException();
        }
        XMLStreamReader xpp = body.getPullParser(true);

        int event = xpp.next();
        while (event != XMLStreamConstants.START_ELEMENT) {
            event = xpp.next();
        }
        event = xpp.next();
        while (event != XMLStreamConstants.START_ELEMENT) {
            event = xpp.next();
        }
        event = xpp.next();
        while (event != XMLStreamConstants.START_ELEMENT) {
            event = xpp.next();
        }

        Object obj = arrayEncoder.deSerialize(xpp);
        Object[] structs = (Object[]) obj;
        EchoStruct[] response = new EchoStruct[structs.length];
        for (int i = 0; i < structs.length; i++) {
            response[i] = (EchoStruct) structs[i];

        }
        return response;
    }
}
