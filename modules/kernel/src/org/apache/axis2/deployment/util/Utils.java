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

package org.apache.axis2.deployment.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentClassLoader;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.*;
import org.apache.axis2.description.java2wsdl.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.PolicyComponent;
import org.apache.ws.commons.schema.utils.NamespaceMap;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {

	public static String defaultEncoding = new OutputStreamWriter(System.out)
			.getEncoding();

	private static Log log = LogFactory.getLog(Utils.class);

	public static void addFlowHandlers(Flow flow, ClassLoader clsLoader)
			throws AxisFault {
		int count = flow.getHandlerCount();

		for (int j = 0; j < count; j++) {
			HandlerDescription handlermd = flow.getHandler(j);
			Handler handler;

			final Class handlerClass = getHandlerClass(
					handlermd.getClassName(), clsLoader);

			try {
				handler = (Handler) org.apache.axis2.java.security.AccessController
						.doPrivileged(new PrivilegedExceptionAction() {
							public Object run() throws InstantiationException,
									IllegalAccessException {
								return handlerClass.newInstance();
							}
						});
				handler.init(handlermd);
				handlermd.setHandler(handler);
			} catch (PrivilegedActionException e) {
				throw AxisFault.makeFault(e);
			}
		}
	}

	public static boolean loadHandler(ClassLoader loader1,
			HandlerDescription desc) throws DeploymentException {
		String handlername = desc.getClassName();
		Handler handler;
		try {
			final Class handlerClass = Loader.loadClass(loader1, handlername);
			Package aPackage = (Package) org.apache.axis2.java.security.AccessController
					.doPrivileged(new PrivilegedAction() {
						public Object run() {
							return handlerClass.getPackage();
						}
					});
			if (aPackage != null
					&& aPackage.getName().equals("org.apache.axis2.engine")) {
				String name = handlerClass.getName();
				log.warn("Dispatcher " + name + " is now deprecated.");
				if (name.indexOf("InstanceDispatcher") != -1) {
					log.warn("Please remove the entry for "
							+ handlerClass.getName() + "from axis2.xml");
				} else {
					log
							.warn("Please edit axis2.xml "
									+ "and replace with the same class in org.apache.axis2.dispatchers package");
				}
			}
			handler = (Handler) org.apache.axis2.java.security.AccessController
					.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws InstantiationException,
								IllegalAccessException {
							return handlerClass.newInstance();
						}
					});
			handler.init(desc);
			desc.setHandler(handler);
        } catch (UnsupportedClassVersionError e) {
            throw e;
        } catch (ClassNotFoundException e) {
			if (handlername.indexOf("jaxws") > 0) {
				log.warn("[JAXWS] - unable to load " + handlername);
				return false;
			}
			throw new DeploymentException(e);
		} catch (Exception e) {
			throw new DeploymentException(e);
		}
		return true;
	}

	public static URL[] getURLsForAllJars(URL url, File tmpDir) {
		FileInputStream fin = null;
		InputStream in = null;
		ZipInputStream zin = null;
		try {
			ArrayList array = new ArrayList();
			in = url.openStream();
			String fileName = url.getFile();
			int index = fileName.lastIndexOf('/');
			if (index != -1) {
				fileName = fileName.substring(index + 1);
			}
			final File f = createTempFile(fileName, in, tmpDir);

			fin = (FileInputStream) org.apache.axis2.java.security.AccessController
					.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws FileNotFoundException {
							return new FileInputStream(f);
						}
					});
			array.add(f.toURL());
			zin = new ZipInputStream(fin);

			ZipEntry entry;
			String entryName;
			while ((entry = zin.getNextEntry()) != null) {
				entryName = entry.getName();
				/**
				 * id the entry name start with /lib and end with .jar then
				 * those entry name will be added to the arraylist
				 */
				if ((entryName != null)
						&& entryName.toLowerCase().startsWith("lib/")
						&& entryName.toLowerCase().endsWith(".jar")) {
					String suffix = entryName.substring(4);
					File f2 = createTempFile(suffix, zin, tmpDir);
					array.add(f2.toURL());
				}
			}
			return (URL[]) array.toArray(new URL[array.size()]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					//
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					//
				}
			}
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	public static File createTempFile(final String suffix, InputStream in,
			final File tmpDir) throws IOException {
		byte data[] = new byte[2048];
		int count;
		File f;
		if (tmpDir == null) {
			String directory = (String) org.apache.axis2.java.security.AccessController
					.doPrivileged(new PrivilegedAction() {
						public Object run() {
							return System.getProperty("java.io.tmpdir");
						}
					});
			final File tempFile = new File(directory, "_axis2");
			Boolean exists = (Boolean) org.apache.axis2.java.security.AccessController
					.doPrivileged(new PrivilegedAction() {
						public Object run() {
							return Boolean.valueOf(tempFile.exists());
						}
					});
			if (!exists.booleanValue()) {
				Boolean mkdirs = (Boolean) org.apache.axis2.java.security.AccessController
						.doPrivileged(new PrivilegedAction() {
							public Object run() {
								return Boolean.valueOf(tempFile.mkdirs());
							}
						});
				if (!mkdirs.booleanValue()) {
					throw new IOException("Unable to create the directory");
				}
			}
			try {
				f = (File) org.apache.axis2.java.security.AccessController
						.doPrivileged(new PrivilegedExceptionAction() {
							public Object run() throws IOException {
								return File.createTempFile("axis2", suffix,
										tempFile);
							}
						});
			} catch (PrivilegedActionException e) {
				throw (IOException) e.getException();
			}
		} else {
			try {
				f = (File) org.apache.axis2.java.security.AccessController
						.doPrivileged(new PrivilegedExceptionAction() {
							public Object run() throws IOException {
								return File.createTempFile("axis2", suffix,
										tmpDir);
							}
						});
			} catch (PrivilegedActionException e) {
				throw (IOException) e.getException();
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Created temporary file : " + f.getAbsolutePath());// $NON-SEC-4
		}
		final File f2 = f;
		org.apache.axis2.java.security.AccessController
				.doPrivileged(new PrivilegedAction() {
					public Object run() {
						f2.deleteOnExit();
						return null;
					}
				});
		FileOutputStream out;
		try {
			out = (FileOutputStream) org.apache.axis2.java.security.AccessController
					.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws FileNotFoundException {
							return new FileOutputStream(f2);
						}
					});
		} catch (PrivilegedActionException e) {
			throw (FileNotFoundException) e.getException();
		}
		while ((count = in.read(data, 0, 2048)) != -1) {
			out.write(data, 0, count);
		}
		out.close();
		return f;
	}

	public static ClassLoader getClassLoader(ClassLoader parent, String path)
			throws DeploymentException {
		return getClassLoader(parent, new File(path));
	}

	/**
	 * Get a ClassLoader which contains a classpath of a) the passed directory
	 * and b) any jar files inside the "lib/" or "Lib/" subdirectory of the
	 * passed directory.
	 * 
	 * @param parent
	 *            parent ClassLoader which will be the parent of the result of
	 *            this method
	 * @param file
	 *            a File which must be a directory for this to be useful
	 * @return a new ClassLoader pointing to both the passed dir and jar files
	 *         under lib/
	 * @throws DeploymentException
	 *             if problems occur
	 */
	public static ClassLoader getClassLoader(final ClassLoader parent, File file)
			throws DeploymentException {
		URLClassLoader classLoader;

		if (file == null)
			return null; // Shouldn't this just return the parent?

		try {
			ArrayList urls = new ArrayList();
			urls.add(file.toURL());

			// lower case directory name
			File libfiles = new File(file, "lib");
			if (!addFiles(urls, libfiles)) {
				// upper case directory name
				libfiles = new File(file, "Lib");
				addFiles(urls, libfiles);
			}

			final URL urllist[] = new URL[urls.size()];
			for (int i = 0; i < urls.size(); i++) {
				urllist[i] = (URL) urls.get(i);
			}
			classLoader = (URLClassLoader) AccessController
					.doPrivileged(new PrivilegedAction() {
						public Object run() {
							return new URLClassLoader(urllist, parent);
						}
					});
			return classLoader;
		} catch (MalformedURLException e) {
			throw new DeploymentException(e);
		}
	}

	private static boolean addFiles(ArrayList urls, final File libfiles)
			throws MalformedURLException {
		Boolean exists = (Boolean) org.apache.axis2.java.security.AccessController
				.doPrivileged(new PrivilegedAction() {
					public Object run() {
						return Boolean.valueOf(libfiles.exists());
					}
				});
		if (exists.booleanValue()) {
			urls.add(libfiles.toURL());
			File jarfiles[] = (File[]) org.apache.axis2.java.security.AccessController
					.doPrivileged(new PrivilegedAction() {
						public Object run() {
							return libfiles.listFiles();
						}
					});
			for (int i = 0; i < jarfiles.length; i++) {
				File jarfile = jarfiles[i];
				if (jarfile.getName().endsWith(".jar")) {
					urls.add(jarfile.toURL());
				}
			}
		}
		return exists.booleanValue();
	}

	private static Class getHandlerClass(String className, ClassLoader loader1)
			throws AxisFault {
		Class handlerClass;

		try {
			handlerClass = Loader.loadClass(loader1, className);
		} catch (ClassNotFoundException e) {
			throw AxisFault.makeFault(e);
		}

		return handlerClass;
	}

	/**
	 * This guy will create a AxisService using java reflection
	 * 
	 * @param axisService
	 *            the target AxisService
	 * @param axisConfig
	 *            the in-scope AxisConfiguration
	 * @param excludeOperations
	 *            a List of Strings (or null), each containing a method to
	 *            exclude
	 * @param nonRpcMethods
	 *            a List of Strings (or null), each containing a non-rpc method
	 *            name
	 * @throws Exception
	 *             if a problem occurs
	 */
	public static void fillAxisService(final AxisService axisService,
			AxisConfiguration axisConfig, ArrayList excludeOperations,
			ArrayList nonRpcMethods) throws Exception {
		String serviceClass;
		Parameter implInfoParam = axisService
				.getParameter(Constants.SERVICE_CLASS);
		ClassLoader serviceClassLoader = axisService.getClassLoader();

		if (implInfoParam != null) {
			serviceClass = (String) implInfoParam.getValue();
		} else {
			// if Service_Class is null, every AbstractMR will look for
			// ServiceObjectSupplier. This is user specific and may contain
			// other looks.
			implInfoParam = axisService
					.getParameter(Constants.SERVICE_OBJECT_SUPPLIER);
			if (implInfoParam != null) {
				String className = ((String) implInfoParam.getValue()).trim();
				final Class serviceObjectMaker = Loader.loadClass(
						serviceClassLoader, className);
				if (serviceObjectMaker.getModifiers() != Modifier.PUBLIC) {
					throw new AxisFault("Service class " + className
							+ " must have public as access Modifier");
				}

				// Find static getServiceObject() method, call it if there
				final Method method = (Method) org.apache.axis2.java.security.AccessController
						.doPrivileged(new PrivilegedExceptionAction() {
							public Object run() throws NoSuchMethodException {
								return serviceObjectMaker.getMethod(
										"getServiceObject",
										new Class[] { AxisService.class });
							}
						});
				Object obj = null;
				if (method != null) {
					obj = org.apache.axis2.java.security.AccessController
							.doPrivileged(new PrivilegedExceptionAction() {
								public Object run()
										throws InstantiationException,
										IllegalAccessException,
										InvocationTargetException {
									return method.invoke(serviceObjectMaker
											.newInstance(),
											new Object[] { axisService });
								}
							});
				}
				if (obj == null) {
					log.warn("ServiceObjectSupplier implmentation Object could not be found");
					throw new DeploymentException(
							"ServiceClass or ServiceObjectSupplier implmentation Object could not be found");
				}
				serviceClass = obj.getClass().getName();
			} else {
				return;
			}
		}
		// adding name spaces
		NamespaceMap map = new NamespaceMap();
		map.put(Java2WSDLConstants.AXIS2_NAMESPACE_PREFIX,
				Java2WSDLConstants.AXIS2_XSD);
		map.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX,
				Java2WSDLConstants.URI_2001_SCHEMA_XSD);
		axisService.setNamespaceMap(map);
		SchemaGenerator schemaGenerator;
		Parameter generateBare = axisService
				.getParameter(Java2WSDLConstants.DOC_LIT_BARE_PARAMETER);
		if (generateBare != null && "true".equals(generateBare.getValue())) {
			schemaGenerator = new DocLitBareSchemaGenerator(serviceClassLoader,
					serviceClass.trim(),
					axisService.getSchemaTargetNamespace(), axisService
							.getSchemaTargetNamespacePrefix(), axisService);
		} else {
			schemaGenerator = new DefaultSchemaGenerator(serviceClassLoader,
					serviceClass.trim(),
					axisService.getSchemaTargetNamespace(), axisService
							.getSchemaTargetNamespacePrefix(), axisService);
		}
		schemaGenerator.setExcludeMethods(excludeOperations);
		schemaGenerator.setNonRpcMethods(nonRpcMethods);
		if (!axisService.isElementFormDefault()) {
			schemaGenerator
					.setElementFormDefault(Java2WSDLConstants.FORM_DEFAULT_UNQUALIFIED);
		}
		// package to namespace map
		schemaGenerator.setPkg2nsmap(axisService.getP2nMap());
		Collection schemas = schemaGenerator.generateSchema();
		axisService.addSchema(schemas);
		axisService.setSchemaTargetNamespace(schemaGenerator
				.getSchemaTargetNameSpace());
		axisService.setTypeTable(schemaGenerator.getTypeTable());
		if (Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE.equals(axisService
				.getTargetNamespace())) {
			axisService
					.setTargetNamespace(schemaGenerator.getTargetNamespace());
		}

		Method[] method = schemaGenerator.getMethods();
		PhasesInfo pinfo = axisConfig.getPhasesInfo();

		for (int i = 0; i < method.length; i++) {
			Method jmethod = method[i];
			String opName = jmethod.getName();
			AxisOperation operation = axisService
					.getOperation(new QName(opName));
			// if the operation there in services.xml then try to set it schema
			// element name
			if (operation == null) {
				operation = axisService.getOperation(new QName(
						jmethod.getName()));
			}
			MessageReceiver mr = axisService.getMessageReceiver(operation
					.getMessageExchangePattern());
			if (mr != null) {
			} else {
				mr = axisConfig.getMessageReceiver(operation
						.getMessageExchangePattern());
			}
			if (operation.getMessageReceiver() == null) {
				operation.setMessageReceiver(mr);
			}
			pinfo.setOperationPhases(operation);
			axisService.addOperation(operation);
			if (operation.getSoapAction() == null) {
				operation.setSoapAction("urn:" + opName);
			}
		}
	}

	public static AxisOperation getAxisOperationForJmethod(Method method)
			throws AxisFault {
		AxisOperation operation;
		if ("void".equals(method.getReturnType().getName())) {
			if (method.getExceptionTypes().length > 0) {
				operation = AxisOperationFactory
						.getAxisOperation(WSDLConstants.MEP_CONSTANT_ROBUST_IN_ONLY);
			} else {
				operation = AxisOperationFactory
						.getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_ONLY);
			}
		} else {
			operation = AxisOperationFactory
					.getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_OUT);
		}
		String opName = method.getName();
		operation.setName(new QName(opName));
		WebMethod methodAnnon = method.getAnnotation(WebMethod.class);
        if (methodAnnon != null) {
			String action = methodAnnon.action();
			if (action != null && !"".equals(action)) {
				operation.setSoapAction(action);
			}
		}
		return operation;
	}

	public static OMElement getParameter(String name, String value,
			boolean locked) {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement parameter = fac.createOMElement("parameter", null);
		parameter.addAttribute("name", name, null);
		parameter.addAttribute("locked", Boolean.toString(locked), null);
		parameter.setText(value);
		return parameter;
	}

	/**
	 * This method is to get the list of services there in a module if module
	 * want to add services then the way of doing that is 1. Add a directory
	 * called services inside the module (both in mar case and expanded case) 2.
	 * Then add a services.list file into that directory adding all the modules
	 * you want to add 3. Then put all the services into services directory in
	 * the module 4. All the class is module can be access via a the module
	 * services.
	 */

	public static void deployModuleServices(AxisModule module,
			ConfigurationContext configCtx) throws AxisFault {
		try {
			AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
			ArchiveReader archiveReader = new ArchiveReader();
			PhasesInfo phasesInfo = axisConfig.getPhasesInfo();
			final ClassLoader moduleClassLoader = module.getModuleClassLoader();
			ArrayList services = new ArrayList();
			final InputStream in = (InputStream) org.apache.axis2.java.security.AccessController
					.doPrivileged(new PrivilegedAction() {
						public Object run() {
							return moduleClassLoader
									.getResourceAsStream("aars/aars.list");
						}
					});
			if (in != null) {
				BufferedReader input;
				try {
					input = new BufferedReader(
							(InputStreamReader) org.apache.axis2.java.security.AccessController
									.doPrivileged(new PrivilegedAction() {
										public Object run() {
											return new InputStreamReader(in);
										}
									}));
					String line;
					while ((line = input.readLine()) != null) {
						line = line.trim();
						if (line.length() > 0 && line.charAt(0) != '#') {
							services.add(line);
						}
					}
					input.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			if (services.size() > 0) {
				for (int i = 0; i < services.size(); i++) {
					final String servicename = (String) services.get(i);
					if (servicename == null || "".equals(servicename)) {
						continue;
					}
					InputStream fin = (InputStream) org.apache.axis2.java.security.AccessController
							.doPrivileged(new PrivilegedAction() {
								public Object run() {
									return moduleClassLoader
											.getResourceAsStream("aars/"
													+ servicename);
								}
							});
					if (fin == null) {
						throw new AxisFault("No service archive found : "
								+ servicename);
					}
					File inputFile = Utils
							.createTempFile(
									servicename,
									fin,
									(File) axisConfig
											.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR));
					DeploymentFileData filedata = new DeploymentFileData(
							inputFile);

					filedata
							.setClassLoader(
									false,
									moduleClassLoader,
									(File) axisConfig
											.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR));
					HashMap wsdlservice = archiveReader.processWSDLs(filedata);
					if (wsdlservice != null && wsdlservice.size() > 0) {
						Iterator servicesitr = wsdlservice.values().iterator();
						while (servicesitr.hasNext()) {
							AxisService service = (AxisService) servicesitr
									.next();
							Iterator operations = service.getOperations();
							while (operations.hasNext()) {
								AxisOperation axisOperation = (AxisOperation) operations
										.next();
								phasesInfo.setOperationPhases(axisOperation);
							}
						}
					}
					AxisServiceGroup serviceGroup = new AxisServiceGroup(
							axisConfig);
					serviceGroup.setServiceGroupClassLoader(filedata
							.getClassLoader());
					ArrayList serviceList = archiveReader.processServiceGroup(
							filedata.getAbsolutePath(), filedata, serviceGroup,
							false, wsdlservice, configCtx);
					for (int j = 0; j < serviceList.size(); j++) {
						AxisService axisService = (AxisService) serviceList
								.get(j);
						Parameter moduleService = new Parameter();
						moduleService.setValue("true");
						moduleService.setName(AxisModule.MODULE_SERVICE);
						axisService.addParameter(moduleService);
						serviceGroup.addService(axisService);
					}
					axisConfig.addServiceGroup(serviceGroup);
					fin.close();
				}
			}
		} catch (IOException e) {
			throw AxisFault.makeFault(e);
		}
	}

	/**
	 * Normalize a uri containing ../ and ./ paths.
	 * 
	 * @param uri
	 *            The uri path to normalize
	 * @return The normalized uri
	 */
	public static String normalize(String uri) {
		if ("".equals(uri)) {
			return uri;
		}
		int leadingSlashes = 0;
		for (leadingSlashes = 0; leadingSlashes < uri.length()
				&& uri.charAt(leadingSlashes) == '/'; ++leadingSlashes) {
			// FIXME: this block is empty!!
		}
		boolean isDir = (uri.charAt(uri.length() - 1) == '/');
		StringTokenizer st = new StringTokenizer(uri, "/");
		LinkedList clean = new LinkedList();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if ("..".equals(token)) {
				if (!clean.isEmpty() && !"..".equals(clean.getLast())) {
					clean.removeLast();
					if (!st.hasMoreTokens()) {
						isDir = true;
					}
				} else {
					clean.add("..");
				}
			} else if (!".".equals(token) && !"".equals(token)) {
				clean.add(token);
			}
		}
		StringBuffer sb = new StringBuffer();
		while (leadingSlashes-- > 0) {
			sb.append('/');
		}
		for (Iterator it = clean.iterator(); it.hasNext();) {
			sb.append(it.next());
			if (it.hasNext()) {
				sb.append('/');
			}
		}
		if (isDir && sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
			sb.append('/');
		}
		return sb.toString();
	}

	public static String getPath(String parent, String childPath) {
		Stack parentStack = new Stack();
		Stack childStack = new Stack();
		if (parent != null) {
			String[] values = parent.split("/");
			if (values.length > 0) {
				for (int i = 0; i < values.length; i++) {
					String value = values[i];
					parentStack.push(value);
				}
			}
		}
		String[] values = childPath.split("/");
		if (values.length > 0) {
			for (int i = 0; i < values.length; i++) {
				String value = values[i];
				childStack.push(value);
			}
		}
		String filepath = "";
		while (!childStack.isEmpty()) {
			String value = (String) childStack.pop();
			if ("..".equals(value)) {
				parentStack.pop();
			} else if (!"".equals(value)) {
				if ("".equals(filepath)) {
					filepath = value;
				} else {
					filepath = value + "/" + filepath;
				}
			}
		}
		while (!parentStack.isEmpty()) {
			String value = (String) parentStack.pop();
			if (!"".equals(value)) {
				filepath = value + "/" + filepath;
			}
		}
		return filepath;
	}

	/**
	 * Searches for jar files inside /lib dirctory. If there are any, the names
	 * of those jar files will be added to the array list
	 */
	public static List findLibJars(URL url) {
		ArrayList embedded_jars = new ArrayList();
		try {
			ZipInputStream zin = new ZipInputStream(url.openStream());
			ZipEntry entry;
			String entryName = "";
			while ((entry = zin.getNextEntry()) != null) {
				entryName = entry.getName();
				/**
				 * if the entry name start with /lib and ends with .jar add it
				 * to the the arraylist
				 */
				if (entryName != null
						&& (entryName.startsWith("lib/") || entryName
								.startsWith("Lib/"))
						&& entryName.endsWith(".jar")) {
					embedded_jars.add(entryName);
				}
			}
			zin.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return embedded_jars;
	}

	/**
	 * To add the exclude method when generating schemas , here the exclude
	 * methods will be session releated axis2 methods
	 */
	public static void addExcludeMethods(ArrayList excludeList) {
		excludeList.add("init");
		excludeList.add("setOperationContext");
		excludeList.add("startUp");
		excludeList.add("destroy");
		excludeList.add("shutDown");
	}

	public static DeploymentClassLoader createClassLoader(File serviceFile)
			throws MalformedURLException {
		ClassLoader contextClassLoader = (ClassLoader) org.apache.axis2.java.security.AccessController
				.doPrivileged(new PrivilegedAction() {
					public Object run() {
						return Thread.currentThread().getContextClassLoader();
					}
				});
		return createDeploymentClassLoader(new URL[] { serviceFile.toURL() },
				contextClassLoader, new ArrayList());
	}

	public static ClassLoader createClassLoader(ArrayList urls,
			ClassLoader serviceClassLoader, boolean extractJars, File tmpDir) {
		URL url = (URL) urls.get(0);
		if (extractJars) {
			try {
				URL[] urls1 = Utils.getURLsForAllJars(url, tmpDir);
				urls.remove(0);
				urls.addAll(0, Arrays.asList(urls1));
				URL[] urls2 = (URL[]) urls.toArray(new URL[urls.size()]);
				return createDeploymentClassLoader(urls2, serviceClassLoader,
						null);
			} catch (Exception e) {
				log
						.warn("Exception extracting jars into temporary directory : "
								+ e.getMessage()
								+ " : switching to alternate class loading mechanism");
				log.debug(e.getMessage(), e);
			}
		}
		List embedded_jars = Utils.findLibJars(url);
		URL[] urls2 = (URL[]) urls.toArray(new URL[urls.size()]);
		return createDeploymentClassLoader(urls2, serviceClassLoader,
				embedded_jars);
	}

	public static File toFile(URL url) throws UnsupportedEncodingException {
		String path = URLDecoder.decode(url.getPath(), defaultEncoding);
		File file = new File(path.replace('/', File.separatorChar).replace('|',
				':'));
		return file;
	}

	public static ClassLoader createClassLoader(URL[] urls,
			ClassLoader serviceClassLoader, boolean extractJars, File tmpDir) {
		if (extractJars) {
			try {
				URL[] urls1 = Utils.getURLsForAllJars(urls[0], tmpDir);
				return createDeploymentClassLoader(urls1, serviceClassLoader,
						null);
			} catch (Exception e) {
				log
						.warn("Exception extracting jars into temporary directory : "
								+ e.getMessage()
								+ " : switching to alternate class loading mechanism");
				log.debug(e.getMessage(), e);
			}
		}
		List embedded_jars = Utils.findLibJars(urls[0]);
		return createDeploymentClassLoader(urls, serviceClassLoader,
				embedded_jars);
	}

	private static DeploymentClassLoader createDeploymentClassLoader(
			final URL[] urls, final ClassLoader serviceClassLoader,
			final List embeddedJars) {
		return (DeploymentClassLoader) AccessController
				.doPrivileged(new PrivilegedAction() {
					public Object run() {
						return new DeploymentClassLoader(urls, embeddedJars,
								serviceClassLoader);
					}
				});
	}

	/**
	 * This method is to process bean exclude parameter and the XML format of
	 * that would be <parameter name="beanPropertyRules"> <bean class="full
	 * qualified class name" excludeProperties="name,age"/>+ </parameter>
	 * 
	 * @param service ,
	 *            AxisService object
	 */
	public static void processBeanPropertyExclude(AxisService service) {
		Parameter excludeBeanProperty = service
				.getParameter("beanPropertyRules");
		if (excludeBeanProperty != null) {
			OMElement parameterElement = excludeBeanProperty
					.getParameterElement();
			Iterator bneasItr = parameterElement.getChildrenWithName(new QName(
					"bean"));
			ExcludeInfo excludeInfo = new ExcludeInfo();
			while (bneasItr.hasNext()) {
				OMElement bean = (OMElement) bneasItr.next();
				String clazz = bean.getAttributeValue(new QName(
						DeploymentConstants.TAG_CLASS_NAME));
				String excludePropertees = bean.getAttributeValue(new QName(
						DeploymentConstants.TAG_EXCLUDE_PROPERTIES));
				String includeProperties = bean.getAttributeValue(new QName(
						DeploymentConstants.TAG_INCLUDE_PROPERTIES));
				excludeInfo.putBeanInfo(clazz, new BeanExcludeInfo(
						excludePropertees, includeProperties));
			}
			service.setExcludeInfo(excludeInfo);
		}
	}

	public static String getShortFileName(String filename) {
		File file = new File(filename);
		return file.getName();
	}

	/**
	 * The util method to prepare the JSR 181 annotated service name from given
	 * annotation or for defaults JSR 181 specifies that the in
	 * javax.jws.WebService the parameter serviceName contains the wsdl:service
	 * name to mapp. If its not available then the default will be Simple name
	 * of the class + "Service"
	 * 
	 * @return String version of the ServiceName according to the JSR 181 spec
	 */
	public static String getAnnotatedServiceName(Class serviceClass,
			WebService serviceAnnotation) {
		String serviceName = "";
		if (serviceAnnotation.serviceName() != null) {
			serviceName = serviceAnnotation.serviceName();
		}
		if (serviceName.equals("")) {
			serviceName = serviceClass.getName();
			int firstChar = serviceName.lastIndexOf('.') + 1;
			if (firstChar > 0) {
				serviceName = serviceName.substring(firstChar);
			}
			serviceName += "Service";
		}
		return serviceName;
	}

	public static void addEndpointsToService(AxisService axisService)
			throws AxisFault {

		String serviceName = axisService.getName();
		Iterator transportInValues = null;

		if (axisService.isEnableAllTransports()) {
			AxisConfiguration axisConfiguration = axisService
					.getAxisConfiguration();
			if (axisConfiguration != null) {
				ArrayList transports = new ArrayList();
				for (Iterator iterator = axisConfiguration.getTransportsIn()
						.values().iterator(); iterator.hasNext();) {
					TransportInDescription transportInDescription = (TransportInDescription) iterator
							.next();
					transports.add(transportInDescription.getName());
				}
				transportInValues = transports.iterator();
			}
		} else {
			transportInValues = axisService.getExposedTransports().iterator();
		}

		HashMap bindingCache = new HashMap();

		if (transportInValues != null) {
			for (; transportInValues.hasNext();) {
				String transportName = (String) transportInValues.next();
				String protocol = transportName.substring(0, 1).toUpperCase()
						+ transportName.substring(1, transportName.length())
								.toLowerCase();
				/*
				 * populates soap11 endpoint
				 */
				String soap11EndpointName = serviceName + protocol
						+ "Soap11Endpoint";

				AxisEndpoint httpSoap11Endpoint = new AxisEndpoint();
				httpSoap11Endpoint.setName(soap11EndpointName);
				httpSoap11Endpoint.setParent(axisService);
				httpSoap11Endpoint.setTransportInDescription(transportName);
				populateSoap11Endpoint(axisService, httpSoap11Endpoint,
						bindingCache);
				axisService.addEndpoint(httpSoap11Endpoint.getName(),
						httpSoap11Endpoint);
				// setting soap11 endpoint as the default endpoint
				axisService.setEndpointName(soap11EndpointName);

				/*
				 * generating Soap12 endpoint
				 */
				String soap12EndpointName = serviceName + protocol
						+ "Soap12Endpoint";
				AxisEndpoint httpSoap12Endpoint = new AxisEndpoint();
				httpSoap12Endpoint.setName(soap12EndpointName);
				httpSoap12Endpoint.setParent(axisService);
				httpSoap12Endpoint.setTransportInDescription(transportName);
				populateSoap12Endpoint(axisService, httpSoap12Endpoint,
						bindingCache);
				axisService.addEndpoint(httpSoap12Endpoint.getName(),
						httpSoap12Endpoint);

				/*
				 * generating Http endpoint
				 */
				if ("http".equals(transportName)) {
					String httpEndpointName = serviceName + protocol
							+ "Endpoint";
					AxisEndpoint httpEndpoint = new AxisEndpoint();
					httpEndpoint.setName(httpEndpointName);
					httpEndpoint.setParent(axisService);
					httpEndpoint.setTransportInDescription(transportName);
					populateHttpEndpoint(axisService, httpEndpoint, bindingCache);
					axisService.addEndpoint(httpEndpoint.getName(),
							httpEndpoint);
				}
			}
		}
	}

	public static void addEndpointsToService(AxisService axisService,
			AxisConfiguration axisConfiguration) throws AxisFault {

		String serviceName = axisService.getName();
		Iterator transportInValues = null;

		if (axisConfiguration != null) {
			ArrayList transports = new ArrayList();
			for (Iterator iterator = axisConfiguration.getTransportsIn()
					.values().iterator(); iterator.hasNext();) {
				TransportInDescription transportInDescription = (TransportInDescription) iterator
						.next();
				transports.add(transportInDescription.getName());
			}
			transportInValues = transports.iterator();
		}

		HashMap bindingCache = new HashMap();
		if (transportInValues != null) {
			for (; transportInValues.hasNext();) {
				String transportName = (String) transportInValues.next();
				String protocol = transportName.substring(0, 1).toUpperCase()
						+ transportName.substring(1, transportName.length())
								.toLowerCase();
				/*
				 * populates soap11 endpoint
				 */
				String soap11EndpointName = serviceName + protocol
						+ "Soap11Endpoint";

				AxisEndpoint httpSoap11Endpoint = new AxisEndpoint();
				httpSoap11Endpoint.setName(soap11EndpointName);
				httpSoap11Endpoint.setParent(axisService);
				httpSoap11Endpoint.setTransportInDescription(transportName);
				populateSoap11Endpoint(axisService, httpSoap11Endpoint,
						bindingCache);
				axisService.addEndpoint(httpSoap11Endpoint.getName(),
						httpSoap11Endpoint);
				// setting soap11 endpoint as the default endpoint
				axisService.setEndpointName(soap11EndpointName);

				/*
				 * generating Soap12 endpoint
				 */
				String soap12EndpointName = serviceName + protocol
						+ "Soap12Endpoint";
				AxisEndpoint httpSoap12Endpoint = new AxisEndpoint();
				httpSoap12Endpoint.setName(soap12EndpointName);
				httpSoap12Endpoint.setParent(axisService);
				httpSoap12Endpoint.setTransportInDescription(transportName);
				populateSoap12Endpoint(axisService, httpSoap12Endpoint,
						bindingCache);
				axisService.addEndpoint(httpSoap12Endpoint.getName(),
						httpSoap12Endpoint);

				/*
				 * generating Http endpoint
				 */
				if ("http".equals(transportName)
						|| "https".equals(transportName)) {
					String httpEndpointName = serviceName + protocol
							+ "Endpoint";
					AxisEndpoint httpEndpoint = new AxisEndpoint();
					httpEndpoint.setName(httpEndpointName);
					httpEndpoint.setParent(axisService);
					httpEndpoint.setTransportInDescription(transportName);
					populateHttpEndpoint(axisService, httpEndpoint, bindingCache);
					axisService.addEndpoint(httpEndpoint.getName(),
							httpEndpoint);
				}
			}
		}
	}

	public static void addSoap11Endpoint(AxisService axisService, URL url)
			throws Exception {
		String protocol = url.getProtocol();
		protocol = protocol.substring(0, 1).toUpperCase()
				+ protocol.substring(1, protocol.length()).toLowerCase();

		String serviceName = axisService.getName();
		String soap11EndpointName = serviceName + protocol + "Soap11Endpoint";

		AxisEndpoint httpSoap11Endpoint = new AxisEndpoint();
		httpSoap11Endpoint.setName(soap11EndpointName);
		httpSoap11Endpoint.setParent(axisService);
		httpSoap11Endpoint.setEndpointURL(url.toString());
		httpSoap11Endpoint.setTransportInDescription(url.getProtocol());

		populateSoap11Endpoint(axisService, httpSoap11Endpoint, null);
		axisService.addEndpoint(httpSoap11Endpoint.getName(),
				httpSoap11Endpoint);
		// setting soap11 endpoint as the default endpoint
		axisService.setEndpointName(soap11EndpointName);
	}

	public static void addSoap12Endpoint(AxisService axisService, URL url)
			throws Exception {
		String protocol = url.getProtocol();
		protocol = protocol.substring(0, 1).toUpperCase()
				+ protocol.substring(1, protocol.length()).toLowerCase();

		String serviceName = axisService.getName();
		String soap12EndpointName = serviceName + protocol + "Soap12Endpoint";

		AxisEndpoint httpSoap12Endpoint = new AxisEndpoint();
		httpSoap12Endpoint.setName(soap12EndpointName);
		httpSoap12Endpoint.setParent(axisService);
		httpSoap12Endpoint.setEndpointURL(url.toString());
		httpSoap12Endpoint.setTransportInDescription(url.getProtocol());

		populateSoap12Endpoint(axisService, httpSoap12Endpoint, null);
		axisService.addEndpoint(httpSoap12Endpoint.getName(),
				httpSoap12Endpoint);
	}

	public static void addHttpEndpoint(AxisService axisService, URL url) {
		String serviceName = axisService.getName();
		String protocol = url.getProtocol();
		protocol = protocol.substring(0, 1).toUpperCase()
				+ protocol.substring(1, protocol.length()).toLowerCase();

		String httpEndpointName = serviceName + protocol + "Endpoint";
		AxisEndpoint httpEndpoint = new AxisEndpoint();
		httpEndpoint.setName(httpEndpointName);
		httpEndpoint.setParent(axisService);
		httpEndpoint.setEndpointURL(url.toString());
		httpEndpoint.setTransportInDescription(url.getProtocol());
		populateHttpEndpoint(axisService, httpEndpoint, null);
		axisService.addEndpoint(httpEndpoint.getName(), httpEndpoint);
	}

	public static void processPolicyAttachments(Iterator attachmentElements,
			AxisService service) throws XMLStreamException,
			FactoryConfigurationError {
		OMElement attachmentElement;
		HashMap attachmentsMap = new HashMap();

		for (; attachmentElements.hasNext();) {
			attachmentElement = (OMElement) attachmentElements.next();
			OMElement appliesToElem = attachmentElement
					.getFirstChildWithName(new QName(
							DeploymentConstants.POLICY_NS_URI,
							DeploymentConstants.TAG_APPLIES_TO));
			ArrayList policyComponents = new ArrayList();

			// process <wsp:Policy> elements ..
			for (Iterator policyElements = attachmentElement
					.getChildrenWithName(new QName(
							DeploymentConstants.POLICY_NS_URI,
							DeploymentConstants.TAG_POLICY)); policyElements
					.hasNext();) {
				PolicyComponent policy = PolicyUtil
						.getPolicyFromOMElement((OMElement) policyElements
								.next());
				policyComponents.add(policy);
			}

			// process <wsp:PolicyReference> elements ..
			for (Iterator policyRefElements = attachmentElement
					.getChildrenWithName(new QName(
							DeploymentConstants.POLICY_NS_URI,
							DeploymentConstants.TAG_POLICY_REF)); policyRefElements
					.hasNext();) {
				PolicyComponent policyRef = PolicyUtil
						.getPolicyReferenceFromOMElement((OMElement) policyRefElements
								.next());
				policyComponents.add(policyRef);
			}

			for (Iterator policySubjects = appliesToElem
					.getChildrenWithName(new QName("policy-subject")); policySubjects
					.hasNext();) {
				OMElement policySubject = (OMElement) policySubjects.next();
				String identifier = policySubject.getAttributeValue(new QName(
						"identifier"));

				ArrayList values = (ArrayList) attachmentsMap.get(identifier);
				if (values == null) {
					values = new ArrayList();
					attachmentsMap.put(identifier, values);
				}
				values.addAll(policyComponents);
			}
		}

		for (Iterator keys = attachmentsMap.keySet().iterator(); keys.hasNext();) {
			String identifier = (String) keys.next();
			if (identifier.startsWith("binding:soap")) {
				processSoapAttachments(identifier, (List) attachmentsMap
						.get(identifier), service);
			}
		}
	}

	private static void populateSoap11Endpoint(AxisService axisService,
			AxisEndpoint axisEndpoint, HashMap bindingCache) {
		String serviceName = axisService.getName();
		String name = serviceName + "Soap11Binding";

		QName bindingName = new QName(name);

		AxisBinding axisBinding = (bindingCache != null) ? (AxisBinding) bindingCache
				.get(name)
				: null;
		if (axisBinding == null) {
			axisBinding = new AxisBinding();
			axisBinding.setName(bindingName);

			axisBinding.setType(Java2WSDLConstants.TRANSPORT_URI);
			axisBinding.setProperty(WSDLConstants.WSDL_1_1_STYLE,
					WSDLConstants.STYLE_DOC);

			axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
					SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

			for (Iterator iterator = axisService.getChildren(); iterator
					.hasNext();) {
				AxisOperation operation = (AxisOperation) iterator.next();
				AxisBindingOperation axisBindingOperation = new AxisBindingOperation();

				axisBindingOperation.setName(operation.getName());
				axisBindingOperation.setAxisOperation(operation);

				String soapAction = operation.getSoapAction();
				if (soapAction != null) {
					axisBindingOperation.setProperty(
							WSDL2Constants.ATTR_WSOAP_ACTION, soapAction);
				}
				axisBinding.addChild(axisBindingOperation.getName(),
						axisBindingOperation);
				populateBindingOperation(axisService, axisBinding,
						axisBindingOperation);
			}
			if (bindingCache != null) {
				bindingCache.put(name, axisBinding);
			}
		}
		// axisBinding.setParent(axisEndpoint);
		axisEndpoint.setBinding(axisBinding);
	}

	private static void populateSoap12Endpoint(AxisService axisService,
			AxisEndpoint axisEndpoint, HashMap bindingCache) {
		String serviceName = axisService.getName();
		String name = serviceName + "Soap12Binding";

		QName bindingName = new QName(name);

		AxisBinding axisBinding = (bindingCache != null) ? (AxisBinding) bindingCache
				.get(name)
				: null;
		if (axisBinding == null) {
			axisBinding = new AxisBinding();
			axisBinding.setName(bindingName);

			axisBinding.setType(Java2WSDLConstants.TRANSPORT_URI);
			axisBinding.setProperty(WSDLConstants.WSDL_1_1_STYLE,
					WSDLConstants.STYLE_DOC);

			axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
					SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

			for (Iterator iterator = axisService.getChildren(); iterator
					.hasNext();) {
				AxisOperation operation = (AxisOperation) iterator.next();
				AxisBindingOperation axisBindingOperation = new AxisBindingOperation();

				axisBindingOperation.setName(operation.getName());
				axisBindingOperation.setAxisOperation(operation);

				String soapAction = operation.getSoapAction();
				if (soapAction != null) {
					axisBindingOperation.setProperty(
							WSDL2Constants.ATTR_WSOAP_ACTION, soapAction);
				}
				axisBinding.addChild(axisBindingOperation.getName(),
						axisBindingOperation);

				populateBindingOperation(axisService, axisBinding,
						axisBindingOperation);
			}
			if (bindingCache != null) {
				bindingCache.put(name, axisBinding);
			}
		}
		axisEndpoint.setBinding(axisBinding);
	}

	private static void populateHttpEndpoint(AxisService axisService,
			AxisEndpoint axisEndpoint, HashMap bindingCache) {
		String serviceName = axisService.getName();
		String name = serviceName + "HttpBinding";

		QName bindingName = new QName(name);

		AxisBinding axisBinding = (bindingCache != null) ? (AxisBinding) bindingCache
				.get(name)
				: null;

		if (axisBinding == null) {
			axisBinding = new AxisBinding();
			axisBinding.setName(bindingName);

			axisBinding.setType(WSDL2Constants.URI_WSDL2_HTTP);
			axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD, "POST");

			for (Iterator iterator = axisService.getChildren(); iterator
					.hasNext();) {
				AxisOperation operation = (AxisOperation) iterator.next();
				AxisBindingOperation axisBindingOperation = new AxisBindingOperation();

                QName operationQName = operation.getName();
                axisBindingOperation.setName(operationQName);
				axisBindingOperation.setAxisOperation(operation);
                String httpLocation = serviceName + "/" + operationQName.getLocalPart();
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocation);
                axisBinding.addChild(axisBindingOperation.getName(),
						axisBindingOperation);

				populateBindingOperation(axisService, axisBinding,
						axisBindingOperation);
			}
			if (bindingCache != null) {
				bindingCache.put(name, axisBinding);
			}
		}
		axisEndpoint.setBinding(axisBinding);
	}

	private static void populateBindingOperation(AxisService axisService,
			AxisBinding axisBinding, AxisBindingOperation axisBindingOperation) {

		AxisOperation axisOperation = axisBindingOperation.getAxisOperation();

		if (WSDLUtil.isInputPresentForMEP(axisOperation
				.getMessageExchangePattern())) {
			AxisMessage axisInMessage = axisOperation
					.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			AxisBindingMessage axisBindingInMessage = new AxisBindingMessage();

			axisBindingInMessage.setName(axisInMessage.getName());
			axisBindingInMessage.setDirection(axisInMessage.getDirection());
			axisBindingInMessage.setAxisMessage(axisInMessage);

			axisBindingInMessage.setParent(axisBindingOperation);
			axisBindingOperation.addChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE,
					axisBindingInMessage);
		}

		if (WSDLUtil.isOutputPresentForMEP(axisOperation
				.getMessageExchangePattern())) {
			AxisMessage axisOutMessage = axisOperation
					.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
			AxisBindingMessage axisBindingOutMessage = new AxisBindingMessage();

			axisBindingOutMessage.setName(axisOutMessage.getName());
			axisBindingOutMessage.setDirection(axisOutMessage.getDirection());
			axisBindingOutMessage.setAxisMessage(axisOutMessage);

			axisBindingOutMessage.setParent(axisBindingOperation);
			axisBindingOperation.addChild(
					WSDLConstants.MESSAGE_LABEL_OUT_VALUE,
					axisBindingOutMessage);
		}

		ArrayList faultMessagesList = axisOperation.getFaultMessages();
		for (Iterator iterator2 = faultMessagesList.iterator(); iterator2
				.hasNext();) {
			AxisMessage axisFaultMessage = (AxisMessage) iterator2.next();
			AxisBindingMessage axisBindingFaultMessage = new AxisBindingMessage();
            axisBindingFaultMessage.setName(axisFaultMessage.getName());
            axisBindingFaultMessage.setFault(true);
			axisBindingFaultMessage.setAxisMessage(axisFaultMessage);
			axisBindingFaultMessage.setParent(axisBindingOperation);
			axisBindingOperation.addFault(axisBindingFaultMessage);
            axisBinding.addFault(axisBindingFaultMessage);
        }

		axisBindingOperation.setAxisOperation(axisOperation);
		axisBindingOperation.setParent(axisBinding);
	}

	private static void processSoapAttachments(String identifier,
			List policyComponents, AxisService service) {
		Map map = service.getEndpoints();
		String soapVersion = (identifier.indexOf("soap12") > -1) ? SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI
				: SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;

		for (Iterator iterator = map.values().iterator(); iterator.hasNext();) {
			AxisEndpoint axisEndpoint = (AxisEndpoint) iterator.next();
			AxisBinding axisBinding = axisEndpoint.getBinding();
			String wsoap = (String) axisBinding
					.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
			if (soapVersion.equals(wsoap)) {
				String[] identifiers = identifier.split("/");
				int key = identifiers.length;
				if (key == 1) {
					axisBinding.getPolicySubject().attachPolicyComponents(
							policyComponents);
				} else if (key == 2 || key == 3) {
					String opName = identifiers[1];
					opName = opName.substring(opName.indexOf(":") + 1, opName
							.length());
					AxisBindingOperation bindingOperation = null;
					boolean found = false;
					for (Iterator i = axisBinding.getChildren(); i.hasNext();) {
						bindingOperation = (AxisBindingOperation) i.next();
						if (opName.equals(bindingOperation.getName()
								.getLocalPart())) {
							found = true;
							break;
						}
					}
					if (!found) {
						throw new IllegalArgumentException(
								"Invalid binding operation " + opName);
					}

					if (key == 2) {
						bindingOperation.getPolicySubject()
								.attachPolicyComponents(policyComponents);
					} else {
						if ("in".equals(identifiers[2])) {
							AxisBindingMessage bindingInMessage = (AxisBindingMessage) bindingOperation
									.getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
							bindingInMessage.getPolicySubject()
									.attachPolicyComponents(policyComponents);

						} else if ("out".equals(identifiers[2])) {
							AxisBindingMessage bindingOutMessage = (AxisBindingMessage) bindingOperation
									.getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
							bindingOutMessage.getPolicySubject()
									.attachPolicyComponents(policyComponents);
						} else {
							// FIXME faults
						}
					}
				}
				break;
			}
		}
	}
}
