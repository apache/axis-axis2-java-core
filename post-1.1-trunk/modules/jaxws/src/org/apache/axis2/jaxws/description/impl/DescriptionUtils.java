/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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


package org.apache.axis2.jaxws.description.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;

/**
 * Utilities used throughout the Description package.
 */
class DescriptionUtils {
    
    static boolean isEmpty(String string) {
        return (string == null || "".equals(string));
    }
    
    static boolean isEmpty(QName qname) {
        return qname == null || isEmpty(qname.getLocalPart());
    }

    /**
     * Creat a java class name given a java method name (i.e. capitalize the first letter)
     * @param name
     * @return
     */
    static String javaMethodtoClassName(String methodName) {
        String className = null;
        if(methodName != null){
            StringBuffer buildClassName = new StringBuffer(methodName);
            buildClassName.replace(0, 1, methodName.substring(0,1).toUpperCase());
            className = buildClassName.toString();
        }
        return className;
    }
    
	/**
	 * @return Returns TRUE if we find just one WebMethod Annotation with exclude flag
	 * set to false
	 */
	static boolean falseExclusionsExist(DescriptionBuilderComposite dbc) {
		
		MethodDescriptionComposite mdc = null;
		Iterator<MethodDescriptionComposite> iter = dbc.getMethodDescriptionsList().iterator();
		
		while (iter.hasNext()) {
			mdc = iter.next();

			WebMethodAnnot wma = mdc.getWebMethodAnnot();
			if (wma != null) {
				if (wma.exclude() == false)
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Gathers all MethodDescriptionCompsite's that contain a WebMethod Annotation with the
	 * exclude set to FALSE
	 * @return Returns List<MethodDescriptionComposite> 
	 */
	static ArrayList<MethodDescriptionComposite> getMethodsWithFalseExclusions(DescriptionBuilderComposite dbc) {
		
		ArrayList<MethodDescriptionComposite> mdcList = new ArrayList<MethodDescriptionComposite>();
		Iterator<MethodDescriptionComposite> iter = dbc.getMethodDescriptionsList().iterator(); 
		
		if (DescriptionUtils.falseExclusionsExist(dbc)) {
			while (iter.hasNext()) {
				MethodDescriptionComposite mdc = iter.next();
				if (mdc.getWebMethodAnnot() != null) {
					if (mdc.getWebMethodAnnot().exclude() == false) {
						mdcList.add(mdc);
					}
				}
			}
		}
		
		return mdcList;
	}
	
	/*
	 * Check whether a MethodDescriptionComposite contains a WebMethod annotation with 
	 * exlude set to true
	 */
	static boolean isExcludeTrue(MethodDescriptionComposite mdc) {
	
		if (mdc.getWebMethodAnnot() != null) {
			if (mdc.getWebMethodAnnot().exclude() == true) {
					return true;
			}
		}
		
		return false;
	}
	
    static String javifyClassName(String className) {
    	if(className.indexOf("/") != -1) {
    		return className.replaceAll("/", ".");
    	}
    	return className;
    }

}
