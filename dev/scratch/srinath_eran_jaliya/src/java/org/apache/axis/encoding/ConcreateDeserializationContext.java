/*
 * Created on Oct 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.encoding;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaders;
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
public class ConcreateDeserializationContext implements DeseializationContext{
	private OMDocument omdoc;
    private OMElement envelope;
    
	public ConcreateDeserializationContext(InputStream in)throws Exception{
		XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
		pf.setNamespaceAware(true);
		XmlPullParser  parser = pf.newPullParser();
		parser.setInput(new InputStreamReader(in));
		omdoc = new StreamingXPPOMBuilder(parser).getModel().getDocument();
		
	}
	
	public SOAPEnvelope parseEnvelope()throws AxisFault{
        this.envelope = omdoc.getDocumentElement();
        SOAPEnvelope soapEnvelope = new SOAPEnvelope(this.envelope);
        return soapEnvelope;
	}
    
    public SOAPHeaders parseHeaders() throws AxisFault{
        Iterator childeren = this.envelope.getChildren();
        
        while(childeren.hasNext()){
            OMNode node = (OMNode)childeren.next();
            if(node.getType() == OMNode.ELEMENT_NODE){
                return new SOAPHeaders((OMElement)node);
            } 
        }
        
        return null;
    }
    
    public QName enterTheBody(int style) throws AxisFault{
        if(Constants.SOAP_STYLE_RPC_ENCODED == style || style == Constants.SOAP_STYLE_RPC_LITERAL){
            Iterator childeren = this.envelope.getChildren();
            while(childeren.hasNext()){
                OMNode node = (OMNode)childeren.next();

                //TODO
                if(node == null){
                    System.out.println("Why the some nodes are null :( :( :(");
                    continue; 
                }

                if(node.getType() == OMNode.ELEMENT_NODE){
                    OMElement element = (OMElement)node;
                    if(Constants.ELEM_BODY.equals(element.getLocalName())){
                        Iterator bodychilderen = element.getChildren();
                        while(bodychilderen.hasNext()){
                            node = (OMNode)bodychilderen.next();
                            
                            //TODO
                            if(node == null){
                                System.out.println("Why the some nodes are null :( :( :(");
                                continue; 
                            }
                            
                            if(node.getType() == OMNode.ELEMENT_NODE){
                                OMElement bodyChild  = (OMElement)node;
                            
                                OMNamespace omns = bodyChild.getNamespace();

                                if(omns != null){
                                    String ns = omns.getValue();
                                    if(ns != null){
                                        return new QName(ns,bodyChild.getLocalName()); 
                                    }
                                }
                                throw new AxisFault("SOAP Body must be NS Qualified");                
                            }    
                        }
                        
                    }
                } 
            }
        }
        return null;
    }
	
}
