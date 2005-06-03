package sample.google.spellcheck;

import org.apache.axis.soap.SOAPFactory;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMElement;
import org.apache.axis.clientapi.AsyncResult;
import org.apache.axis.clientapi.Call;
import org.apache.axis.clientapi.Callback;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.AddressingConstants;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.net.URL;
import java.net.MalformedURLException;

/**
 *  class sample.google.spellcheck.FormModel
 * This is the Impementation of the Asynchronous Client
 * @author Nadana Gunarathna
 *
 */
public class FormModel extends Callback {

    Observer observer;
    public FormModel(Observer observer)
    {
        this.observer = observer;
    }

    private OMElement getElement(String word){
        SOAPFactory omfactory=OMAbstractFactory.getSOAP11Factory();
        OMNamespace opN = omfactory.createOMNamespace("urn:GoogleSearch","ns1");
        OMNamespace emptyNs=omfactory.createOMNamespace("", null);

        OMElement method = omfactory.createOMElement("doSpellingSuggestion", opN);
       //reqEnv.getBody().addChild(method);
        method.addAttribute("soapenv:encodingStyle","http://schemas.xmlsoap.org/soap/encoding/",null);
        OMElement value1 = omfactory.createOMElement("key",emptyNs);
        OMElement value2=omfactory.createOMElement("phrase",emptyNs);
        value1.addAttribute("xsi:type","xsd:string",null);
        value2.addAttribute("xsi:type","xsd:string",null);
        value1.addChild(omfactory.createText(value1, "wzdxGcZQFHJ71w7IgCj5ddQGLmODsP9g"));
        value2.addChild(omfactory.createText(value2,word));
        method.addChild(value2);
        method.addChild(value1);
        return method;
    }

    private SOAPEnvelope getEnvelope(String word)
    {
        SOAPFactory omfactory=OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv=omfactory.getDefaultEnvelope();
        OMNamespace emptyNs=omfactory.createOMNamespace("", null);
        OMNamespace xsi = reqEnv.declareNamespace("http://www.w3.org/1999/XMLSchema-instance","xsi");
        OMNamespace xsd = reqEnv.declareNamespace("http://www.w3.org/1999/XMLSchema","xsd");
        OMNamespace opN = reqEnv.declareNamespace("urn:GoogleSearch","ns1");
        OMElement method = omfactory.createOMElement("doSpellingSuggestion", opN);
        reqEnv.getBody().addChild(method);
        method.addAttribute("soapenv:encodingStyle","http://schemas.xmlsoap.org/soap/encoding/",null);
        OMElement value1 = omfactory.createOMElement("key",emptyNs);
        OMElement value2=omfactory.createOMElement("phrase",emptyNs);
        value1.addAttribute("xsi:type","xsd:string",null);
        value2.addAttribute("xsi:type","xsd:string",null);
        value1.addChild(omfactory.createText(value1, "wzdxGcZQFHJ71w7IgCj5ddQGLmODsP9g"));
        value2.addChild(omfactory.createText(value2,word));
        method.addChild(value2);
        method.addChild(value1);
        return reqEnv;

    }
    public void doAsyncSpellingSuggestion(String word)
    {
        OMElement requestElement = getElement(word);
        System.out.println("Initializing the Client Call....");
        Call call = null;
        try {
            call = new Call();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("Setting the Endpointreference ");
        URL url = null;
        try {
//            url = new URL("http","127.0.0.1",8080,"/search/beta2");
            url = new URL("http","api.google.com","/search/beta2");
        } catch (MalformedURLException e) {

            e.printStackTrace();
            System.exit(0);
        }

        call.setTo(new EndpointReference(AddressingConstants.WSA_TO, url.toString()));
        //call.invokeNonBlocking("doGoogleSpellingSugg",requestEnvelop, new ClientCallbackHandler(parent));
        try {
            call.invokeNonBlocking("doGoogleSpellingSugg",requestElement,this);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
    public void doSyncSpellingSuggestion(String word)
    {
        SOAPEnvelope response=null;
        SOAPEnvelope requestEnvelope = getEnvelope(word);
        System.out.println("Initializing the Client Call....");
        Call call = null;
        try {
            call = new Call();
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        System.out.println("Setting the Endpointreference ");
        URL url = null;
        try {
            url = new URL("http","api.google.com","/search/beta2");
        } catch (MalformedURLException e) {

            e.printStackTrace();
            System.exit(0);
        }

        call.setTo(new EndpointReference(AddressingConstants.WSA_TO, url.toString()));
        try {
            response=(SOAPEnvelope)call.invokeBlocking("doGoogleSpellingSugg",requestEnvelope);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


            this.getResponse(response);
    }
    public String getResponse(SOAPEnvelope responseEnvelope){
        ////////////////////////////////////////////
        try {
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
            responseEnvelope.serialize(writer);
            writer.flush();
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ////////////////////////////////////////////
        QName qName1 = new QName("urn:GoogleSearch", "doSpellingSuggestionResponse");
        QName qName2 = new QName("urn:GoogleSearch", "return");
        OMElement returnvalue1 = responseEnvelope.getBody().getFirstChildWithName(qName1);
        OMElement val = returnvalue1.getFirstChildWithName(qName2);
        org.apache.axis.om.OMNode omtext = val.getFirstChild();
        String sugession = null;
        sugession = ((org.apache.axis.om.OMText) omtext).getText();
        this.observer.update(sugession);

        return sugession;
    }

    public void onComplete(AsyncResult asyncResult) {
        String sugession = getResponse(asyncResult.getResponseEnvelope());
        this.observer.update(sugession);
        //To change body of implemented methods use File | Settings | File Templates.

    }

    public void reportError(Exception e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
