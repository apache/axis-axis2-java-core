//
// Generated By:JAX-WS RI IBM 2.1.1 in JDK 6 (JAXB RI IBM JAXB 2.1.3 in JDK 1.6)
//


package org.apache.axis2.jaxws.jaxb.string;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebEndpoint;
import jakarta.xml.ws.WebServiceClient;
import jakarta.xml.ws.WebServiceFeature;

@WebServiceClient(name = "JAXBStringService", targetNamespace = "http://string.jaxb.jaxws.axis2.apache.org")
public class JAXBStringService
    extends Service
{

    private final static URL JAXBSTRINGSERVICE_WSDL_LOCATION;

    private static String wsdlLocation="/src/test/java/org/apache/axis2/jaxws/jaxb/string/META-INF/echostring.wsdl";
    static {
        URL url = null;
        try {
                try{
                        String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
                        wsdlLocation = new File(baseDir + wsdlLocation).getAbsolutePath();
                }catch(Exception e){
                        e.printStackTrace();
                }
                File file = new File(wsdlLocation);
                url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        JAXBSTRINGSERVICE_WSDL_LOCATION = url;
    }

    public JAXBStringService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public JAXBStringService() {
        super(JAXBSTRINGSERVICE_WSDL_LOCATION, new QName("http://string.jaxb.jaxws.axis2.apache.org", "JAXBStringService"));
    }

    /**
     * 
     * @return
     *     returns JAXBStringPortType
     */
    @WebEndpoint(name = "JAXBStringPort")
    public JAXBStringPortType getJAXBStringPort() {
        return (JAXBStringPortType)super.getPort(new QName("http://string.jaxb.jaxws.axis2.apache.org", "JAXBStringPort"), JAXBStringPortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link jakarta.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns JAXBStringPortType
     */
    @WebEndpoint(name = "JAXBStringPort")
    public JAXBStringPortType getJAXBStringPort(WebServiceFeature... features) {
        return (JAXBStringPortType)super.getPort(new QName("http://string.jaxb.jaxws.axis2.apache.org", "JAXBStringPort"), JAXBStringPortType.class, features);
    }

}
