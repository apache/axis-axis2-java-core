package sample.mtom.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.axis2.Constants;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.util.OptionsValidator;

import sample.mtom.service.MTOMSampleStub;
import sample.mtom.service.MTOMSampleStub.AttachmentRequest;
import sample.mtom.service.MTOMSampleStub.AttachmentResponse;
import sample.mtom.service.MTOMSampleStub.AttachmentType;
import sample.mtom.service.MTOMSampleStub.Base64Binary;

public class Client {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CommandLineOptionParser optionsParser = new CommandLineOptionParser(
				args);
		List invalidOptionsList = optionsParser
				.getInvalidOptions(new OptionsValidator() {
					public boolean isInvalid(CommandLineOption option) {
						String optionType = option.getOptionType();
						return !("dest".equalsIgnoreCase(optionType) || "file"
								.equalsIgnoreCase(optionType));
					}
				});

		if ((invalidOptionsList.size() > 0) || (args.length != 4)) {
			// printUsage();
			System.out.println("Invalid Parameters.");
			return;
		}

		Map optionsMap = optionsParser.getAllOptions();

		CommandLineOption fileOption = (CommandLineOption) optionsMap
				.get("file");
		CommandLineOption destinationOption = (CommandLineOption) optionsMap
				.get("dest");
		File file = new File(fileOption.getOptionValue());
		if (file.exists())
			transferFile(file, destinationOption.getOptionValue());
		else
			throw new FileNotFoundException();
	}

	public static void transferFile(File file, String destination)
			throws RemoteException {
		MTOMSampleStub serviceStub = new MTOMSampleStub(
				"http://localhost:8081/axis2/services/MTOMSample");

		// Enable MTOM in the client side
		serviceStub._getServiceClient().getOptions().setProperty(
				Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

		// Uncomment and fill the following if you want to have client side file
		// caching switched ON.
		/*
		 * serviceStub._getServiceClient().getOptions().setProperty(
		 * Constants.Configuration.CACHE_ATTACHMENTS, Constants.VALUE_TRUE);
		 * serviceStub._getServiceClient().getOptions().setProperty(
		 * Constants.Configuration.ATTACHMENT_TEMP_DIR, "your temp dir");
		 * serviceStub._getServiceClient().getOptions().setProperty(
		 * Constants.Configuration.FILE_SIZE_THRESHOLD, "4000");
		 */

		// Populating the code generated beans
		AttachmentRequest attachmentRequest = new AttachmentRequest();
		AttachmentType attachmentType = new AttachmentType();
		Base64Binary base64Binary = new Base64Binary();

		// Creating a javax.activation.FileDataSource from the input file.
		FileDataSource fileDataSource = new FileDataSource(file);
		// Create a dataHandler using the fileDataSource. Any implementation of
		// javax.activation.DataSource interface can fit here.
		DataHandler dataHandler = new DataHandler(fileDataSource);
		base64Binary.setBase64Binary(dataHandler);
		base64Binary.setContentType(dataHandler.getContentType());
		attachmentType.setBinaryData(base64Binary);
		attachmentType.setFileName(destination);
		attachmentRequest.setAttachmentRequest(attachmentType);

		AttachmentResponse response = serviceStub.attachment(attachmentRequest);
		System.out.println(response.getAttachmentResponse());
	}

}
