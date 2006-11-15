package sample.soapwithattachments.service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.activation.DataHandler;

import org.apache.axiom.attachments.Attachments;
import org.apache.axis2.context.MessageContext;

public class AttachmentService {

	public String uploadFile(String name, String attchmentID) throws IOException
	{
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        Attachments attachment = msgCtx.getAttachmentMap();
        DataHandler dataHandler = attachment.getDataHandler(attchmentID);
        File file = new File(
				name);
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		dataHandler.writeTo(fileOutputStream);
		fileOutputStream.flush();
		fileOutputStream.close();
		
		return "File saved succesfully.";
	}

}
