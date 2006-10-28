/**
 * MTOMServiceSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.1-SNAPSHOT Oct 19, 2006 (07:04:21 LKT)
 */
package sample.mtom.service;

import java.io.File;
import java.io.FileOutputStream;

import javax.activation.DataHandler;

import org.apache.ws.axis2.mtomsample.AttachmentResponse;
import org.apache.ws.axis2.mtomsample.AttachmentType;
import org.w3.www._2005._05.xmlmime.Base64Binary;

/**
 * MTOMServiceSkeleton java skeleton for the axisService
 */
public class MTOMSampleSkeleton {

	/**
	 * Auto generated method signature
	 * 
	 * @param param0
	 * @throws Exception 
	 * 
	 */
	public org.apache.ws.axis2.mtomsample.AttachmentResponse attachment(
			org.apache.ws.axis2.mtomsample.AttachmentRequest param0)
			throws Exception

	{
		AttachmentType attachmentRequest = param0.getAttachmentRequest();
		Base64Binary binaryData = attachmentRequest.getBinaryData();
		DataHandler dataHandler = binaryData.getBase64Binary();
		File file = new File(
				attachmentRequest.getFileName());
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		dataHandler.writeTo(fileOutputStream);
		fileOutputStream.flush();
		fileOutputStream.close();
		
		AttachmentResponse response = new AttachmentResponse();
		response.setAttachmentResponse("File saved succesfully.");
		return response;
	}

}
