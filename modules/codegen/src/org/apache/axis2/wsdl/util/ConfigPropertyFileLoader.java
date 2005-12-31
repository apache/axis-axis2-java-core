package org.apache.axis2.wsdl.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Tries to load the properties from the config properties
 */
public class ConfigPropertyFileLoader {



    private static String dbSupporterTemplateName;
    private static String testObjectTemplateName;
    private static String[] extensionClassNames;
    private static String[] thirdPartySchemaNames;
    private static String[] languageTypes;
    private static String[] databindingFrameworkNames;
    private static Map languageEmitterMap;
    private static Map languageSpecificPropertiesMap;
    private static Map databindingFrameworkNameToExtensionMap;

    private static String defaultLanguage;
    private static String defaultDBFrameworkName;


    private static final String CODE_GEN_KEY_PREFIX = "codegen.extension";
    private static final String THIRD_PARTY_SCHEMA_KEY_PREFIX = "codegen.thirdparty.schema";
    private static final String LANGUAGE_TYPE_KEY_PREFIX = "codegen.languages";
    private static final String DEFAULT_LANGUAGE_TYPE_KEY = "codegen.languages.default";
    private static final String EMITTER_CLASS_KEY = "codegen.emitters";
    private static final String DATA_BINDING_FRAMEWORK_NAME_KEY = "codegen.databinding.frameworks";
    private static final String DATA_BINDING_FRAMEWORK_DEFAULT_NAME_KEY = "codegen.databinding.frameworks.default";
    private static final String DATA_BINDING_FRAMEWORK_EXTENSION_NAME_KEY = "codegen.databinding.extensions";
    private static final String DATA_BINDING_TEMPLATE_NAME_KEY = "codegen.databinding.supporter.template";
    private static final String DATA_BINDING_TEST_OBJECT_TEMPLATE_NAME_KEY = "codegen.databinding.testobject.template";


    public static final String CODEGEN_CONFIG_PROPERTIES = "/org/apache/axis2/wsdl/codegen/codegen-config.properties";

    /* Note - Should be a non regular expression character. If not it should be properly escaped */
    private static final String SEPERATOR_CHAR = ",";

    static {
        try {

            InputStream stream = ConfigPropertyFileLoader.class.getResourceAsStream(CODEGEN_CONFIG_PROPERTIES);
            if (stream == null) {
                URL url = ConfigPropertyFileLoader.class.getResource(CODEGEN_CONFIG_PROPERTIES);
                stream = new FileInputStream(url.toString());
            }
            Properties props = new Properties();
            props.load(stream);

            //create a new map for the lang specific properties
            languageSpecificPropertiesMap = new HashMap();

            //create a new map for the databinding frameworks and their extensions
            databindingFrameworkNameToExtensionMap = new HashMap();

            //load the extension class names
            String tempString = props.getProperty(CODE_GEN_KEY_PREFIX);
            if (tempString != null) {
                extensionClassNames = tempString.split(SEPERATOR_CHAR);

            }

            //load the data binding framework names
            tempString = props.getProperty(DATA_BINDING_FRAMEWORK_NAME_KEY);
            if (tempString != null) {
                databindingFrameworkNames = tempString.split(SEPERATOR_CHAR);
            }

            //populate the data binding framework name to extension name map
            tempString = props.getProperty(DATA_BINDING_FRAMEWORK_EXTENSION_NAME_KEY);
            if (tempString != null) {
                String[] frameworkExtensionNames = tempString.split(SEPERATOR_CHAR);

                try {
                    for (int i = 0; i < frameworkExtensionNames.length; i++) {
                        databindingFrameworkNameToExtensionMap.put(databindingFrameworkNames[i], frameworkExtensionNames[i]);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new Exception("Number of frameworks and extension names do not match!");
                }

            }

            //load the default framework name
            tempString = props.getProperty(DATA_BINDING_FRAMEWORK_DEFAULT_NAME_KEY);

            if (tempString == null || !databindingFrameworkNameToExtensionMap.containsKey(tempString)) {
                throw new Exception("Unknown framework specified for default!");
            }
            defaultDBFrameworkName = tempString;
            //load the third party schema names
            tempString = props.getProperty(THIRD_PARTY_SCHEMA_KEY_PREFIX);
            if (tempString != null) {
                thirdPartySchemaNames = tempString.split(SEPERATOR_CHAR);

            }
            //the db supporter template name
            dbSupporterTemplateName = props.getProperty(DATA_BINDING_TEMPLATE_NAME_KEY);

            testObjectTemplateName = props.getProperty(DATA_BINDING_TEST_OBJECT_TEMPLATE_NAME_KEY);



            //load the language names
            tempString = props.getProperty(LANGUAGE_TYPE_KEY_PREFIX);
            if (tempString != null) {
                languageTypes = tempString.split(SEPERATOR_CHAR);

                //load the language emitter map
                tempString = props.getProperty(EMITTER_CLASS_KEY);
                if (tempString == null) {
                    throw new Exception("No emitters found");
                } else {
                    String[] tempClassNames = tempString.split(SEPERATOR_CHAR);
                    //populate the map
                    languageEmitterMap = new HashMap();
                    for (int i = 0; i < tempClassNames.length; i++) {
                        languageEmitterMap.put(languageTypes[i], tempClassNames[i]);
                    }

                }
            }

            // load the default language
            tempString = props.getProperty(DEFAULT_LANGUAGE_TYPE_KEY);
            if (null == tempString || !languageEmitterMap.containsKey(tempString)) {
                throw new Exception("Unknown Language specified for default!");
            }
            defaultLanguage = tempString;

            // run through the language specific properties and populate the
            // language specific property map
            //
            String languageType;
            String tempkey;
            HashMap langSpecificMap;
            for (int i = 0; i < languageTypes.length; i++) {
                languageType = languageTypes[i];
                langSpecificMap = new HashMap();
                Enumeration keyEnum = props.keys();
                while (keyEnum.hasMoreElements()) {
                    tempkey = keyEnum.nextElement().toString();
                    if (tempkey.startsWith(languageType + ".")) {
                        langSpecificMap.put(tempkey, props.get(tempkey));
                    }
                }
                //now add this to the lang specific properties map
                languageSpecificPropertiesMap.put(languageType, langSpecificMap);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException("Exception while loading the property file", e);
        }

    }

    /**
     * Get the test object support template. this is used in the
     * generated test class
     * @return
     */
    public static String getTestObjectTemplateName() {
        return testObjectTemplateName;
    }

    /**
     * The databinder template. This is the template that has the
     * logic for creating the databind supporters
     * @return
     */
    public static String getDbSupporterTemplateName() {
        return dbSupporterTemplateName;
    }
    /**
     * get the extension class names
     *
     * @return
     */
    public static String[] getExtensionClassNames() {
        return extensionClassNames;
    }

    /**
     * get the third party schema names list
     *
     * @return
     */
    public static String[] getThirdPartySchemaNames() {
        return thirdPartySchemaNames;
    }

    /**
     * get the language type names
     *
     * @return
     */
    public static String[] getLanguageTypes() {
        return languageTypes;
    }

    /**
     * get the emitter names map keyd with the language name
     *
     * @return
     */
    public static Map getLanguageEmitterMap() {
        return languageEmitterMap;
    }

    /**
     * get the default language name
     *
     * @return
     */
    public static String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * get the language specific properties
     *
     * @return
     */
    public static Map getLanguageSpecificPropertiesMap() {
        return languageSpecificPropertiesMap;
    }

    /**
     * get the databinding framework names
     *
     * @return
     */
    public static String[] getDatabindingFrameworkNames() {
        return databindingFrameworkNames;
    }

    /**
     * get the extensions map for the databinding frameworks
     * the entries are keys by the framework name
     *
     * @return
     */
    public static Map getDatabindingFrameworkNameToExtensionMap() {
        return databindingFrameworkNameToExtensionMap;
    }

    /**
     * get the default DB framwork name
     *
     * @return
     */
    public static String getDefaultDBFrameworkName() {
        return defaultDBFrameworkName;
    }
}
