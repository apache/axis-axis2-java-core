package org.apache.axis2.wsdl.util;

import org.apache.axis2.wsdl.i18n.CodegenMessages;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Loads the properties from the config properties.
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


    public static final String DEFAULT_CODEGEN_CONFIG_PROPERTIES =
            "/org/apache/axis2/wsdl/codegen/codegen-config.properties";

    /* Note - Should be a non regular expression character. If not it should be properly escaped */
    private static final String SEPARATOR_CHAR = ",";

    /**
     * Loads a stream from the given
     * @param propertiesReference
     * @return
     * @throws FileNotFoundException
     */
    private static InputStream getStream(String propertiesReference) throws FileNotFoundException {
        InputStream stream = ConfigPropertyFileLoader.class.getResourceAsStream(propertiesReference);
        if (stream == null) {
            URL url = ConfigPropertyFileLoader.class.getResource(propertiesReference);
            stream = new FileInputStream(url.toString());
        }
        return stream;
    }

    static {
        loadAllProperties();
    }

    public static void reload(){
        reset();
        loadAllProperties();
    }

    private static void reset(){
        dbSupporterTemplateName = null;
        testObjectTemplateName = null;
        extensionClassNames = null;
        thirdPartySchemaNames = null;
        languageTypes = null;
        databindingFrameworkNames = null;
        languageEmitterMap = null;
        languageSpecificPropertiesMap = null;
        databindingFrameworkNameToExtensionMap = null;
        defaultLanguage = null;
        defaultDBFrameworkName = null;

    }
    private static void loadAllProperties() {
        try {
            //look for the system property "org.apache.axis2.codegen.config" to for a property
            //entry refering to the config properties
            String property = System.getProperty("org.apache.axis2.codegen.config");
            InputStream stream = null;

            if (property!=null){
                stream = getStream(property);
            }else{
                stream = getStream(DEFAULT_CODEGEN_CONFIG_PROPERTIES);
            }

            if (stream==null){
                throw new RuntimeException(CodegenMessages.getMessage("propfileload.generalException"));
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
                extensionClassNames = tempString.split(SEPARATOR_CHAR);

            }

            //load the data binding framework names
            tempString = props.getProperty(DATA_BINDING_FRAMEWORK_NAME_KEY);
            if (tempString != null) {
                databindingFrameworkNames = tempString.split(SEPARATOR_CHAR);
            }

            //populate the data binding framework name to extension name map
            tempString = props.getProperty(DATA_BINDING_FRAMEWORK_EXTENSION_NAME_KEY);
            if (tempString != null) {
                String[] frameworkExtensionNames = tempString.split(SEPARATOR_CHAR);

                try {
                    for (int i = 0; i < frameworkExtensionNames.length; i++) {
                        databindingFrameworkNameToExtensionMap.put(databindingFrameworkNames[i], frameworkExtensionNames[i]);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new Exception(CodegenMessages.getMessage("propfileload.frameworkMismatch"));
                }

            }

            //load the default framework name
            tempString = props.getProperty(DATA_BINDING_FRAMEWORK_DEFAULT_NAME_KEY);

            if (tempString == null || !databindingFrameworkNameToExtensionMap.containsKey(tempString)) {
                throw new Exception(CodegenMessages.getMessage("propfileload.unknownFramework"));
            }
            defaultDBFrameworkName = tempString;
            //load the third party schema names
            tempString = props.getProperty(THIRD_PARTY_SCHEMA_KEY_PREFIX);
            if (tempString != null) {
                thirdPartySchemaNames = tempString.split(SEPARATOR_CHAR);

            }
            //the db supporter template name
            dbSupporterTemplateName = props.getProperty(DATA_BINDING_TEMPLATE_NAME_KEY);

            testObjectTemplateName = props.getProperty(DATA_BINDING_TEST_OBJECT_TEMPLATE_NAME_KEY);



            //load the language names
            tempString = props.getProperty(LANGUAGE_TYPE_KEY_PREFIX);
            if (tempString != null) {
                languageTypes = tempString.split(SEPARATOR_CHAR);

                //load the language emitter map
                tempString = props.getProperty(EMITTER_CLASS_KEY);
                if (tempString == null) {
                    throw new Exception(CodegenMessages.getMessage("propfileload.emitterMissing"));
                } else {
                    String[] tempClassNames = tempString.split(SEPARATOR_CHAR);
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
                throw new Exception(CodegenMessages.getMessage("propfileload.unknownDefaultLang"));
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
            throw new RuntimeException(CodegenMessages.getMessage("propfileload.generalException"), e);
        }
    }

    /**
     * Gets the test object support template. This is used in the
     * generated test class.
     * @return Returns String.
     */
    public static String getTestObjectTemplateName() {
        return testObjectTemplateName;
    }

    /**
     * Gets the databinder template name. This is the template that has the
     * logic for creating the databind supporters.
     * @return Returns String.
     */
    public static String getDbSupporterTemplateName() {
        return dbSupporterTemplateName;
    }
    /**
     * Gets the extension class names.
     *
     * @return Returns String[].
     */
    public static String[] getExtensionClassNames() {
        return extensionClassNames;
    }

    /**
     * Gets the third party schema names list.
     *
     * @return Returns String[].
     */
    public static String[] getThirdPartySchemaNames() {
        return thirdPartySchemaNames;
    }

    /**
     * Gets the language type names.
     *
     * @return Returns String[].
     */
    public static String[] getLanguageTypes() {
        return languageTypes;
    }

    /**
     * Gets the emitter names map keys with the language name.
     *
     * @return Returns Map.
     */
    public static Map getLanguageEmitterMap() {
        return languageEmitterMap;
    }

    /**
     * Gets the default language name.
     *
     * @return Returns String.
     */
    public static String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Gets the language specific properties.
     *
     * @return Returns Map.
     */
    public static Map getLanguageSpecificPropertiesMap() {
        return languageSpecificPropertiesMap;
    }

    /**
     * Gets the databinding framework names.
     *
     * @return Returns String[].
     */
    public static String[] getDatabindingFrameworkNames() {
        return databindingFrameworkNames;
    }

    /**
     * Gets the extensions map for the databinding frameworks.
     * The entries are keys by the framework name.
     *
     * @return Returns Map.
     */
    public static Map getDatabindingFrameworkNameToExtensionMap() {
        return databindingFrameworkNameToExtensionMap;
    }

    /**
     * Gets the default DB framwork name.
     *
     * @return Returns String.
     */
    public static String getDefaultDBFrameworkName() {
        return defaultDBFrameworkName;
    }
}
