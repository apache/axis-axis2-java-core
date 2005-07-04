package sample.mtom.client;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.attachments.ImageDataSource;
import org.apache.axis2.attachments.JDK13IO;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.om.*;
import org.apache.axis2.om.impl.llom.OMTextImpl;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;


public class EchoRawMTOMTest {
    private File inputFile = null;

    private EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO,
            "http://127.0.0.1:8080/axis2/services/MyService");


    private QName operationName = new QName("echoOMElement");


    public EchoRawMTOMTest() {

    }

    private OMElement createEnvelope(String fileName) throws Exception {

        DataHandler expectedDH;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");

        OMElement data = fac.createOMElement("mtomSample", omNs);
        OMElement image = fac.createOMElement("image", omNs);
        Image expectedImage;
        expectedImage = new JDK13IO()
                .loadImage(new FileInputStream(inputFile));

        ImageDataSource dataSource = new ImageDataSource("test.jpg",
                expectedImage);
        expectedDH = new DataHandler(dataSource);
        OMText textData = new OMTextImpl(expectedDH, true);
        image.addChild(textData);

        OMElement imageName = fac.createOMElement("fileName", omNs);
        if (fileName != null) {
            imageName.setText(fileName);
        }
        data.addChild(image);
        data.addChild(imageName);

        return data;

    }

    public OMElement testEchoXMLSync(String fileName) throws Exception {

        OMElement payload = createEnvelope(fileName);

        Call call = new Call();
        call.setTo(targetEPR);
        call.set(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        call.setTransportInfo(Constants.TRANSPORT_HTTP,
                Constants.TRANSPORT_HTTP, false);

        OMElement result = (OMElement) call.invokeBlocking(operationName
                .getLocalPart(), payload);

        return result;
    }


    public void setTargetEPR(String targetEPR) {
        this.targetEPR = new EndpointReference(AddressingConstants.WSA_TO,
                targetEPR);

    }


    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }
}
