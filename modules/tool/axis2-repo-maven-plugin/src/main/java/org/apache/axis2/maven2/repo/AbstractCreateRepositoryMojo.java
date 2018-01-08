/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.maven2.repo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

public abstract class AbstractCreateRepositoryMojo extends AbstractMojo {
    /**
     * @parameter property="project.artifacts"
     * @readonly
     * @required
     */
    private Set<Artifact> projectArtifacts;
    
    /**
     * @parameter property="project.collectedProjects"
     * @required
     * @readonly
     */
    private List<MavenProject> collectedProjects;
    
    /**
     * The directory (relative to the repository root) where AAR files are copied. This should be
     * set to the same value as the <tt>ServicesDirectory</tt> property in <tt>axis2.xml</tt>.
     * 
     * @parameter default-value="services"
     */
    private String servicesDirectory;
    
    /**
     * The directory (relative to the repository root) where MAR files are copied. This should be
     * set to the same value as the <tt>ModulesDirectory</tt> property in <tt>axis2.xml</tt>.
     * 
     * @parameter default-value="modules"
     */
    private String modulesDirectory;
    
    /**
     * The directory (relative to the repository root) where JAX-WS service JARs will be deployed.
     * 
     * @parameter default-value="servicejars"
     */
    private String jaxwsServicesDirectory;
    
    /**
     * The <tt>axis2.xml</tt> file to be copied into the repository.
     * 
     * @parameter
     */
    private File axis2xml;
    
    /**
     * If present, an <tt>axis2.xml</tt> file will be generated (Experimental).
     * 
     * @parameter
     */
    private GeneratedAxis2Xml generatedAxis2xml;
    
    /**
     * The directory (relative to the repository root) where the <tt>axis2.xml</tt> file will be
     * written. If this parameter is not set, then the file will be written into the repository
     * root.
     * 
     * @parameter
     */
    private String configurationDirectory;
    
    /**
     * Specifies whether the plugin should scan the project dependencies for AAR and MAR artifacts.
     * 
     * @parameter default-value="true"
     */
    private boolean useDependencies;
    
    /**
     * Specifies whether the plugin should scan Maven modules for AAR and MAR artifacts. This
     * parameter only has an effect for multimodule projects.
     * 
     * @parameter default-value="true"
     */
    private boolean useModules;
    
    /**
     * Specifies whether the plugin should generate <tt>services.list</tt> and <tt>modules.list</tt>
     * files.
     * 
     * @parameter default-value="false"
     */
    private boolean generateFileLists;
    
    /**
     * Specifies whether the plugin strips version numbers from AAR files.
     * 
     * @parameter default-value="true"
     */
    private boolean stripServiceVersion;
    
    /**
     * Specifies whether the plugin strips version numbers from MAR files.
     * 
     * @parameter default-value="false"
     */
    private boolean stripModuleVersion;
    
    /**
     * Specifies whether modules should be deployed to the repository.
     * 
     * @parameter default-value="true"
     */
    private boolean includeModules;
    
    /**
     * Comma separated list of modules (by artifactId) to include in the repository.
     * 
     * @parameter
     */
    private String modules;
    
    /**
     * Specifies whether services should be deployed to the repository.
     * 
     * @parameter default-value="true"
     */
    private boolean includeServices;
    
    /**
     * Comma separated list of services (by artifactId) to include in the repository.
     * 
     * @parameter
     */
    private String services;
    
    /**
     * A list of JAX-WS service JARs to be generated (by packaging class files from the current
     * project).
     * 
     * @parameter
     */
    private JAXWSService[] jaxwsServices;
    
    /**
     * A list of service descriptions that should be processed to build exploded AARs.
     * 
     * @parameter
     */
    private ServiceDescription[] serviceDescriptions;
    
    protected abstract String getScope();
    
    protected abstract File getInputDirectory();
    
    protected abstract File getOutputDirectory();
    
    protected abstract File[] getClassDirectories();

    private static void applyParameters(OMElement parentElement, Parameter[] parameters) {
        if (parameters == null) {
            return;
        }
        for (Parameter parameter : parameters) {
            OMElement parameterElement = null;
            for (Iterator<OMElement> it = parentElement.getChildrenWithLocalName("parameter"); it.hasNext(); ) {
                OMElement candidate = it.next();
                if (candidate.getAttributeValue(new QName("name")).equals(parameter.getName())) {
                    parameterElement = candidate;
                    break;
                }
            }
            if (parameterElement == null) {
                parameterElement = parentElement.getOMFactory().createOMElement("parameter", null, parentElement);
                parameterElement.addAttribute("name", parameter.getName(), null);
            }
            parameterElement.setText(parameter.getValue());
        }
    }

    private static void addMessageHandlers(OMElement root, MessageHandler[] handlers, String localName) {
        if (handlers == null) {
            return;
        }
        OMElement parent = root.getFirstChildWithName(new QName(localName + "s"));
        for (MessageHandler handler : handlers) {
            OMElement element = parent.getOMFactory().createOMElement(localName, null, parent);
            element.addAttribute("contentType", handler.getContentType(), null);
            element.addAttribute("class", handler.getClassName(), null);
        }
    }

    private static void processTransports(OMElement root, Transport[] transports, String localName) {
        if (transports == null) {
            return;
        }
        for (Transport transport : transports) {
            for (Iterator<OMElement> it = root.getChildrenWithLocalName(localName); it.hasNext(); ) {
                OMElement transportElement = it.next();
                if (transportElement.getAttributeValue(new QName("name")).equals(transport.getName())) {
                    applyParameters(transportElement, transport.getParameters());
                }
            }
        }
    }
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        File inputDirectory = getInputDirectory();
        File outputDirectory = getOutputDirectory();
        if (inputDirectory.exists()) {
            log.info("Copying files from " + inputDirectory);
            DirectoryScanner ds = new DirectoryScanner();
            ds.setBasedir(inputDirectory);
            ds.scan();
            for (String relativePath : ds.getIncludedFiles()) {
                try {
                    FileUtils.copyFile(
                            new File(inputDirectory, relativePath),
                            new File(outputDirectory, relativePath));
                } catch (IOException ex) {
                    throw new MojoExecutionException("Failed to copy " + relativePath, ex);
                }
            }
        }
        Set<Artifact> artifacts = new HashSet<Artifact>();
        if (useDependencies) {
            artifacts.addAll(projectArtifacts);
        }
        if (useModules) {
            for (MavenProject project : collectedProjects) {
                artifacts.add(project.getArtifact());
                artifacts.addAll(project.getAttachedArtifacts());
            }
        }
        if (includeModules || includeServices) {
            FilterArtifacts filter = new FilterArtifacts();
            filter.addFilter(new ScopeFilter(getScope(), null));
            if (includeModules && includeServices) {
                filter.addFilter(new TypeFilter("aar,mar", null));
            } else if (includeModules) {
                filter.addFilter(new TypeFilter("mar", null));
            }
            try {
                artifacts = filter.filter(artifacts);
            } catch (ArtifactFilterException ex) {
                throw new MojoExecutionException(ex.getMessage(), ex);
            }
            selectArtifacts(artifacts, modules, "mar");
            selectArtifacts(artifacts, services, "aar");
            Map<String,ArchiveDeployer> deployers = new HashMap<String,ArchiveDeployer>();
            deployers.put("aar", new ArchiveDeployer(outputDirectory, servicesDirectory, "services.list", generateFileLists, stripServiceVersion));
            deployers.put("mar", new ArchiveDeployer(outputDirectory, modulesDirectory, "modules.list", generateFileLists, stripModuleVersion));
            for (Artifact artifact : artifacts) {
                File file = artifact.getFile();
                if (file == null || file.isDirectory()) {
                    throw new MojoFailureException("Artifact " + artifact.getId() + " not available. " +
                            "This typically means that it is part of the reactor but that the " +
                            "package phase has not been executed.");
                }
                String type = artifact.getType();
                ArchiveDeployer deployer = deployers.get(type);
                if (deployer == null) {
                    throw new MojoExecutionException("No deployer found for artifact type " + type);
                }
                deployer.deploy(log, artifact);
            }
            for (ArchiveDeployer deployer : deployers.values()) {
                deployer.finish(log);
            }
        }
        if (jaxwsServices != null) {
            File targetDirectory = new File(outputDirectory, jaxwsServicesDirectory);
            for (JAXWSService service : jaxwsServices) {
                String jarName = service.getName() + ".jar";
                try {
                    JarArchiver archiver = new JarArchiver();
                    archiver.setDestFile(new File(targetDirectory, jarName));
                    String[] packages = service.getPackages();
                    String[] includes = new String[packages.length];
                    for (int i=0; i<packages.length; i++) {
                        includes[i] = packages[i].replace('.', '/') + "/**/*.class";
                    }
                    for (File classDirectory : getClassDirectories()) {
                        archiver.addDirectory(classDirectory, includes, new String[0]);
                    }
                    if (service.getResourcesDirectory() != null) {
                        archiver.addDirectory(service.getResourcesDirectory());
                    }
                    archiver.createArchive();
                } catch (ArchiverException ex) {
                    throw new MojoExecutionException("Failed to build " + jarName, ex);
                } catch (IOException ex) {
                    throw new MojoExecutionException("Failed to build " + jarName, ex);
                }
            }
        }
        if (serviceDescriptions != null) {
            File parentDirectory = new File(outputDirectory, servicesDirectory);
            for (ServiceDescription serviceDescription : serviceDescriptions) {
                File servicesFile = new File(serviceDescription.getDirectory(), "services.xml");
                File metaInfDirectory;
                try {
                    InputStream in = new FileInputStream(servicesFile);
                    try {
                        OMDocument doc = OMXMLBuilderFactory.createOMBuilder(in).getDocument();
                        OMElement serviceElement;
                        {
                            Iterator<OMElement> it = doc.getOMDocumentElement().getChildrenWithLocalName("service");
                            if (!it.hasNext()) {
                                throw new MojoFailureException("No service found in " + servicesFile);
                            }
                            serviceElement = it.next();
                            if (it.hasNext()) {
                                throw new MojoFailureException(servicesFile + " contains more than one service");
                            }
                        }
                        String serviceName = serviceElement.getAttributeValue(new QName("name"));
                        log.info("Building service " + serviceName);
                        metaInfDirectory = new File(new File(parentDirectory, serviceName), "META-INF");
                        metaInfDirectory.mkdirs();
                        if (serviceDescription.getScope() != null) {
                            serviceElement.addAttribute("scope", serviceDescription.getScope(), null);
                        }
                        applyParameters(serviceElement, serviceDescription.getParameters());
                        FileOutputStream out = new FileOutputStream(new File(metaInfDirectory, "services.xml"));
                        try {
                            doc.serialize(out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                    DirectoryScanner ds = new DirectoryScanner();
                    ds.setBasedir(serviceDescription.getDirectory());
                    ds.setExcludes(new String[] { "services.xml" });
                    ds.scan();
                    for (String relativePath : ds.getIncludedFiles()) {
                        try {
                            FileUtils.copyFile(
                                    new File(serviceDescription.getDirectory(), relativePath),
                                    new File(metaInfDirectory, relativePath));
                        } catch (IOException ex) {
                            throw new MojoExecutionException("Failed to copy " + relativePath, ex);
                        }
                    }
                } catch (IOException ex) {
                    throw new MojoExecutionException(ex.getMessage(), ex);
                } catch (XMLStreamException ex) {
                    throw new MojoExecutionException(ex.getMessage(), ex);
                }
            }
        }
        if (generatedAxis2xml != null || axis2xml != null) {
            File targetDirectory = configurationDirectory == null
                    ? outputDirectory : new File(outputDirectory, configurationDirectory);
            targetDirectory.mkdirs();
            File axis2xmlFile = new File(targetDirectory, "axis2.xml");
            if (axis2xml != null) {
                log.info("Copying axis2.xml");
                try {
                    FileUtils.copyFile(axis2xml, axis2xmlFile);
                } catch (IOException ex) {
                    throw new MojoExecutionException("Error copying axis2.xml file: " + ex.getMessage(), ex);
                }
            } else {
                log.info("Generating axis2.xml");
                try {
                    FilterArtifacts filter = new FilterArtifacts();
                    filter.addFilter(new ScopeFilter(getScope(), null));
                    filter.addFilter(new TypeFilter("jar", null));
                    List<URL> urls = new ArrayList<URL>();
                    for (Artifact artifact : filter.filter(projectArtifacts)) {
                        urls.add(artifact.getFile().toURI().toURL());
                    }
                    URLClassLoader classLoader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]));
                    InputStream in = classLoader.getResourceAsStream("org/apache/axis2/deployment/axis2_default.xml");
                    if (in == null) {
                        throw new MojoFailureException("The default axis2.xml file could not be found");
                    }
                    try {
                        OMDocument axis2xmlDoc = OMXMLBuilderFactory.createOMBuilder(in).getDocument();
                        OMElement root = axis2xmlDoc.getOMDocumentElement();
                        for (Iterator<OMNode> it = root.getDescendants(false); it.hasNext(); ) {
                            OMNode node = it.next();
                            if (node instanceof OMElement) {
                                OMElement element = (OMElement)node;
                                String classAttr = element.getAttributeValue(new QName("class"));
                                if (classAttr != null) {
                                    try {
                                        classLoader.loadClass(classAttr);
                                    } catch (ClassNotFoundException ex) {
                                        it.remove();
                                    }
                                }
                            }
                        }
                        applyParameters(root, generatedAxis2xml.getParameters());
                        processTransports(root, generatedAxis2xml.getTransportReceivers(), "transportReceiver");
                        processTransports(root, generatedAxis2xml.getTransportSenders(), "transportSender");
                        addMessageHandlers(root, generatedAxis2xml.getMessageBuilders(), "messageBuilder");
                        addMessageHandlers(root, generatedAxis2xml.getMessageFormatters(), "messageFormatter");
                        if (generatedAxis2xml.getHandlers() != null) {
                            for (Handler handler : generatedAxis2xml.getHandlers()) {
                                boolean handlerInserted = false;
                                for (Iterator<OMElement> phaseOrderIterator = root.getChildrenWithLocalName("phaseOrder"); phaseOrderIterator.hasNext(); ) {
                                    OMElement phaseOrder = phaseOrderIterator.next();
                                    if (phaseOrder.getAttributeValue(new QName("type")).equals(handler.getFlow())) {
                                        for (Iterator<OMElement> phaseIterator = phaseOrder.getChildrenWithLocalName("phase"); phaseIterator.hasNext(); ) {
                                            OMElement phase = phaseIterator.next();
                                            if (phase.getAttributeValue(new QName("name")).equals(handler.getPhase())) {
                                                OMElement handlerElement = axis2xmlDoc.getOMFactory().createOMElement("handler", null, phase);
                                                handlerElement.addAttribute("name", handler.getName(), null);
                                                handlerElement.addAttribute("class", handler.getClassName(), null);
                                                handlerInserted = true;
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                                if (!handlerInserted) {
                                    throw new MojoFailureException("Flow " + handler.getFlow() + " and phase " + handler.getPhase() + " not found");
                                }
                            }
                        }
                        if (generatedAxis2xml.getModules() != null) {
                            for (String module : generatedAxis2xml.getModules()) {
                                axis2xmlDoc.getOMFactory().createOMElement("module", null, root).addAttribute("ref", module, null);
                            }
                        }
                        OutputStream out = new FileOutputStream(axis2xmlFile);
                        try {
                            axis2xmlDoc.serialize(out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                } catch (ArtifactFilterException ex) {
                    throw new MojoExecutionException(ex.getMessage(), ex);
                } catch (IOException ex) {
                    throw new MojoExecutionException(ex.getMessage(), ex);
                } catch (XMLStreamException ex) {
                    throw new MojoExecutionException(ex.getMessage(), ex);
                }
            }
        }
    }

    private void selectArtifacts(Set<Artifact> artifacts, String list, String type) throws MojoFailureException {
        if (list != null) {
            Set<String> set = new HashSet<String>(Arrays.asList(StringUtils.stripAll(StringUtils.split(list, ","))));
            for (Iterator<Artifact> it = artifacts.iterator(); it.hasNext(); ) {
                Artifact artifact = it.next();
                if (artifact.getType().equals(type) && !set.remove(artifact.getArtifactId())) {
                    it.remove();
                }
            }
            if (!set.isEmpty()) {
                throw new MojoFailureException("The following " + type + " artifacts have not been found: " + set);
            }
        }
    }
}
