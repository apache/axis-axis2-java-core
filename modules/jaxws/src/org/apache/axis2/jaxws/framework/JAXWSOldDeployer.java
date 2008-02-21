package org.apache.axis2.jaxws.framework;


import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.Loader;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * JAXWSOldDeployer is a custom deployer modeled after the POJODeployer. Its purpose
 * is to deploy .wars and expanded .war directories
 */
public class JAXWSOldDeployer implements Deployer {

    private static Log log = LogFactory.getLog(JAXWSDeployer.class);

    private ConfigurationContext configCtx;

    //To initialize the deployer
    public void init(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
    }//Will process the file and add that to axisConfig

    public void deploy(DeploymentFileData deploymentFileData) {
        ClassLoader threadClassLoader = null;
        try {
            threadClassLoader = Thread.currentThread().getContextClassLoader();
            String extension = DeploymentFileData.getFileExtension(deploymentFileData.getName());
            if ("war".equals(extension) || "jar".equals(extension)) {
                ArrayList classList;
                FileInputStream fin = null;
                ZipInputStream zin = null;
                try {
                    fin = new FileInputStream(deploymentFileData.getAbsolutePath());
                    zin = new ZipInputStream(fin);
                    ZipEntry entry;
                    classList = new ArrayList();
                    while ((entry = zin.getNextEntry()) != null) {
                        String name = entry.getName();
                        if (name.endsWith(".class")) {
                            classList.add(name);
                        }
                    }
                    zin.close();
                    fin.close();
                } catch (Exception e) {
                    log.debug(Messages.getMessage("deployingexception",e.getMessage()),e);
                    throw new DeploymentException(e);
                } finally {
                    if (zin != null) {
                        zin.close();
                    }
                    if (fin != null) {
                        fin.close();
                    }
                }
                ArrayList axisServiceList = new ArrayList();
                for (int i = 0; i < classList.size(); i++) {
                    String className = (String) classList.get(i);
                    ClassLoader classLoader = Utils.createClassLoader(
                            new URL[]{deploymentFileData.getFile().toURL()},
                            configCtx.getAxisConfiguration().getSystemClassLoader(),
                            true,
                            (File) configCtx.getAxisConfiguration().
                                    getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR));
                    Thread.currentThread().setContextClassLoader(classLoader);
                    className = className.replaceAll(".class", "");
                    className = className.replaceAll("/", ".");
                    
                    try {
                        Class claxx = Class.forName(className);
                    	
                        Class pojoClass = Loader.loadClass(classLoader, className);
                        WebService wsAnnotation = (WebService) pojoClass.getAnnotation(WebService.class);
                        WebServiceProvider wspAnnotation = null;
                        if (wsAnnotation == null) {
                        	wspAnnotation = (WebServiceProvider) pojoClass.getAnnotation(WebServiceProvider.class);
                        }
                        
                        //Create the AxisService 
                        //If this contains a WebService annotation, make sure that we are not
                        //processing the pure SEI. We should only create the AxisService based on the 
                        //implementation class
                        if ((wsAnnotation != null && !isEmpty(wsAnnotation.endpointInterface())) || wspAnnotation != null) {
                            AxisService axisService;
                            axisService =
                                    createAxisService(classLoader,
                                                      className,
                                                      deploymentFileData.getFile().toURL());
                            
                            if (axisService != null) {
                            	axisServiceList.add(axisService);                            	
                            }
                            	
                        }
                    } catch (Exception e) {
                    	// Seems like the jax-ws jars missing in the class path .
                    	log.debug(Messages.getMessage("jaxwsjarsmissing",e.getMessage()),e);
                    }


                }
                if (axisServiceList.size() > 0) {
                    AxisServiceGroup serviceGroup = new AxisServiceGroup();
                    serviceGroup.setServiceGroupName(deploymentFileData.getName());
                    for (int i = 0; i < axisServiceList.size(); i++) {
                        AxisService axisService = (AxisService) axisServiceList.get(i);
                        serviceGroup.addService(axisService);
                    }
                    configCtx.getAxisConfiguration().addServiceGroup(serviceGroup);
                } else {
                	//REVIEW: If presented with a .jar/.war that contains no annotated classes
                	//is this actually an error that should be propagated up?
                    String msg = "Error:\n No annotated classes found in the jar: " +
                                 deploymentFileData.getFile().getName() +
                                 ". Service deployment failed.";
                    log.error(msg);
                    configCtx.getAxisConfiguration().getFaultyServices().
                            put(deploymentFileData.getFile().getAbsolutePath(), msg);
                }
            } 
            //TODO: Possible other extensions here
            //else if () {}
        } catch (Exception e) {
             log.debug(Messages.getMessage("stroringfaultyservice",e.getMessage()),e);
            storeFaultyService(deploymentFileData, e);
        } catch (Throwable t) {
            log.debug(Messages.getMessage("stroringfaultyservice",t.getMessage()),t);
            storeFaultyService(deploymentFileData, t);
        } finally {
            if (threadClassLoader != null) {
                Thread.currentThread().setContextClassLoader(threadClassLoader);
            }
        }
    }

    private void storeFaultyService(DeploymentFileData deploymentFileData, Throwable t) {
        StringWriter errorWriter = new StringWriter();
        PrintWriter ptintWriter = new PrintWriter(errorWriter);
        t.printStackTrace(ptintWriter);
        String error = "Error:\n" + errorWriter.toString();
        configCtx.getAxisConfiguration().getFaultyServices().
                put(deploymentFileData.getFile().getAbsolutePath(), error);
    }

    private AxisService createAxisService(ClassLoader classLoader,
                                          String className,
                                          URL serviceLocation) throws ClassNotFoundException,
                                                                      InstantiationException,
                                                                      IllegalAccessException,
                                                                      AxisFault {
        AxisService axisService = null;
        try {
            Class claxx = Class.forName(
                    "org.apache.axis2.jaxws.description.DescriptionFactory");
            Method mthod = claxx.getMethod(
                    "createAxisService",
                    new Class[]{Class.class});
            Class pojoClass = Loader.loadClass(classLoader, className);
            axisService =
                    (AxisService) mthod.invoke(claxx, new Object[]{pojoClass});
            if (axisService != null) {
                Iterator operations = axisService.getOperations();
                while (operations.hasNext()) {
                    AxisOperation axisOperation = (AxisOperation) operations.next();
                    if (axisOperation.getMessageReceiver() == null) {
                        try {
                            Class jaxwsMR = Loader.loadClass(
                                    "org.apache.axis2.jaxws.server.JAXWSMessageReceiver");
                            MessageReceiver jaxwsMRInstance =
                                    (MessageReceiver) jaxwsMR.newInstance();
                            axisOperation.setMessageReceiver(jaxwsMRInstance);
                        } catch (Exception e) {
                            log.debug("Error occurde while loading JAXWSMessageReceiver for "
                                    + className );
                        }
                    }
                }
            }
            axisService.setElementFormDefault(false);
            axisService.setFileName(serviceLocation);
           //Not needed at this case, the message receivers always set to RPC if this executes
            //setMessageReceivers(axisService);
            
        } catch (Exception e) {
            // Seems like the jax-ws jars missing in the class path .
            log.debug(Messages.getMessage(DeploymentErrorMsgs.JAXWS_JARS_MISSING,e.getMessage()),e);
        }
        return axisService;
    }

    public void setMessageReceivers(AxisService service) {
        Iterator iterator = service.getOperations();
        while (iterator.hasNext()) {
            AxisOperation operation = (AxisOperation) iterator.next();
            String MEP = operation.getMessageExchangePattern();
            if (MEP != null) {
                try {
                    if (WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY.equals(MEP)
                        || WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_ONLY.equals(MEP)
                        || WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)) {
                        Class inOnlyMessageReceiver = Loader.loadClass(
                                "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver");
                        MessageReceiver messageReceiver =
                                (MessageReceiver) inOnlyMessageReceiver.newInstance();
                        operation.setMessageReceiver(messageReceiver);
                    } else {
                        Class inoutMessageReceiver = Loader.loadClass(
                                "org.apache.axis2.rpc.receivers.RPCMessageReceiver");
                        MessageReceiver inOutmessageReceiver =
                                (MessageReceiver) inoutMessageReceiver.newInstance();
                        operation.setMessageReceiver(inOutmessageReceiver);
                    }
                } catch (ClassNotFoundException e) {
                    log.error(e.getMessage(), e);
                } catch (InstantiationException e) {
                    log.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public void setDirectory(String directory) {
    }

    public void setExtension(String extension) {
    }

    public void unDeploy(String fileName) {
        fileName = Utils.getShortFileName(fileName);
        if (fileName.endsWith(".class")) {
            String className = fileName.replaceAll(".class", "");
            try {
                AxisServiceGroup serviceGroup =
                        configCtx.getAxisConfiguration().removeServiceGroup(className);
                configCtx.removeServiceGroupContext(serviceGroup);
                log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                                             fileName));
            } catch (AxisFault axisFault) {
                //May be a faulty service
                log.debug(Messages.getMessage(DeploymentErrorMsgs.FAULTY_SERVICE_REMOVAL,axisFault.getMessage()),axisFault);
                configCtx.getAxisConfiguration().removeFaultyService(fileName);
            }
        } else if (fileName.endsWith(".jar")) {
            try {
                AxisServiceGroup serviceGroup =
                        configCtx.getAxisConfiguration().removeServiceGroup(fileName);
                configCtx.removeServiceGroupContext(serviceGroup);
                log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                                             fileName));
            } catch (AxisFault axisFault) {
                //May be a faulty service
                log.debug(Messages.getMessage(DeploymentErrorMsgs.FAULTY_SERVICE_REMOVAL,axisFault.getMessage()),axisFault);
                configCtx.getAxisConfiguration().removeFaultyService(fileName);
            }
        }
    }

    private boolean isEmpty(String string) {
        return (string == null || "".equals(string));
    }

}

