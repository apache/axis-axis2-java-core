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

    private static String[] extensionClassNames;
    private static String[] thirdPartySchemaNames;
    private static String[] languageTypes;
    private static Map languageEmitterMap;
    private static String defaultLanguage;
    private static Map languageSpecificPropertiesMap;


    private static final String CODE_GEN_KEY = "codegen.extension";
    private static final String THIRD_PARTY_SCHEMA_KEY = "codegen.thirdparty.schema";
    private static final String LANGUAGE_TYPE_KEY = "codegen.languages";
    private static final String DEFAULT_LANGUAGE_TYPE_KEY = "codegen.languages.default";
    private static final String EMITTER_CLASS_KEY = "codegen.emitters";

    /* Note - Should be a non regular expression character. If not it should be properly escaped */
    private static final String SEPERATOR_CHAR = ",";

    static{
        try {

            InputStream stream =ConfigPropertyFileLoader.class.getResourceAsStream("/org/apache/axis2/wsdl/codegen/codegen-config.properties");
            if (stream ==null) {
                URL url = ConfigPropertyFileLoader.class.getResource("/org/apache/axis2/wsdl/codegen/codegen-config.properties");
                stream = new FileInputStream(url.toString());
            }
//            InputStream stream = Object.class.getResourceAsStream("/org/apache/axis2/wsdl/codegen/codegen-config.properties");
            Properties props = new Properties();
            props.load(stream);

            //create a new map for the lang specific properties
            languageSpecificPropertiesMap = new HashMap();

            String tempString = props.getProperty(CODE_GEN_KEY);
            if (tempString!=null){
                extensionClassNames = tempString.split(SEPERATOR_CHAR);

            }

            tempString = props.getProperty(THIRD_PARTY_SCHEMA_KEY);
            if (tempString!=null){
                thirdPartySchemaNames = tempString.split(SEPERATOR_CHAR);

            }

            tempString = props.getProperty(LANGUAGE_TYPE_KEY);
            if (tempString!=null){
                languageTypes = tempString.split(SEPERATOR_CHAR);

                //load the language emitter map
                tempString = props.getProperty(EMITTER_CLASS_KEY);
                if (tempString==null){
                    throw new Exception("No emitters found");
                }else{
                    String[] tempClassNames = tempString.split(SEPERATOR_CHAR);
                    //populate the map
                    languageEmitterMap = new HashMap();
                    for (int i = 0; i < tempClassNames.length; i++) {
                        languageEmitterMap.put(languageTypes[i],tempClassNames[i]);
                    }

                }
            }

            tempString = props.getProperty(DEFAULT_LANGUAGE_TYPE_KEY);
            if (null==tempString || !languageEmitterMap.containsKey(tempString) ){
                throw new Exception("Unknown Language specified for default!");
            }
            defaultLanguage = tempString;

            // run through the language specific properties and populate the
            // language specific property map
            //
            String languageType;
            String tempkey;
            HashMap langSpecificMap ;
            for (int i = 0; i < languageTypes.length; i++) {
                languageType = languageTypes[i];
                langSpecificMap = new HashMap();
                Enumeration keyEnum = props.keys();
                while (keyEnum.hasMoreElements()) {
                    tempkey = keyEnum.nextElement().toString();
                    if (tempkey.startsWith(languageType+".")){
                        langSpecificMap.put(tempkey,props.get(tempkey));
                    }
                }
                //now add this to the lang specific properties map
                languageSpecificPropertiesMap.put(languageType,langSpecificMap);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (Exception e){
            throw new RuntimeException("Unknown Exception in loading the property file",e);
        }

    }

    public static String[] getExtensionClassNames() {
        return extensionClassNames;
    }

    public static String[] getThirdPartySchemaNames() {
        return thirdPartySchemaNames;
    }

    public static String[] getLanguageTypes() {
        return languageTypes;
    }

    public static Map getLanguageEmitterMap() {
        return languageEmitterMap;
    }

    public static String getDefaultLanguage() {
        return defaultLanguage;
    }

    public static Map getLanguageSpecificPropertiesMap() {
        return languageSpecificPropertiesMap;
    }
}
