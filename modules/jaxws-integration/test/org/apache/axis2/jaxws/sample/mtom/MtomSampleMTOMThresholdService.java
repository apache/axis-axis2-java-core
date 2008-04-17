package org.apache.axis2.jaxws.sample.mtom;

import java.awt.Image;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.jws.WebService;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;

import org.apache.axis2.datasource.jaxb.JAXBAttachmentUnmarshallerMonitor;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.provider.DataSourceImpl;
import org.test.mtom.ImageDepot;
import org.test.mtom.ObjectFactory;
/**
 * Endpoint with MTOM enabled and Threshold set to size bigger than the attachment size.
 * The response from Server should have attachments inlined.
 */
@WebService(serviceName="MtomSampleService",
	    endpointInterface="org.apache.axis2.jaxws.sample.mtom.MtomSample")
@MTOM(enabled=true, threshold=99000)
public class MtomSampleMTOMThresholdService implements MtomSample {

    public ImageDepot sendImage(ImageDepot input) {
        TestLogger.logger.debug("MtomSampleMTOMEnableService [new sendImage request received]");
        DataHandler data = input.getImageData();

        TestLogger.logger.debug("[contentType] " + data.getContentType());
        ImageDepot output = (new ObjectFactory()).createImageDepot();
        Image image = null;
        
        resetAttachmentUnmarshallingMonitor();
        try {
            InputStream stream = (InputStream) data.getContent();
            image = ImageIO.read(stream);
            
            DataSource imageDS = new DataSourceImpl("image/jpeg", "test.jpg", image);
            DataHandler handler = new DataHandler(imageDS);
            output.setImageData(handler);
        }
        catch (Exception e) {
            throw new WebServiceException(e);
        }
        return output;
    }

    public ImageDepot sendText(byte[] input) {
        TestLogger.logger.debug("[new sendText request received]");
        return null;
    }

    /**
     * Reset the monitor so that we can determine if an
     * attachment is unmarshalled on the response.
     */
    private void resetAttachmentUnmarshallingMonitor() {
        if (JAXBAttachmentUnmarshallerMonitor.isMonitoring()) {
            JAXBAttachmentUnmarshallerMonitor.clear();
        }
    }
}
