package sample.google.spellcheck;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.AsyncResult;
import org.apache.axis.clientapi.Call;
import org.apache.axis.clientapi.Callback;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.soap.SOAPBody;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.SOAPFactory;
import sample.google.common.util.PropertyLoader;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *  class sample.google.spellcheck.FormModel
 * This is the Impementation of the Asynchronous Client
 * @author Nadana Gunarathna
 *
 */
public class FormModel {

    Observer observer;
    private static final String PROTOCOL = "http";

    public FormModel(Observer observer)
    {
        this.observer = observer;
    }

    private OMElement getElement(String word){
        SOAPFactory omfactory=OMAbstractFactory.getSOAP11Factory();
        OMNamespace opN = omfactory.createOMNamespace("urn:GoogleSearch","ns1");
        OMNamespace emptyNs=omfactory.createOMNamespace("", null);

        OMElement method = omfactory.createOMElement("doSpellingSuggestion", opN);
        method.declareNamespace("http://www.w3.org/1999/XMLSchema-instance","xsi");
        method.declareNamespace("http://www.w3.org/1999/XMLSchema","xsd");

        //reqEnv.getBody().addChild(method);
        method.addAttribute("soapenv:encodingStyle","http://schemas.xmlsoap.org/soap/encoding/",null);
        OMElement value1 = omfactory.createOMElement("key",emptyNs);
        OMElement value2=omfactory.createOMElement("phrase",emptyNs);
        value1.addAttribute("xsi:type","xsd:string",null);
        value2.addAttribute("xsi:type","xsd:string",null);
        value1.addChild(omfactory.createText(value1,PropertyLoader.getGoogleKey() ));
        value2.addChild(omfactory.createText(value2,word));
        method.addChild(value2);
        method.addChild(value1);
        return method;
    }


    public void doAsyncSpellingSuggestion(String word)
    {
        OMElement requestElement = getElement(word);
        Call call = null;
        try {
            call = new Call();
        } catch (AxisFault axisFault) {
            observer.updateError(axisFault.getMessage());
        }
        URL url = null;
        try {
            url = new URL(PROTOCOL,PropertyLoader.getGoogleEndpointURL(),PropertyLoader.getGoogleEndpointServiceName());
        } catch (MalformedURLException e) {
            observer.updateError(e.getMessage());;
        }

        call.setTo(new EndpointReference(AddressingConstants.WSA_TO, url.toString()));
        try {
            call.invokeNonBlocking("doGoogleSpellingSugg",requestElement,new GoogleCallBack(word));
        } catch (AxisFault axisFault) {
            observer.updateError(axisFault.getMessage());
        }

    }
    public void doSyncSpellingSuggestion(String word)
    {
        OMElement responseElement=null;
        OMElement requestElement = getElement(word);
        Call call = null;
        try {
            call = new Call();
        } catch (AxisFault axisFault) {
            observer.updateError(axisFault.getMessage());
        }
        URL url = null;
        try {
            url = new URL(PROTOCOL,PropertyLoader.getGoogleEndpointURL(),PropertyLoader.getGoogleEndpointServiceName());
        } catch (MalformedURLException e) {
            observer.updateError(e.getMessage());
        }

        call.setTo(new EndpointReference(AddressingConstants.WSA_TO, url.toString()));
        try {
            responseElement=(OMElement)call.invokeBlocking("doGoogleSpellingSugg",requestElement);
        } catch (AxisFault axisFault) {
            observer.updateError(axisFault.getMessage());
        }


        this.getResponseFromElement(responseElement);
    }

    public String getResponseFromElement(OMElement responseElement){

        OMElement val = responseElement.getFirstElement();
        String sugession = val.getText();
        this.observer.update(sugession);
        return sugession;
    }


    public String getResponse(SOAPEnvelope responseEnvelope){
        QName qName1 = new QName("urn:GoogleSearch", "doSpellingSuggestionResponse");
        QName qName2 = new QName("urn:GoogleSearch", "return");
              

        SOAPBody body = responseEnvelope.getBody();
        if (body.hasFault()) {
            observer.updateError(body.getFault().getException().getMessage());
            return  null;
        } else{
            OMElement root = body.getFirstChildWithName(qName1);
            OMElement val =null;
            if (root!=null){
                 val = root.getFirstChildWithName(qName2);
            }else{
                observer.updateError("Correct response not received!");
                return  null;
            }

            String sugession = val.getText();
            if ((sugession==null) ||(sugession.trim().equals(""))){
                return null;
            }else{
                return sugession;
            }

        }
    }

    private class GoogleCallBack extends Callback{
        private String originalWord;
        public GoogleCallBack(String originalWord) {
            this.originalWord = originalWord;
        }

        public void onComplete(AsyncResult result) {
            String suggestion = getResponse(result.getResponseEnvelope());
            if (suggestion==null){
                observer.update(originalWord);
                observer.updateError("No suggestions found for "+originalWord);
            }else{
                observer.update(suggestion);
            }
        }

        public void reportError(Exception e) {
            observer.updateError(e.getMessage());
        }
    }

}
