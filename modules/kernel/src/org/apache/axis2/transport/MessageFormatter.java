package org.apache.axis2.transport;

import java.io.OutputStream;
import java.net.URL;

import org.apache.axis2.AxisFault;

public interface MessageFormatter {
	
	public long getContentLength();
	
	public String getContentType();
	
	/**
	 * @return a byte array of the message formatted according to the given message format.
	 */
	public byte[] getBytes() throws AxisFault;
	
	/**
	 * @return this only if you want set a transport header for SOAP Action
	 */
	public String getSOAPAction();
	
	/**
	 * To support deffered writing transports as in http chunking.. 
	 * Axis2 was doing this for some time.. 
	 * TODO: Clarify why it is really needed.
	 * @param out
	 * @param preserve : do not consume the OM when this is set..
	 */
	public void handleOMOutput(OutputStream out, boolean preserve) throws AxisFault;
	
	/**
	 * Some message formats may want to alter the target url.
	 * @return the target URL
	 */
	public URL getTargetAddress();
}
