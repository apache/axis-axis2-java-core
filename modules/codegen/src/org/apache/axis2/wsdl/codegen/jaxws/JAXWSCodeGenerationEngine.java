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

package org.apache.axis2.wsdl.codegen.jaxws;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.util.LogOutputStream;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.tools.ws.wscompile.WsimportTool;

/**
 * The Class JAXWSCodeGenerationEngine.
 */
public class JAXWSCodeGenerationEngine {

    private static final Log log = LogFactory
            .getLog(JAXWSCodeGenerationEngine.class);

    private CodeGenConfiguration configuration;
    private CommandLineOptionParser commandLineOptionParser;
    private String[] originalArgs;

    /**
     * Instantiates a new jAXWS code generation engine.
     * 
     * @param configuration
     *            the configuration
     * @param originalArgs
     *            the original args
     * @throws CodeGenerationException
     *             the code generation exception
     */
    public JAXWSCodeGenerationEngine(CodeGenConfiguration configuration,
            String[] originalArgs) throws CodeGenerationException {
        this.configuration = configuration;
        this.originalArgs = originalArgs;
        // loadExtensions();
    }

    /**
     * Instantiates a new jAXWS code generation engine.
     * 
     * @param commandLineOptionParser
     *            the command line option parser
     * @param originalArgs
     *            the original args
     */
    public JAXWSCodeGenerationEngine(
            CommandLineOptionParser commandLineOptionParser,
            String[] originalArgs) {
        this.commandLineOptionParser = commandLineOptionParser;
        this.originalArgs = originalArgs;
        // loadExtensions();
    }

    /**
     * Generate.
     * 
     * @throws CodeGenerationException
     *             the code generation exception
     */
    public void generate() throws CodeGenerationException {

        LogOutputStream logOutputStream = new LogOutputStream(log,
                Integer.MAX_VALUE);
        WsimportTool importTool = new WsimportTool(logOutputStream);
        ArrayList<String> args = new ArrayList<String>();
        configurImportToolOptions(args);
        mergeOriginalArgs(args);
        boolean success = importTool.run(args.toArray(new String[args.size()]));
        if (success) {
            log.info("Code generation completed");
        }
    }

    /**
     * Merge original args.
     * 
     * @param args
     *            the args
     */
    private void mergeOriginalArgs(ArrayList<String> args) {
        Map<String, CommandLineOption> allOptions = commandLineOptionParser
                .getAllOptions();
        List<String> axisOptionList = new ArrayList<String>();
        List<String> originalArgsOps = new ArrayList<String>(
                Arrays.asList(originalArgs));
        originalArgsOps
                .remove("-"
                        .concat(CommandLineOptionConstants.WSDL2JavaConstants.JAX_WS_SERVICE_OPTION));
        originalArgsOps
                .remove("-"
                        .concat(CommandLineOptionConstants.WSDL2JavaConstants.JAX_WS_SERVICE_OPTION_LONG));
        Field[] allFields = CommandLineOptionConstants.WSDL2JavaConstants.class
                .getFields();
        Iterator<String> mapItr = allOptions.keySet().iterator();
        for (Field field : allFields) {
            if (String.class.equals(field.getType())) {
                try {
                    axisOptionList
                            .add((String) field
                                    .get(CommandLineOptionConstants.WSDL2JavaConstants.class));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        while (mapItr.hasNext()) {
            CommandLineOption op = allOptions.get(mapItr.next());
            if (axisOptionList.contains(op.getOptionType())) {
                if (op.getOptionType() != null) {
                    originalArgsOps.remove("-".concat(op.getOptionType()));
                }
                if (op.getOptionValue() != null) {
                    originalArgsOps.remove(op.getOptionValue());
                }

            }

        }
        args.addAll(originalArgsOps);
        log.info("Original WSImport options" + Arrays.asList(originalArgsOps));
        log.info("All WSImport options" + Arrays.asList(args));

    }

    /**
     * Configur import tool options.
     * 
     * @param args
     *            the args
     */
    private void configurImportToolOptions(ArrayList<String> args) {

        Map allOptions = commandLineOptionParser.getAllOptions();
        // Set some default options
        args.add(WS_IMPORT_EXTENSION);
        args.add(WS_IMPORT_NO_COMPILE);
        args.add(WS_IMPORT_KEEP_FILE);

        // Set some properties based on AXIS2 WSDL2JAVA options
        String uri = getOptionValue(
                allOptions,
                CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION);
        if (uri != null) {
            args.add(uri);
        }

        String location = getOptionValue(
                allOptions,
                CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION);
        if (location == null) {
            location = getOptionValue(
                    allOptions,
                    CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION_LONG);
        }
        if (location != null) {
            args.add(WS_IMPORT_FILE_OUTPUT_DIR);
            args.add(location);
        }

        String pkg = getOptionValue(allOptions,
                CommandLineOptionConstants.WSDL2JavaConstants.PACKAGE_OPTION);
        if (pkg == null) {
            pkg = getOptionValue(
                    allOptions,
                    CommandLineOptionConstants.WSDL2JavaConstants.PACKAGE_OPTION_LONG);
        }
        if (pkg != null) {
            args.add(WS_IMPORT_PKG);
            args.add(pkg);
        }

        String proxcyHost = getOptionValue(
                allOptions,
                CommandLineOptionConstants.WSDL2JavaConstants.HTTP_PROXY_HOST_OPTION_LONG);
        String proxcyPort = getOptionValue(
                allOptions,
                CommandLineOptionConstants.WSDL2JavaConstants.HTTP_PROXY_PORT_OPTION_LONG);
        if (pkg != null) {
            args.add(WS_IMPORT_PROXY);
            args.add(proxcyHost + ":" + proxcyPort);
        }

    }

    /**
     * Gets the option value.
     * 
     * @param allOptions
     *            the all options
     * @param optionName
     *            the option name
     * @return the option value
     */
    private static String getOptionValue(Map allOptions, String optionName) {
        CommandLineOption option = (CommandLineOption) allOptions
                .get(optionName);
        if (option != null) {
            return option.getOptionValue().toString();
        }
        return null;
    }

    public static final String WS_IMPORT_EXTENSION = "-extension";
    public static final String WS_IMPORT_NO_COMPILE = "-Xnocompile";
    public static final String WS_IMPORT_FILE_OUTPUT_DIR = "-d";
    public static final String WS_IMPORT_SOURCE_OUTPUT_DIR = "-s";
    public static final String WS_IMPORT_KEEP_FILE = "-keep";
    public static final String WS_IMPORT_PKG = "-p";
    public static final String WS_IMPORT_PROXY = "-httpproxy";

}
