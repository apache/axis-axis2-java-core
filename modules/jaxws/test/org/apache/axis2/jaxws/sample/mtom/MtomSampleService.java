package org.apache.axis2.jaxws.sample.mtom;


import java.awt.Image;
import java.io.ByteArrayInputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axis2.jaxws.provider.DataSourceImpl;
import org.test.mtom.ImageDepot;
import org.test.mtom.ObjectFactory;

@WebService(endpointInterface="org.apache.axis2.jaxws.sample.mtom.MtomSample")
@BindingType(SOAPBinding.SOAP11HTTP_MTOM_BINDING)
public class MtomSampleService implements MtomSample {

    public ImageDepot sendImage(ImageDepot input) {
        System.out.println("[new sendImage request received]");
        DataHandler data = input.getImageData();
        
        System.out.println("[contentType] " + data.getContentType());
        ImageDepot output = (new ObjectFactory()).createImageDepot();
        Image image = null;
        try {
            ByteArrayInputStream stream = (ByteArrayInputStream) data.getContent();
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
        System.out.println("[new sendText request received]");
        return null;
    }

}
