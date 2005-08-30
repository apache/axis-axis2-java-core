package org.apache.axis2.wsdl.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Tries to load the properties from the config properties
 */
public class ConfigPropertyFileLoader {

    private static String[] extensionClassNames;
    private static String[] thirdPartySchemaNames;

    private static final String CODE_GEN_KEY = "codegen.extension";
    private static final String THIRD_PARTY_SCHEMA_KEY = "codegen.thirdparty.schema";

    static{
        try {
            InputStream stream = Object.class.getResourceAsStream("/org/apache/axis2/wsdl/codegen/codegen-config.properties");
            Properties props = new Properties();
            props.load(stream);

            String codeGenExtensionClasses = props.getProperty(CODE_GEN_KEY);
            if (codeGenExtensionClasses!=null){
                extensionClassNames = codeGenExtensionClasses.split(",");

            }

            String thirdPartySchemas = props.getProperty(THIRD_PARTY_SCHEMA_KEY);
            if (thirdPartySchemas!=null){
                thirdPartySchemaNames = thirdPartySchemas.split(",");

            }



        } catch (IOException e) {
           throw new RuntimeException(e);
        }

    }

    public static String[] getExtensionClassNames() {
        return extensionClassNames;
    }

    public static String[] getThirdPartySchemaNames() {
        return thirdPartySchemaNames;
    }
}
