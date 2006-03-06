package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.XMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Utility methods for various clients to use.
 */
public class ClientUtils {

    public static TransportOutDescription inferOutTransport(AxisConfiguration ac,
                                                            EndpointReference epr) throws AxisFault {
        if (epr == null || (epr.getAddress() == null)) {
            throw new AxisFault(Messages.getMessage("cannotInferTransport"));
        }

        String uri = epr.getAddress();
        int index = uri.indexOf(':');
        String transport = (index > 0) ? uri.substring(0, index) : null;
        if (transport != null) {
            return ac.getTransportOut(new QName(transport));
        } else {
            throw new AxisFault(Messages.getMessage("cannotInferTransport"));
        }
    }

    public static TransportInDescription inferInTransport(AxisConfiguration ac,
                                                          Options options,
                                                          MessageContext msgCtxt) throws AxisFault {
        String listenerTransportProtocol = options.getTransportInProtocol();
        TransportInDescription transportIn = null;
        if (options.isUseSeparateListener()) {
            if ((listenerTransportProtocol != null) && !"".equals(listenerTransportProtocol)) {
                transportIn = ac.getTransportIn(new QName(listenerTransportProtocol));

                if (transportIn == null) {
                    // TODO : User should not be mandated to give an IN transport. If it is not given, we should
                    // ask from the ListenerManager to give any available transport for this client.
                    throw new AxisFault(Messages.getMessage("unknownTransport",
                            listenerTransportProtocol));
                }
            }
            if (msgCtxt.getAxisService() != null) {
                AxisService service = msgCtxt.getAxisService();
                Iterator itr = service.getEngagedModules().iterator();
                boolean found = false;
                while (itr.hasNext()) {
                    AxisModule axisModule = (AxisModule) itr.next();
                    if (axisModule.getName().getLocalPart().indexOf(Constants.MODULE_ADDRESSING) >= 0) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new AxisFault(Messages.getMessage("2channelNeedAddressing"));
                }

            } else {
                if (!ac.isEngaged(new QName(Constants.MODULE_ADDRESSING))) {
                    throw new AxisFault(Messages.getMessage("2channelNeedAddressing"));
                }
            }
        }

        return transportIn;

    }

    /**
     * To create a AxisService for a given WSDL and the created client is most suitable for clinet side
     * invocation not for server side invocation. Since all the soap acction and wsa action is added to
     * operations
     *
     * @param wsdlURL         location of the WSDL
     * @param wsdlServiceName name of the service to be invoke , if it is null then the first one will
     *                        be selected if there are more than one
     * @param portName        name of the port , if there are more than one , if it is null then the
     *                        first one in the  iterator will be selected
     * @param options         Service client options, to set the target EPR
     * @return AxisService , the created servie will be return
     */
    public static AxisService creatAxisService(URL wsdlURL,
                                               QName wsdlServiceName,
                                               String portName,
                                               Options options) throws AxisFault {
        AxisService axisService;
        try {
            InputStream in = wsdlURL.openConnection().getInputStream();
            Document doc = XMLUtils.newDocument(in);
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            reader.setFeature("javax.wsdl.importDocuments", true);
            Definition wsdlDefinition = reader.readWSDL(null, doc);
            axisService = new AxisService();

            Service wsdlService;
            if (wsdlServiceName != null) {
                wsdlService = wsdlDefinition.getService(wsdlServiceName);
                if (wsdlService == null) {
                    throw new AxisFault(
                            Messages.getMessage("servicenotfoundinwsdl",
                                    wsdlServiceName.getLocalPart()));
                }

            } else {
                Collection col = wsdlDefinition.getServices().values();
                if (col != null && col.size() > 0) {
                    wsdlService = (Service) col.iterator().next();
                    if (wsdlService == null) {
                        throw new AxisFault(Messages.getMessage("noservicefoundinwsdl"));
                    }
                } else {
                    throw new AxisFault(Messages.getMessage("noservicefoundinwsdl"));
                }
            }
            axisService.setName(wsdlService.getQName().getLocalPart());

            Port port;
            if (portName != null) {
                port = wsdlService.getPort(portName);
                if (port == null) {
                    throw new AxisFault(Messages.getMessage("noporttypefoundfor", portName));
                }
            } else {
                Collection ports = wsdlService.getPorts().values();
                if (ports != null && ports.size() > 0) {
                    port = (Port) ports.iterator().next();
                    if (port == null) {
                        throw new AxisFault(Messages.getMessage("noporttypefound"));
                    }
                } else {
                    throw new AxisFault(Messages.getMessage("noporttypefound"));
                }
            }
            List exteElemts = port.getExtensibilityElements();
            if (exteElemts != null) {
                Iterator extItr = exteElemts.iterator();
                while (extItr.hasNext()) {
                    Object extensibilityElement = extItr.next();
                    if (extensibilityElement instanceof SOAPAddress) {
                        SOAPAddress address = (SOAPAddress) extensibilityElement;
                        options.setTo(new EndpointReference(address.getLocationURI()));
                    }
                }
            }

            Binding binding = port.getBinding();
            Iterator bindingOperations = binding.getBindingOperations().iterator();
            while (bindingOperations.hasNext()) {
                BindingOperation bindingOperation = (BindingOperation) bindingOperations.next();
                AxisOperation axisOperation;
                if (bindingOperation.getBindingInput() == null &&
                        bindingOperation.getBindingOutput() != null) {
                    axisOperation = new OutOnlyAxisOperation();
                } else {
                    axisOperation = new OutInAxisOperation();
                }
                axisOperation.setName(new QName(bindingOperation.getName()));
                List list = bindingOperation.getExtensibilityElements();
                if (list != null) {
                    Iterator exteElements = list.iterator();
                    while (exteElements.hasNext()) {
                        Object extensibilityElement = exteElements.next();
                        if (extensibilityElement instanceof SOAPOperation) {
                            SOAPOperation soapOp = (SOAPOperation) extensibilityElement;
                            axisOperation.addParameter(new Parameter(AxisOperation.SOAP_ACTION,
                                    soapOp.getSoapActionURI()));
                        }
                    }
                }
                axisService.addOperation(axisOperation);
            }

        } catch (IOException e) {
            throw new AxisFault("IOException" + e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new AxisFault("ParserConfigurationException" + e.getMessage());
        } catch (SAXException e) {
            throw new AxisFault("SAXException" + e.getMessage());
        } catch (WSDLException e) {
            throw new AxisFault("WSDLException" + e.getMessage());
        }
        return axisService;
    }
}
