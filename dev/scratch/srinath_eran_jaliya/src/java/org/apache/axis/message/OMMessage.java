/*
 * Created on Oct 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.message;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;

import javax.print.attribute.standard.OutputDeviceAssigned;
import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;

import org.apache.axis.om.OMDocument;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.xpp.StreamingXPPOMBuilder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @author hemapani
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OMMessage {
    private SOAPEnvelope envelope;
    //TODO Binary info if they are there  
    
	public OMMessage(InputStream in)throws Exception{
		XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
		pf.setNamespaceAware(true);
		XmlPullParser  parser = pf.newPullParser();
		parser.setInput(new InputStreamReader(in));
		OMDocument omdoc  = new StreamingXPPOMBuilder(parser).getModel().getDocument();
        envelope = new SOAPEnvelope(omdoc.getDocumentElement());
		
	}
	
    public OMMessage(Object[] obj)throws Exception{
            //TODO create OMMessage 
    }

    public void serialize(OutputStream out){
        
    }
    /**
     * @return Returns the envelope.
     */
    public SOAPEnvelope getEnvelope() {
        return envelope;
    }
    /**
     * @param envelope The envelope to set.
     */
    public void setEnvelope(SOAPEnvelope envelope) {
        this.envelope = envelope;
    }
}
