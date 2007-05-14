
package org.apache.axis2.jaxws.resourceinjection.sei;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;

@WebServiceClient(name = "ResourceInjectionService", targetNamespace = "http://resourceinjection.sample.test.org", wsdlLocation = "resourceinjection.wsdl")
public class ResourceInjectionService
    extends Service
{

    private final static URL RESOURCEINJECTIONSERVICE_WSDL_LOCATION;

    private static String wsdlLocation="/test/org/apache/axis2/jaxws/resourceinjection/META-INF/resourceinjection.wsdl";
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
        	url = file.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        RESOURCEINJECTIONSERVICE_WSDL_LOCATION = url;
    }

    public ResourceInjectionService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ResourceInjectionService() {
        super(RESOURCEINJECTIONSERVICE_WSDL_LOCATION, new QName("http://resourceinjection.sample.test.org", "ResourceInjectionService"));
    }

    /**
     * 
     * @return
     *     returns ResourceInjectionPortType
     */
    @WebEndpoint(name = "ResourceInjectionPort")
    public ResourceInjectionPortType getResourceInjectionPort() {
        return (ResourceInjectionPortType)super.getPort(new QName("http://resourceinjection.sample.test.org", "ResourceInjectionPort"), ResourceInjectionPortType.class);
    }

}
