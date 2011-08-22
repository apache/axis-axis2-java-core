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

package org.apache.ws.java2wsdl.jaxws;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.util.LogOutputStream;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOption;

import com.sun.tools.ws.wscompile.WsgenTool;

/**
 * The Class JAXWS2WSDLCodegenEngine.
 */
public class JAXWS2WSDLCodegenEngine {

    /** The Constant log. */
    private static final Log log = LogFactory
            .getLog(JAXWS2WSDLCodegenEngine.class);

    /** The options map. */
    private Map<String, Java2WSDLCommandLineOption> optionsMap;

    /** The original args. */
    private String[] originalArgs;

    /**
     * Instantiates a new jAXW s2 wsdl codegen engine.
     * 
     * @param optionsMap
     *            the options map
     * @param originalArgs
     *            the original args
     */
    public JAXWS2WSDLCodegenEngine(
            Map<String, Java2WSDLCommandLineOption> optionsMap,
            String[] originalArgs) {
        this.optionsMap = optionsMap;
        this.originalArgs = originalArgs;
    }

    /**
     * Generate.
     * 
     * @throws Exception
     *             the exception
     */
    public void generate() throws Exception {
        LogOutputStream logOutputStream = new LogOutputStream(log,
                Integer.MAX_VALUE);
        WsgenTool genTool = new WsgenTool(logOutputStream);

        List<String> args = new ArrayList<String>();
        configurImportToolOptions(args);
        mergeOriginalArgs(args);
        boolean success = genTool.run(args.toArray(new String[args.size()]));
        if (success) {
            log.info("Code generation completed");
        }

    }

    /**
     * Configur import tool options.
     * 
     * @param args
     *            the args
     */
    private void configurImportToolOptions(List<String> args) {

        // Set some default options
        args.add(WS_GEN_EXTENSION);
        args.add(WS_GEN_NO_COMPILE);
        args.add(WS_GEN_KEEP_FILE);
        args.add(WS_GEN_WSDL);

        // Set some properties based on AXIS2 JAVA2WSDL options
        String location = getOptionValue(optionsMap,
                Java2WSDLConstants.OUTPUT_LOCATION_OPTION);
        if (location == null) {
            location = getOptionValue(optionsMap,
                    Java2WSDLConstants.OUTPUT_LOCATION_OPTION_LONG);
        }
        if (location != null) {
            args.add(WS_GEN_FILE_OUTPUT_DIR);
            args.add(location);
        }

        String classPath = getOptionValue(optionsMap,
                Java2WSDLConstants.CLASSPATH_OPTION);
        if (classPath == null) {
            classPath = getOptionValue(optionsMap,
                    Java2WSDLConstants.CLASSPATH_OPTION_LONG);
        }
        if (classPath != null) {
            args.add(WS_GEN_CLASSPATH);
            args.add(classPath);
        }

        String className = getOptionValue(optionsMap,
                Java2WSDLConstants.CLASSNAME_OPTION);
        if (className == null) {
            className = getOptionValue(optionsMap,
                    Java2WSDLConstants.CLASSNAME_OPTION_LONG);
        }
        if (className != null) {
            args.add(className);
        }

    }

    /**
     * Merge original args.
     * 
     * @param args
     *            the args
     */
    private void mergeOriginalArgs(List<String> args) {

        List<String> axisOptionList = new ArrayList<String>();
        List<String> originalArgsOps = new ArrayList<String>(
                Arrays.asList(originalArgs));
        originalArgsOps.remove("-"
                .concat(Java2WSDLConstants.JAX_WS_SERVICE_OPTION));
        originalArgsOps.remove("-"
                .concat(Java2WSDLConstants.JAX_WS_SERVICE_OPTION_LONG));
        Field[] allFields = Java2WSDLConstants.class.getFields();
        Iterator<String> mapItr = optionsMap.keySet().iterator();
        for (Field field : allFields) {
            if (String.class.equals(field.getType())) {
                try {
                    axisOptionList.add((String) field
                            .get(Java2WSDLConstants.class));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        while (mapItr.hasNext()) {
            Java2WSDLCommandLineOption op = optionsMap.get(mapItr.next());
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
        log.debug("Original WSImport options" + Arrays.asList(originalArgsOps));
        log.debug("All WSImport options" + Arrays.asList(args));

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
        Java2WSDLCommandLineOption option = (Java2WSDLCommandLineOption) allOptions
                .get(optionName);
        if (option != null) {
            return option.getOptionValue().toString();
        }
        return null;
    }

    public static final String WS_GEN_FILE_OUTPUT_DIR = "-d";
    public static final String WS_GEN_CLASSPATH = "-cp";
    public static final String WS_GEN_KEEP_FILE = "-keep";
    public static final String WS_GEN_WSDL = "-wsdl";
    public static final String WS_GEN_EXTENSION = "-extension";
    public static final String WS_GEN_NO_COMPILE = "-Xnocompile";

}
