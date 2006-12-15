/**
 * 
 */
package org.apache.axis2.jaxws.description.builder;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;

public class ParameterDescriptionComposite {
    private static String JAXWS_HOLDER_CLASS = "javax.xml.ws.Holder";
    
	private String					parameterType;
	private Class					parameterTypeClass;
	private WebParamAnnot			webParamAnnot;
	private WebServiceRefAnnot 		webServiceRefAnnot;
	private WebServiceContextAnnot	webServiceContextAnnot;
	private int 					listOrder;

	private MethodDescriptionComposite	parentMDC;
	
	public ParameterDescriptionComposite () {
		
	}
	
	public ParameterDescriptionComposite (	
			String					parameterType,
			Class 					parameterTypeClass,
			WebParamAnnot 			webParamAnnot,
			WebServiceRefAnnot 		webServiceRefAnnot,
			WebServiceContextAnnot	webServiceContextAnnot) {

		this.parameterType 			= parameterType;
		this.parameterTypeClass		= parameterTypeClass;
		this.webParamAnnot 			= webParamAnnot;
		this.webServiceRefAnnot 	= webServiceRefAnnot;
		this.webServiceContextAnnot = webServiceContextAnnot;
	}
	
	/**
	 * @return Returns the parameterType.
	 */
	public String getParameterType() {
		return parameterType;
	}

	/**
     * Returns the class associated with the Parameter.  Note that if this is a generic (including a JAXWS
     * Holder<T>) then the class associated with the raw type is returned (i.e. Holder.class).
     * 
     * For a JAX-WS Holder<T>, use getHolderActualType(...) to get the class associated with T.
     * 
	 * @return Returns the parameterTypeClass.
	 */
	public Class getParameterTypeClass() {
		
		if (parameterTypeClass == null) {
			if (getParameterType() != null) {
				parameterTypeClass = getPrimitiveClass(getParameterType());
				if (parameterTypeClass == null) {
					// If this is a Generic, we need to load the class associated with the Raw Type, 
                    // i.e. for JAX-WS Holder<Foo>, we want to load Holder. Othwerise, load the type directly. 
                    String classToLoad = null;
                    if (getRawType(parameterType) != null) {
                        classToLoad = getRawType(parameterType);
                    }
                    else {
                        classToLoad = parameterType; 
                    }
                    parameterTypeClass = loadClassFromPDC(classToLoad);
				}
			}
		}
		return parameterTypeClass;
	}

    /**
     * For JAX-WS Holder<T>, returns the class associated with T.  For non-JAX-WS Holders
     * returns null.
     */
    public Class getHolderActualTypeClass() {
        Class returnClass = null;

        if (isHolderType(parameterType)) {
            String classToLoad = getHolderActualType(parameterType);
            returnClass = loadClassFromPDC(classToLoad);
        }
        return returnClass;
    }
    
    private Class loadClassFromPDC(String classToLoad) {
        Class returnClass = null;
        ClassLoader classLoader = null; 
        
        if (getMethodDescriptionCompositeRef() != null) {
            if (getMethodDescriptionCompositeRef().getDescriptionBuilderCompositeRef() != null){
                classLoader = getMethodDescriptionCompositeRef().getDescriptionBuilderCompositeRef().getClassLoader();
            }
        }
        if (classLoader != null) {          
            try {
                returnClass = Class.forName(classToLoad, false, classLoader);
                
            } 
            catch (ClassNotFoundException ex) {
                throw ExceptionFactory.makeWebServiceException("ParameterDescriptionComposite: Class not found for parameter: " +classToLoad);
            }
        } 
        else {
            //Use the default classloader to get this strings class
            try {
                returnClass = Class.forName(classToLoad);
            } 
            catch (ClassNotFoundException ex) {
                throw ExceptionFactory.makeWebServiceException("ParameterDescriptionComposite: Class not found for parameter: " +classToLoad);
            }
        }
        return returnClass;
    }
    

	/**
	 * @return Returns the webParamAnnot.
	 */
	public WebParamAnnot getWebParamAnnot() {
		return webParamAnnot;
	}

	/**
	 * @return Returns the webServiceRefAnnot.
	 */
	public WebServiceRefAnnot getWebServiceRefAnnot() {
		return webServiceRefAnnot;
	}

	/**
	 * @return Returns the webServiceContextAnnot.
	 */
	public WebServiceContextAnnot getWebServiceContextAnnot() {
		return webServiceContextAnnot;
	}

	/**
	 * @return Returns the webServiceContextAnnot.
	 */
	public int getListOrder() {
		return listOrder;
	}

	/**
	 * @return Returns the parentMDC.
	 */
	public MethodDescriptionComposite getMethodDescriptionCompositeRef() {
		return this.parentMDC;
	}

	/**
	 * @param parameterType The parameterType to set.
	 */
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * @param parameterTypeClass The parameterTypeClass to set.
	 */
	private void setParameterTypeClass(Class parameterTypeClass) {
		this.parameterTypeClass = parameterTypeClass;
	}

	/**
	 * @param webParamAnnot The webParamAnnot to set.
	 */
	public void setWebParamAnnot(WebParamAnnot webParamAnnot) {
		this.webParamAnnot = webParamAnnot;
	}

	/**
	 * @param webServiceRefAnnot The webServiceRefAnnot to set.
	 */
	public void setWebServiceRefAnnot(WebServiceRefAnnot webServiceRefAnnot) {
		this.webServiceRefAnnot = webServiceRefAnnot;
	}

	/**
	 * @param webServiceContextAnnot The webServiceContextAnnot to set.
	 */
	public void setWebServiceContextAnnot(WebServiceContextAnnot webServiceContextAnnot) {
		this.webServiceContextAnnot = webServiceContextAnnot;
	}

	/**
	 * @param webServiceContextAnnot The webServiceContextAnnot to set.
	 */
	public void setListOrder(int listOrder) {
		this.listOrder = listOrder;
	}

	/**
	 * @param  mdc The parent MethodDescriptionComposite to set.
	 */
	public void setMethodDescriptionCompositeRef(MethodDescriptionComposite mdc) {
		this.parentMDC = mdc;
	}

	private Class getPrimitiveClass(String classType) {
		
		Class paramClass = null;

		if (classType.equals("int")) {
			paramClass = int.class;
		} else if (classType.equals("byte")) {
			paramClass = byte.class;
		} else if (classType.equals("char")) {
			paramClass = char.class;
		} else if (classType.equals("short")) {
			paramClass = short.class;
		} else if (classType.equals("boolean")) {
			paramClass = boolean.class;
		} else if (classType.equals("long")) {
			paramClass = long.class;
		} else if (classType.equals("float")) {
			paramClass = float.class;
		} else if (classType.equals("double")) {
			paramClass = double.class;
		} else if (classType.equals("void")) {
			paramClass = void.class;
		}
		return paramClass;
	}
	
	/**
	 * Convenience method for unit testing. We will print all of the 
	 * data members here.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		String newLine = "\n";
		sb.append("***** BEGIN ParameterDescriptionComposite *****");
		sb.append("PDC.parameterType= " + parameterType);
		sb.append(newLine);
		if(webParamAnnot != null) {
			sb.append("\t @WebParam");
			sb.append(newLine);
			sb.append("\t" + webParamAnnot.toString());
		}
		sb.append(newLine);
		if(webServiceRefAnnot != null) {
			sb.append("\t @WebServiceRef");
			sb.append(newLine);
			sb.append("\t" + webServiceRefAnnot.toString());
		}
		sb.append(newLine);
		sb.append("***** END ParameterDescriptionComposite *****");
		return sb.toString();
	}
    
    public String getRawType() {
        return getRawType(parameterType);
    }
    public String getHolderActualType() {
        return getHolderActualType(parameterType);
    }
    public boolean isHolderType() {
        return isHolderType(parameterType);
    }

    /**
     * Returns a string representing the outermost generic raw type class, or null if the argument
     * is not a generic.  For example if the string "javax.xml.ws.Holder<my.package.MyObject>"
     * is passed in, the string "javax.xml.ws.Holder" will be returned.
     * @param inputType
     * @return A string representing the generic raw type or null if there is no generic.
     */
    public static String getRawType(String inputType) {
        String returnRawType = null;
        int leftBracket = inputType.indexOf("<");
        if (leftBracket > 0 ) {
            returnRawType = inputType.substring(0, leftBracket).trim();
        }
        return returnRawType;
    }

    /**
     * Return the actual type in a JAX-WS holder declaration.  For example, for the 
     * argument "javax.xml.ws.Holder<my.package.MyObject>", return "my.package.MyObject".
     * If the actual type itself is a generic, then that raw type will be returned.  For 
     * example, "javax.xml.ws.Holder<java.util.List<my.package.MyObject>>" will return 
     * "java.util.List".
     * 
     * Important note!  The JAX-WS Holder generic only supports a single actual type, i.e. 
     * the generic is javax.xml.ws.Holder<T>.  This method is not general purpose; it does not support 
     * generics with multiple types such as Generic<K,V> at the outermost level.
     * @param holderInputString
     * @return return the actual argument class name for a JAX-WS Holder; returns null
     *         if the argument is not a JAX-WS Holder
     */
    public static String getHolderActualType(String holderInputString) {
        String returnString = null;
        if (isHolderType(holderInputString)) {
            int leftBracket = holderInputString.indexOf("<");
            int rightBracket = holderInputString.lastIndexOf(">");
            if (leftBracket > 0 && rightBracket > leftBracket + 1) {
                String actualType = holderInputString.substring(leftBracket + 1, rightBracket).trim();
                String rawType = getRawType(actualType);
                if (rawType != null) {
                    returnString = rawType;
                }
                else {
                    return returnString = actualType;
                }
            }
        }
        return returnString;
    }
    
    /**
     * Check if the input String is a JAX-WS Holder.  For example 
     * "javax.xml.ws.Holder<my.package.MyObject>".
     * @param checkType
     * @return true if it is a JAX-WS Holder type; false otherwise.
     */
    public static boolean isHolderType(String checkType) {
        boolean isHolder = false;
        if (checkType != null) {
            String rawType = getRawType(checkType);
            if (rawType != null && rawType.equals(JAXWS_HOLDER_CLASS)) {
                isHolder = true;
            }
        }
        return isHolder;
    }
}
