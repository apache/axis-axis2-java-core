package org.apache.axis2.transport;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.util.OptionsParser;
import org.apache.axis2.util.OptionsValidator;
import org.apache.axis2.util.CommandLineOptionConstants.Java2WSDLConstants;
import org.apache.axis2.util.CommandLineOptionConstants.WSDL2JavaConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleAxis2Server {

    private static final Log log = LogFactory.getLog(SimpleHTTPServer.class);

    int port = -1;

    public static int DEFAULT_PORT = 8080;
    

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
		String repoLocation = null;
		String confLocation = null;

		CommandLineOptionParser optionsParser = new CommandLineOptionParser(args);
		List invalidOptionsList = optionsParser.getInvalidOptions(new OptionsValidator() {
			public boolean isInvalid(CommandLineOption option) {
				String optionType = option.getOptionType();
				return !("repo".equalsIgnoreCase(optionType) || "conf"
						.equalsIgnoreCase(optionType));
			}
		});
		
		if ((invalidOptionsList.size()>0)||(args.length>4))
		{
			printUsage();
			return;
		}
		
		Map optionsMap = optionsParser.getAllOptions();

		CommandLineOption repoOption = (CommandLineOption) optionsMap
				.get("repo");
		CommandLineOption confOption = (CommandLineOption) optionsMap
				.get("conf");

		System.out.println("[SimpleAxisServer] Starting");
		if (repoOption != null) {
			repoLocation = repoOption.getOptionValue();
			System.out.println("[SimpleAxisServer] Using the Axis2 Repository"
					+ new File(repoLocation).getAbsolutePath());
		}
		if (confOption != null) {
			confLocation = confOption.getOptionValue();
			System.out
					.println("[SimpleAxisServer] Using the Axis2 Configuration File"
							+ new File(confLocation).getAbsolutePath());
		}
		
		try {
			ConfigurationContext configctx = ConfigurationContextFactory
					.createConfigurationContextFromFileSystem(repoLocation,
							confLocation);
			ListenerManager listenerManager =  new ListenerManager();
				listenerManager.init(configctx);
			listenerManager.start();
			System.out.println("[SimpleAxisServer] Started");
		} catch (Throwable t) {
            log.fatal("Error starting SimpleAxisServer", t);
            System.out.println("[SimpleAxisServer] Shutting down");
        }
    }
    
    public static void printUsage() {
        System.out.println("Usage: SimpleAxisServer -repo <repository>  -conf <axis2 configuration file>");
        System.out.println();
        System.exit(1);
    }
}
