package org.apache.axis2.interopt.sun.round4.complex;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.util.XMLComparator;
import org.apache.axis2.om.impl.llom.exception.XMLComparisonException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Nadana
 * Date: Aug 8, 2005
 * Time: 8:32:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class EchoBlockingClient {

    public static void sendMsg(SunGroupHClientUtil util, String opName, String fName) {

//        EndpointReference targetEPR = new EndpointReference("http://soapinterop.java.sun.com:80/round4/grouph/complexrpcenc");
        EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:8000/round4/grouph/complexrpcenc");

        try {
            Call call = new Call();
            call.setTo(targetEPR);
            call.setExceptionToBeThrownOnSOAPFault(false);
            call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);

            OMElement firstchile = call.invokeBlocking(opName, util.getEchoOMElement());

            StringWriter writer = new StringWriter();

           Class cls = Object.class;
            InputStream stream = cls.getResourceAsStream(fName);

         //   File file = new File(fName);
            XMLStreamReader parser =
                    XMLInputFactory.newInstance().createXMLStreamReader(stream);
            OMXMLParserWrapper builder = new StAXSOAPModelBuilder(parser, null);
            SOAPEnvelope resEnv = (SOAPEnvelope) builder.getDocumentElement();
            OMElement reselementtobe = (OMElement) resEnv.getBody().getFirstElement();
            XMLComparator comparator = new XMLComparator();
            boolean compare = comparator.compare(firstchile, reselementtobe);
            if (!compare) {
                System.out.println("Error");
            } else
                System.out.println("OK");

            System.out.println(writer.toString());

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (XMLComparisonException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
