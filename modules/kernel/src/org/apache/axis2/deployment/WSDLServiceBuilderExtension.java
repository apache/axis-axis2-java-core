package org.apache.axis2.deployment;

import java.util.Iterator;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * The class WSDLServiceBuilderExtension is a ServiceBuilderExtension which
 * facilitate to generate AxisServices based on WSDL 1.1 and WSDL 2.0 documents.
 * </p>
 * 
 * <p>
 * Axis2 ServiceDeployer use this extension.
 * </p>
 * 
 * @since 1.7.0
 */
public class WSDLServiceBuilderExtension extends AbstractServiceBuilderExtension {

    private static Log log = LogFactory.getLog(WSDLServiceBuilderExtension.class);

    public Map<String, AxisService> buildAxisServices(DeploymentFileData deploymentFileData)
            throws DeploymentException {
        ArchiveReader archiveReader = new ArchiveReader();
        Map<String, AxisService> wsdlservices = archiveReader.processWSDLs(deploymentFileData);
        if (wsdlservices != null && wsdlservices.size() > 0) {
            for (AxisService service : wsdlservices.values()) {
                Iterator<AxisOperation> operations = service.getOperations();
                while (operations.hasNext()) {
                    AxisOperation axisOperation = operations.next();
                    try {
                        getConfigurationContext().getAxisConfiguration().getPhasesInfo()
                                .setOperationPhases(axisOperation);
                    } catch (AxisFault e) {
                        throw new DeploymentException(e);
                    }
                }
            }
        }
        return wsdlservices;
    }
}
