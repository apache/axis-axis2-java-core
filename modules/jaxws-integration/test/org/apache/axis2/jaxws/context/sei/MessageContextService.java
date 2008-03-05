package org.apache.axis2.jaxws.context.sei;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;


@WebServiceClient(name = "MessageContextService", 
                  targetNamespace = "http://context.jaxws.axis2.apache.org/", 
                  wsdlLocation = "MessageContext.wsdl")
public class MessageContextService
        extends Service {

    private final static URL MESSAGECONTEXTSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(org.apache.axis2.jaxws.context.sei.MessageContextService.class.getName());

    private static String wsdlLocation = "/test/org/apache/axis2/jaxws/context/META-INF/MessageContext.wsdl";

    static {
        URL url = null;
        try {
            try {
                String baseDir = new File(System.getProperty("basedir", ".")).getCanonicalPath();
                wsdlLocation = new File(baseDir + wsdlLocation).getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
            File file = new File(wsdlLocation);
            url = file.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        MESSAGECONTEXTSERVICE_WSDL_LOCATION = url;
    }

    public MessageContextService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public MessageContextService() {
        super(MESSAGECONTEXTSERVICE_WSDL_LOCATION, new QName("http://context.jaxws.axis2.apache.org/", "MessageContextService"));
    }

    /**
     * @return returns MessageContext
     */
    @WebEndpoint(name = "MessageContextPort")
    public MessageContext getMessageContextPort() {
        return super.getPort(new QName("http://context.jaxws.axis2.apache.org/", "MessageContextPort"), MessageContext.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return returns MessageContext
     */
    @WebEndpoint(name = "MessageContextPort")
    public MessageContext getMessageContextPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://context.jaxws.axis2.apache.org/", "MessageContextPort"), MessageContext.class, features);
    }

}
