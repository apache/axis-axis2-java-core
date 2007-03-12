/*
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

package org.apache.axis2.wsdl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.PortType;
import javax.wsdl.extensions.ExtensibilityElement;
import java.util.*;

/**
 * This class provides support for processing a WSDL4J defintion which includes imports.
 * It allows the imports to be processed into a single WSDL4J Definition object
 */
public class WSDL4JImportedWSDLHelper {

    protected static final Log log = LogFactory.getLog(WSDL4JImportedWSDLHelper.class);
    private static final boolean isTraceEnabled = log.isTraceEnabled();

    /**
     * The intention of this procedure is to process the imports. When
     * processing the imports the imported documents will be populating the
     * items in the main document recursivley
     *
     * @param wsdl4JDefinition
     */
    public static void processImports(Definition wsdl4JDefinition) {
        if (isTraceEnabled) {
            log.trace("processImports: wsdl4JDefinition=" + wsdl4JDefinition);
        }
        Map imported_defs = new HashMap();
        getImportedDefinitions(wsdl4JDefinition, imported_defs);


        for (Iterator iterator = imported_defs.values().iterator(); iterator.hasNext();) {
            Definition imported_def = (Definition) iterator.next();

            Map def_namespaces = wsdl4JDefinition.getNamespaces();
            Map imported_def_namespaces = imported_def.getNamespaces();

            Object prefix;


            for (Iterator prefix_iterator = imported_def_namespaces.keySet().iterator();
                 prefix_iterator.hasNext();) {
                prefix = prefix_iterator.next();

                if (!def_namespaces.containsKey(prefix)) {
                    def_namespaces.put(prefix, imported_def_namespaces.get(prefix));
                }
            }

            // copy types
            Types imported_def_types = imported_def.getTypes();

            if (imported_def_types != null) {
                Types def_types = wsdl4JDefinition.getTypes();

                if (def_types == null) {
                    def_types = wsdl4JDefinition.createTypes();
                    wsdl4JDefinition.setTypes(def_types);
                }

                for (Iterator types_iterator =
                        imported_def_types.getExtensibilityElements().iterator();
                     types_iterator.hasNext();) {
                    // CHECKME
                    def_types.addExtensibilityElement((ExtensibilityElement) types_iterator.next());
                }
            }

            // add messages
            wsdl4JDefinition.getMessages().putAll(imported_def.getMessages());

            // add portTypes
            wsdl4JDefinition.getPortTypes().putAll(imported_def.getPortTypes());


            // add bindings
            wsdl4JDefinition.getBindings().putAll(imported_def.getBindings());

            // add services
            wsdl4JDefinition.getServices().putAll(imported_def.getServices());

            // add ExtensibilityElements
            wsdl4JDefinition.getExtensibilityElements()
                    .addAll(imported_def.getExtensibilityElements());

        }

        // after putting the imports we going to remove them to avoid any confilicts
        List importsList = new ArrayList();
        Map imports = wsdl4JDefinition.getImports();
        Import wsdlImport;
        Vector wsdlImportVector;
        for (Iterator importsVectorIter = imports.values().iterator(); importsVectorIter.hasNext();) {
            wsdlImportVector = (Vector) importsVectorIter.next();
            for (Iterator importsIter = wsdlImportVector.iterator(); importsIter.hasNext();) {
                wsdlImport = (Import) importsIter.next();
                importsList.add(wsdlImport);
            }
        }

        for (Iterator importsListIter = importsList.iterator();importsListIter.hasNext();){
            wsdlImport = (Import) importsListIter.next();
            wsdl4JDefinition.removeImport(wsdlImport);
        }
    }

    private static void getImportedDefinitions(Definition definition, Map importedDefs) {
        Map wsdlImports = definition.getImports();

        Import wsdl_import;
        Definition imported_def;
        String import_def_key;

        for (Iterator iterator = wsdlImports.values().iterator(); iterator.hasNext();) {
            Vector imports = (Vector) iterator.next();
            Iterator iter2 = imports.iterator();
            while (iter2.hasNext()) {
                wsdl_import = (Import) iter2.next();
                if (isTraceEnabled) {
                    log.trace("getImportedDefinitions: import uri=" + wsdl_import.getLocationURI());
                }
                imported_def = wsdl_import.getDefinition();

                import_def_key = imported_def.getDocumentBaseURI();

                if (import_def_key == null) {
                    import_def_key = imported_def.getTargetNamespace();
                }

                if (!importedDefs.containsKey(import_def_key)) {
                    importedDefs.put(import_def_key, imported_def);
                    getImportedDefinitions(imported_def, importedDefs);
                }
            }
        }
    }
}
