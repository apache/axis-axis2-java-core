/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.ws.commons.schema.XmlSchemaElement;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

public class JiBXExtension extends AbstractDBProcessingExtension {

    public static final String BINDING_PATH_OPTION = "bindingfile";
    public static final String UNWRAP_OPTION = "unwrap";
    public static final String JIBX_MODEL_CLASS =
            "org.jibx.binding.model.BindingElement";
    public static final String JIBX_UTILITY_CLASS =
            "org.apache.axis2.jibx.CodeGenerationUtility";
    public static final String BINDING_MAP_METHOD = "getBindingMap";

    public void engage(CodeGenConfiguration configuration) {

        // just return if JiBX binding not active
        if (testFallThrough(configuration.getDatabindingType())) {
            return;
        }

        // check the JiBX binding definition file specified
        String path = (String)configuration.getProperties().get(BINDING_PATH_OPTION);
        if (path == null) {
            throw new RuntimeException("jibx binding option requires -" +
                    BINDING_PATH_OPTION + " {file path} parameter");
        }
        try {

            // try dummy load of framework class first to check missing jars
            try {
                getClass().getClassLoader().loadClass(JIBX_MODEL_CLASS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("JiBX framework jars not in classpath");
            }

            // load the actual utility class
            Class clazz;
            try {
                clazz = JiBXExtension.class.getClassLoader().loadClass(JIBX_UTILITY_CLASS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("JiBX binding extension not in classpath");
            }
            
            // check if unwrapping message elements
            boolean unwrap = configuration.getProperties().containsKey(UNWRAP_OPTION);

            // get all elements for operations, and matching type definitions
            HashMap defsmap = new HashMap();
            Iterator operations = configuration.getAxisService().getOperations();
            while (operations.hasNext()) {
                AxisOperation o =  (AxisOperation)operations.next();
                accumulateElements(o, defsmap, configuration);
            }
            
            // invoke utility class method for actual processing
            Method method = clazz.getMethod(BINDING_MAP_METHOD,
                new Class[] { String.class, HashMap.class, boolean.class });
            TypeMapper mapper = (TypeMapper)method.invoke(null,
                new Object[] { path, defsmap, Boolean.valueOf(unwrap) });

            // set the type mapper to the config
            configuration.setTypeMapper(mapper);

/*            // invoke utility class method for actual processing
            Method method = clazz.getMethod(BINDING_MAP_METHOD,
                    new Class[] { String.class });
            HashMap jibxmap = (HashMap)method.invoke(null, new Object[] { path });

            // Want to find all elements by working down from bindings (if any
            // supplied) or interfaces (if no bindings). Not sure why this dual
            // path is required, but based on the code in
            // org.apache.axis2.wsdl.builder.SchemaUnwrapper
            HashSet elements = new HashSet();
            Iterator operations = configuration.getAxisService().getOperations();
            while (operations.hasNext()) {
                AxisOperation o =  (AxisOperation)operations.next();
                accumulateElements(o, elements);
            }


            //create the type mapper
            //First try to take the one that is already there
            TypeMapper mapper = configuration.getTypeMapper();
            if (mapper==null){
                mapper =new JavaTypeMapper();
            }

            for (Iterator iter = elements.iterator(); iter.hasNext();) {
                QName qname = (QName)iter.next();
                if (qname != null) {
                    String cname = (String)jibxmap.get(qname);
                    if (cname == null) {
                        throw new RuntimeException("No JiBX mapping defined for " + qname);
                    }
                    mapper.addTypeMappingName(qname, cname);
                }
            }

            // set the type mapper to the config
            configuration.setTypeMapper(mapper);	*/

        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Accumulate the QNames of all message elements used by an interface. Based on
     *
     * @param op
     * @param config 
     * @param elements
     */
    private void accumulateElements(AxisOperation op, HashMap defsmap, CodeGenConfiguration config) {
        String MEP = op.getMessageExchangePattern();
        if (WSDLUtil.isInputPresentForMEP(op.getMessageExchangePattern())) {
            AxisMessage msg = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (msg != null) {
                addElementType(msg, defsmap, config);
            }
        }

        if (WSDLUtil.isOutputPresentForMEP(op.getMessageExchangePattern())) {
            AxisMessage msg = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            if (msg != null) {
                addElementType(msg, defsmap, config);
            }
        }
    }

    /**
     * Add message element information to schema definition mappings.
     * 
     * @param msg
     * @param defsmap
     * @param configuration 
     */
    private void addElementType(AxisMessage msg, HashMap defsmap, CodeGenConfiguration config) {
        QName qname = msg.getElementQName();
        XmlSchemaElement element =
            config.getAxisService().getSchemaElement(qname);
        if (qname != null) {
            defsmap.put(qname, element);
        }
    }
}