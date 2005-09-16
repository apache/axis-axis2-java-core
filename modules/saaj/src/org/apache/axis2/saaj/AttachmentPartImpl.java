package org.apache.axis2.saaj;

import org.apache.axis2.om.OMText;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.SessionUtils;

import javax.activation.DataHandler;
import javax.activation.UnsupportedDataTypeException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

public class AttachmentPartImpl extends AttachmentPart {
	
    /**
     * Bulds a new <code>AttachmentPart</code>.
     */
    public AttachmentPartImpl() {
        setMimeHeader(HTTPConstants.HEADER_CONTENT_ID, SessionUtils.generateSessionId());
    }
    
    /**
     * Bulds a new <code>AttachmentPart</code> with a <code>DataHandler</code>.
     *
     * @param dh the <code>DataHandler</code>
     */
    public AttachmentPartImpl(javax.activation.DataHandler dh) {
    	setMimeHeader(HTTPConstants.HEADER_CONTENT_ID,
    			SessionUtils.generateSessionId());
    	dataHandler = dh;
    	if(dh != null) {
    		setMimeHeader(HTTPConstants.HEADER_CONTENT_TYPE, dh.getContentType());
    		omText = org.apache.axis2.om.OMAbstractFactory.getOMFactory().createText(dataHandler, true);
    	}
    }

	public OMText getOMText() throws SOAPException{
		if(omText == null){
			throw new SOAPException("OMText set to null");
		}
		return omText;
	}

	public int getSize() throws SOAPException {
		if (dataHandler == null) {
            return 0;
        }
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
            dataHandler.writeTo(bout);
        } catch (java.io.IOException ex) {
            throw new SOAPException(ex);
        }
        return bout.size();
	}

	public void clearContent() {
		
		dataHandler = null;
		omText = null;
	}

	public Object getContent() throws SOAPException {
		if(dataHandler==null) {
			throw new SOAPException("No content is present in this AttachmentPart");
		}
		try {
			String ContentType = dataHandler.getContentType();
			if(ContentType.equals("text/plain") ||
					ContentType.equals("text/xml") ||
					ContentType.equals("text/html")) {
				//For these content types underlying DataContentHandler surely does 
				//the conversion to appropriate java object and we will return that java object
				return dataHandler.getContent();
			} else {
				try {
					return dataHandler.getContent();
				}catch (UnsupportedDataTypeException e) {
					//If the underlying DataContentHandler can't handle the object contents,
					//we will return an inputstream of raw bytes represneting the content data					
					return dataHandler.getDataSource().getInputStream();
				}
			}
		} catch(Exception e) {
			throw new SOAPException(e);
		}
	}

	public void setContent(Object object, String contentType) {
		
		DataHandler dh = new DataHandler(object, contentType);
		this.setDataHandler(dh);
	}

	public DataHandler getDataHandler() throws SOAPException {
		if (dataHandler==null) {
			throw new SOAPException("No Content present in the Attachment part");
		}
		return dataHandler;
	}

	public void setDataHandler(DataHandler datahandler) {
		
		this.dataHandler = datahandler;
    	if(datahandler != null) {
    		setMimeHeader(HTTPConstants.HEADER_CONTENT_TYPE, datahandler.getContentType());
    		omText = org.apache.axis2.om.OMAbstractFactory.getOMFactory().createText(dataHandler, true);
    	}
	}

	public void removeMimeHeader(String header) {
		
		mimeHeaders.removeHeader(header);
	}

	public void removeAllMimeHeaders() {
		
		mimeHeaders.removeAllHeaders();
	}

	public String[] getMimeHeader(String name) {
		
		return mimeHeaders.getHeader(name);
	}

	public void setMimeHeader(String name, String value) {
		
		mimeHeaders.setHeader(name, value);
	}

	public void addMimeHeader(String name, String value) {
		
		mimeHeaders.addHeader(name, value);
	}

	public Iterator getAllMimeHeaders() {
		
		return mimeHeaders.getAllHeaders();
	}

	public Iterator getMatchingMimeHeaders(String[] names) {
		
		return mimeHeaders.getMatchingHeaders(names);
	}

	public Iterator getNonMatchingMimeHeaders(String[] names) {
		
		return mimeHeaders.getNonMatchingHeaders(names);
	}
	
    public boolean matches(javax.xml.soap.MimeHeaders headers) {
        for (Iterator i = headers.getAllHeaders(); i.hasNext();) {
            javax.xml.soap.MimeHeader hdr = (javax.xml.soap.MimeHeader) i.next();
            String values[] = mimeHeaders.getHeader(hdr.getName());
            boolean found = false;
            if (values != null) {
                for (int j = 0; j < values.length; j++) {
                    if (!hdr.getValue().equalsIgnoreCase(values[j])) {
                        continue;
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
	
    //Should we make it private?
	DataHandler dataHandler;
	
	/**
	 *  Field mimeHeaders.           
	 */
	private MimeHeaders mimeHeaders = new MimeHeaders();
	
	//private Object contentObject;
	
	private OMText omText;

}
