/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * @see org.apache.axis.i18n.Messages
 * 
 * FUNCTIONAL TEMPLATE for Messages classes.
 * 
 * Copy this template to your package.
 * 
 * For subpackages of org.apache.axis.*, the internal constants
 * are set appropriately.  To adapt this scheme to an extension project
 * (package prefix differs from org.apache.axis.*), edit the projectName
 * attribute.  The others shouldn't need to be changed unless this is
 * being adapted to a non-AXIS related project..
 * 
 * @author Richard A. Sitze (rsitze@us.ibm.com)
 * @author Karl Moss (kmoss@macromedia.com)
 * @author Glen Daniels (gdaniels@apache.org)
 */
public class Messages {
    private static final Class  thisClass = Messages.class;

    private static final String projectName = MessagesConstants.projectName;

    private static final String resourceName = MessagesConstants.resourceName;
    private static final Locale locale = MessagesConstants.locale;

    private static final String packageName = getPackage(thisClass.getName());
    private static final ClassLoader classLoader = thisClass.getClassLoader();

    private static final ResourceBundle parent =
        (MessagesConstants.rootPackageName == packageName)
        ? null
        : MessagesConstants.rootBundle;


    /***** NO NEED TO CHANGE ANYTHING BELOW *****/

    private static final MessageBundle messageBundle =
        new MessageBundle(projectName, packageName, resourceName,
                                     locale, classLoader, parent);

    /**
      * Get a message from resource.properties from the package of the given object.
      * @param key The resource key
      * @return The formatted message
      */
    public static String getMessage(String key)
        throws MissingResourceException
    {
        return messageBundle.getMessage(key);
    }

    /**
      * Get a message from resource.properties from the package of the given object.
      * @param key The resource key
      * @param arg0 The argument to place in variable {0}
      * @return The formatted message
      */
    public static String getMessage(String key, String arg0)
        throws MissingResourceException
    {
        return messageBundle.getMessage(key, arg0);
    }

    /**
      * Get a message from resource.properties from the package of the given object.
      * @param key The resource key
      * @param arg0 The argument to place in variable {0}
      * @param arg1 The argument to place in variable {1}
      * @return The formatted message
      */
    public static String getMessage(String key, String arg0, String arg1)
        throws MissingResourceException
    {
        return messageBundle.getMessage(key, arg0, arg1);
    }

    /**
      * Get a message from resource.properties from the package of the given object.
      * @param key The resource key
      * @param arg0 The argument to place in variable {0}
      * @param arg1 The argument to place in variable {1}
      * @param arg2 The argument to place in variable {2}
      * @return The formatted message
      */
    public static String getMessage(String key, String arg0, String arg1, String arg2)
        throws MissingResourceException
    {
        return messageBundle.getMessage(key, arg0, arg1, arg2);
    }

    /**
      * Get a message from resource.properties from the package of the given object.
      * @param key The resource key
      * @param arg0 The argument to place in variable {0}
      * @param arg1 The argument to place in variable {1}
      * @param arg2 The argument to place in variable {2}
      * @param arg3 The argument to place in variable {3}
      * @return The formatted message
      */
    public static String getMessage(String key, String arg0, String arg1, String arg2, String arg3)
        throws MissingResourceException
    {
        return messageBundle.getMessage(key, arg0, arg1, arg2, arg3);
    }

    /**
      * Get a message from resource.properties from the package of the given object.
      * @param key The resource key
      * @param arg0 The argument to place in variable {0}
      * @param arg1 The argument to place in variable {1}
      * @param arg2 The argument to place in variable {2}
      * @param arg3 The argument to place in variable {3}
      * @param arg4 The argument to place in variable {4}
      * @return The formatted message
      */
    public static String getMessage(String key, String arg0, String arg1, String arg2, String arg3, String arg4)
        throws MissingResourceException
    {
        return messageBundle.getMessage(key, arg0, arg1, arg2, arg3, arg4);
    }

    /**
      * Get a message from resource.properties from the package of the given object.
      * @param key The resource key
      * @param args An array of objects to place in corresponding variables
      * @return The formatted message
      */
    public static String getMessage(String key, String[] args)
        throws MissingResourceException
    {
        return messageBundle.getMessage(key, args);
    }
    
    public static ResourceBundle getResourceBundle() {
        return messageBundle.getResourceBundle();
    }
    
    public static MessageBundle getMessageBundle() {
        return messageBundle;
    }

    private static final String getPackage(String name) {
        return name.substring(0, name.lastIndexOf('.')).intern();
    }
}
