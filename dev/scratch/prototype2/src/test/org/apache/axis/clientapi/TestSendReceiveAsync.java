/*
* Created on Dec 22, 2004
*
* TODO To change the template for this generated file go to
* Window - Preferences - Java - Code Style - Code Templates
*/
package org.apache.axis.clientapi;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.SimpleAxisOperationImpl;
import org.apache.axis.engine.Echo;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.EngineUtils;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.providers.RawXMLProvider;
import org.apache.axis.transport.http.SimpleHTTPServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileReader;

/**
 * @author Jaliya
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class TestSendReceiveAsync extends AbstractTestCase {
    private Log log = LogFactory.getLog(getClass());

    private QName serviceName = new QName("", "EchoXMLService");

    private QName operationName = new QName("http://localhost/my", "echoOMElement");

    private QName transportName = new QName("http://localhost/my", "NullTransport");

    private EngineRegistry engineRegistry;

    private MessageContext mc;

    private Thread thisThread;

    private SimpleHTTPServer sas;

    private boolean finish = false;

    /**
     * @param testName
     */
    public TestSendReceiveAsync(String testName) {
        super(testName);
        // TODO Auto-generated constructor stub
    }


    protected void setUp() throws Exception {
        engineRegistry = EngineUtils.createMockRegistry(serviceName, operationName, transportName);

        AxisService service = new AxisService(serviceName);
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.setServiceClass(Echo.class);
        service.setProvider(new RawXMLProvider());
        AxisOperation operation = new SimpleAxisOperationImpl(operationName);

        service.addOperation(operation);

        EngineUtils.createExecutionChains(service);
        engineRegistry.addService(service);

        sas = EngineUtils.startServer(engineRegistry);
    }

    protected void tearDown() throws Exception {

        while (!finish) {
            Thread.sleep(500);
        }
        EngineUtils.stopServer();


    }


    public void testSendReceiveAsync() throws Exception {

        SOAPEnvelope envelope = getBasicEnvelope();
        EndpointReference targetEPR = new EndpointReference(AddressingConstants.WSA_TO, "http://127.0.0.1:" + EngineUtils.TESTING_PORT + "/axis/services/EchoXMLService");
        Call call = new Call();
        call.setTo(targetEPR);
        call.setListenerTransport("http", true);

        Callback callback = new Callback() {
            public void onComplete(AsyncResult result) {

                try {
                    result.getResponseEnvelope().serialize(XMLOutputFactory.newInstance()
                            .createXMLStreamWriter(System.out), true);
                } catch (XMLStreamException e) {
                    reportError(e);


                } finally {
                    finish = true;
                }
            }

            public void reportError(Exception e) {
                e.printStackTrace();
            }
        };
        call.sendReceiveAsync(envelope, callback);

    }

    private SOAPEnvelope getBasicEnvelope() throws Exception {

        SOAPEnvelope envelope = new StAXSOAPModelBuilder(XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(getTestResourceFile("clientapi/SimpleSOAPEnvelope.xml")))).getSOAPEnvelope();

        /*   File file = new File("./target/test-classes/clientapi/SimpleSOAPEnvelope.xml");
           XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file)); //put the file

           OMXMLParserWrapper builder = OMXMLBuilderFactory.createStAXSOAPModelBuilder(OMFactory
                   .newInstance(), xmlStreamReader);
           return (SOAPEnvelope) builder.getDocumentElement(); */
        return envelope;
    }


}
