package org.apache.axis2.dataretrieval.client;
import java.net.URL;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.dataretrieval.DRConstants;
import org.apache.axis2.description.AxisService;

public class MexClient extends ServiceClient {

    public MexClient(ConfigurationContext configContext, AxisService axisService)
            throws AxisFault {
        super(configContext, axisService);
    }

    public MexClient(ConfigurationContext configContext,
            Definition wsdl4jDefinition, QName wsdlServiceName, String portName)
            throws AxisFault {
        super(configContext, wsdl4jDefinition, wsdlServiceName, portName);
    }

    public MexClient(ConfigurationContext configContext, URL wsdlURL,
            QName wsdlServiceName, String portName) throws AxisFault {
        super(configContext, wsdlURL, wsdlServiceName, portName);
    }

    public MexClient() throws AxisFault {
    }

    /**
     * Builds OMElement that makes up of SOAP body.
     */
    public OMElement setupGetMetadataRequest(String dialect,
            String identifier) throws AxisFault {
        
        // Attempt to engage MEX module
    /*    try{
           super.engageModule(new QName("metadataExchange"));
        }
        catch (Exception e){
          throw new AxisFault ("Unable to proceed with GetMetadata Request!", e);      
        } */
        
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(
                DRConstants.SPEC.NS_URI, DRConstants.SPEC.NS_PREFIX);

        OMElement method = fac.createOMElement(DRConstants.SPEC.GET_METADATA,
                omNs);
        if (dialect != null) {
            OMElement dialect_Elem = fac.createOMElement(
                    DRConstants.SPEC.DIALET, omNs);

            dialect_Elem.setText(dialect);
            method.addChild(dialect_Elem);
        }
        // create Identifier element
        if (identifier != null) {
            OMElement id_Elem = fac.createOMElement(
                    DRConstants.SPEC.IDENTIFIER, omNs);
            id_Elem.setText(identifier);
            method.addChild(id_Elem);
        }
        return method;
    }
}
