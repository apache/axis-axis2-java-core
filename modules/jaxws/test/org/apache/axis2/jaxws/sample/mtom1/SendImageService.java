
package org.apache.axis2.jaxws.sample.mtom1;

import org.apache.axis2.jaxws.TestLogger;

import javax.jws.WebService;
import java.rmi.RemoteException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;


/**
 * A JAXB implementation
 *
 */

@WebService(
		targetNamespace = "urn://mtom1.sample.jaxws.axis2.apache.org",
		serviceName = "SendImageService",
		portName = "sendImageSoap",		
		wsdlLocation = "META-INF/samplemtomjpeg.wsdl",
		endpointInterface = "org.apache.axis2.jaxws.sample.mtom1.SendImageInterface")
@BindingType (SOAPBinding.SOAP11HTTP_MTOM_BINDING)	
public class SendImageService implements SendImageInterface {
 
	
    /**
     * Required impl method from JAXB interface
     * 
     * - No MTOM setting via @BindingType
     * - Using PAYLOAD mode
     * - Sending back the same obj it received
     * - The JAXB object is for image/jpeg MIME type
     *
     * @param ImageDepot obj
     * @return ImageDepot obj
     */
    public ImageDepot invoke(ImageDepot request) throws WebServiceException
    {
        TestLogger.logger.debug("--------------------------------------");
        TestLogger.logger.debug("SendImageService");

       if (request == null) {
           throw new WebServiceException("Null input received.");
       } else if (request.getImageData() == null) {
           throw new WebServiceException("Image is null");
       }

        TestLogger.logger.debug("SendImageService: Request received.");
       return request;
    }
}
