package org.apache.axis2.wsdl.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Tries to load the properties from the config properties
 */
public class ConfigPropertyFileLoader {

    private static String[] extensionClassNames;

    private static final String CODE_GEN_KEY = "codegen.extension";

    static{
        try {
            InputStream stream = Object.class.getResourceAsStream("/org/apache/axis2/wsdl/codegen/codegen-config.properties");
            Properties props = new Properties();
            props.load(stream);

            String codeGenExtensionClasses = props.getProperty(CODE_GEN_KEY);
            if (codeGenExtensionClasses!=null){
                extensionClassNames = codeGenExtensionClasses.split(",");

            }



        } catch (IOException e) {
           throw new RuntimeException(e);
        }

    }

    public static String[] getExtensionClassNames() {
        return extensionClassNames;
    }
}
