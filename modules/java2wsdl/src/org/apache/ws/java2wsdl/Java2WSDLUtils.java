package org.apache.ws.java2wsdl;

/*
* Copyright 2004,2005 The Apache Software Foundation.
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
*
*/


public class Java2WSDLUtils
{
	public static final String HTTP = "http://";
	public static final char PACKAGE_CLASS_DELIMITER = '.';
	public static final String SCHEMA_NAMESPACE_EXTN = "/xsd";
	/**
     * check the entry for a URL. This is a simple check and need to be improved
     *
     * @param entry
     */

    public static boolean isURL(String entry) {
        return entry.startsWith("http://");
    }
    
    /**
     * A method to strip the fully qualified className to a simple classname
     *
     * @param qualifiedName
     */
    public static String getSimpleClassName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index > 0) {
            return qualifiedName.substring(index + 1, qualifiedName.length());
        }
        return qualifiedName;
    }
    
    public static StringBuffer namespaceFromPackageName(String packageName)
	{
		StringBuffer strBuf = new StringBuffer(HTTP);
		int prevIndex = packageName.length();
		int currentIndex = packageName.lastIndexOf(PACKAGE_CLASS_DELIMITER);
		while( currentIndex != -1 )
		{
			prevIndex = currentIndex;
			currentIndex = packageName.lastIndexOf(PACKAGE_CLASS_DELIMITER, prevIndex - 1);
			strBuf.append(packageName.substring(currentIndex + 1, prevIndex));
			if ( currentIndex != -1 )
			{
				strBuf.append(PACKAGE_CLASS_DELIMITER);
			}
		}
		return strBuf;
	}
    
    public static StringBuffer schemaNamespaceFromPackageName(String packageName)
	{
		return namespaceFromPackageName(packageName).append(SCHEMA_NAMESPACE_EXTN);
	}
}
