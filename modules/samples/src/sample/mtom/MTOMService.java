package sample.mtom;

import java.awt.Image;
import java.io.FileOutputStream;

import javax.activation.DataHandler;

import org.apache.axis2.attachments.JDK13IO;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMText;

/**
 * Created by IntelliJ IDEA.
 * User: Jaliya
 * Date: Jun 2, 2005
 * Time: 2:17:58 PM
 */
public class MTOMService {
    public OMElement mtomSample(OMElement element) throws Exception {
        //Praparing the OMElement so that it can be attached to another OM Tree.
        //First the OMElement should be completely build in case it is not fully built and still
        //some of the xml is in the stream.
    	OMElement imageEle = element.getFirstElement();
    	OMElement imageName = (OMElement)imageEle.getNextSibling();
    	OMText binaryNode = (OMText) imageEle.getFirstChild();
    	String nameNode = imageName.getText();
    	DataHandler actualDH;
		actualDH = binaryNode.getDataHandler();
		Image actualObject = new JDK13IO().loadImage(actualDH.getDataSource()
				.getInputStream());
		FileOutputStream imageOutStream = new FileOutputStream(nameNode);
		new JDK13IO().saveImage("image/jpeg", actualObject, imageOutStream);
		OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("urn://fakenamespace","ns");
		OMElement ele =  fac.createOMElement("response",ns);
        ele.setText("Image Saved");
        return ele;
    }
}
