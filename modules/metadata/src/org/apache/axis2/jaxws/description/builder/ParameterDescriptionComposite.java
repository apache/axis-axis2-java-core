/**
 * 
 */
package org.apache.axis2.jaxws.description.builder;

import org.apache.axis2.jaxws.ExceptionFactory;

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
        
        // If this is an array, then create a string version as described by Class.getName.
        // For example, "Foo[][]" becomes "[[LFoo".  Note that arrays of primitives must also be parsed.
        classToLoad = reparseIfArray(classToLoad);
        
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
     * If the parameter represents and array, then the returned string is in a format that
     * a Class.forName(String) can be done on it.  This format is described by Class.getName().
     * If the parameter does not represent an array, the parememter is returned unmodified.
     * 
     * Note that arrays of primitives are processed as well as arrays of objects.
     * @param classToLoad
     * @return
     */
	private String reparseIfArray(String classToLoad) {
	    
        String reparsedClassName = classToLoad;
        if (isClassAnArray(classToLoad)) {
            String baseType = getBaseArrayClassName(classToLoad);
            String dimensionPrefix = getArrayDimensionPrefix(classToLoad);
            if (getPrimitiveTypeArrayEncoding(baseType) != null) {
                reparsedClassName = dimensionPrefix + getPrimitiveTypeArrayEncoding(baseType);
            }
            else {
                reparsedClassName = dimensionPrefix + "L" + baseType + ";";  
            }
        }
        return reparsedClassName;
    }
    /**
     * Answers if the String representing the class contains an array declaration.
     * For example "Foo[][]" would return true, as would "int[]".
     * @param className
     * @return
     */
    private boolean isClassAnArray(String className) {
        if (className != null && className.indexOf("[") > 0) {
            return true;
        }
        else {
            return false;
        }
    }
    /** 
     * For an class name that is an array, return the non-array declaration portion.
     * For example "my.package.Foo[][]" would return "my.package.Foo". Returns null if
     * the argument does not contain an array declaration.
     * @param fullClassName
     * @return
     */
    private String getBaseArrayClassName(String fullClassName) {
        String baseArrayClassName = null;
        if (fullClassName != null) {
            int firstArrayDimension = fullClassName.indexOf("[");
            if (firstArrayDimension > 0) {
                baseArrayClassName = fullClassName.substring(0, firstArrayDimension);
            }
        }
        return baseArrayClassName;
    }
    /**
     * Return a prefix suitable for passing to Class.forName(String) for an array.  Each array dimension
     * represented by "[]" will be represented by a single "[".
     * @param arrayClassName
     * @return
     */
    private String getArrayDimensionPrefix(String arrayClassName) {
        StringBuffer arrayDimPrefix = new StringBuffer();
        
        if (arrayClassName != null) {
            int arrayDimIndex = arrayClassName.indexOf("[]");
            while (arrayDimIndex > 0) {
                arrayDimPrefix.append("[");
                // Skip over this "[]" and see if there are any more.
                int startNext = arrayDimIndex + 2;
                arrayDimIndex = arrayClassName.indexOf("[]", startNext);
            }
        }
        
        if (arrayDimPrefix.length() > 0)
            return arrayDimPrefix.toString();
        else
            return null;
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
    
    private static final String INT_PRIMITIVE = "int";
    private static final String BYTE_PRIMITIVE = "byte";
    private static final String CHAR_PRIMITIVE = "char";
    private static final String SHORT_PRIMITIVE = "short";
    private static final String BOOLEAN_PRIMITIVE = "boolean";
    private static final String LONG_PRIMITIVE = "long";
    private static final String FLOAT_PRIMITIVE = "float";
    private static final String DOUBLE_PRIMITIVE = "double";
    private static final String VOID_PRIMITIVE = "void";

    /**
     * For primitives, return the appropriate primitive class.  Note that arrays of primitives are
     * handled differently, like arrays of objects.  Only non-array primitives are processed by this
     * method.
     * @param classType
     * @return
     */
	private Class getPrimitiveClass(String classType) {
		
		Class paramClass = null;

		if (INT_PRIMITIVE.equals(classType)) {
			paramClass = int.class;
		} else if (BYTE_PRIMITIVE.equals(classType)) {
			paramClass = byte.class;
		} else if (CHAR_PRIMITIVE.equals(classType)) {
			paramClass = char.class;
		} else if (SHORT_PRIMITIVE.equals(classType)) {
			paramClass = short.class;
		} else if (BOOLEAN_PRIMITIVE.equals(classType)) {
			paramClass = boolean.class;
		} else if (LONG_PRIMITIVE.equals(classType)) {
			paramClass = long.class;
		} else if (FLOAT_PRIMITIVE.equals(classType)) {
			paramClass = float.class;
		} else if (DOUBLE_PRIMITIVE.equals(classType)) {
			paramClass = double.class;
		} else if (VOID_PRIMITIVE.equals(classType)) {
			paramClass = void.class;
		}
		return paramClass;
	}
    /**
     * Returns the encoding used to represent a Class for an array of 
     * a primitive type.  For example, an array of boolean is represented by "Z".  
     * This is as described in the javadoc for Class.getName().  If the argument is
     * not a primitive type, a null will be returned.
     * 
     * Note that arrays of voids are not allowed; a null will be returned.
     * @param primitiveType
     * @return
     */
    private String getPrimitiveTypeArrayEncoding(String primitiveType) {
        String encoding = null;
        
        if (BOOLEAN_PRIMITIVE.equals(primitiveType)) {
            encoding = "Z";
        }
        else if (BYTE_PRIMITIVE.equals(primitiveType)) {
            encoding = "B";
        }
        else if (CHAR_PRIMITIVE.equals(primitiveType)) {
            encoding = "C";
        }
        else if (DOUBLE_PRIMITIVE.equals(primitiveType)) {
            encoding = "D";
        }
        else if (FLOAT_PRIMITIVE.equals(primitiveType)) {
            encoding = "F";
        }
        else if (INT_PRIMITIVE.equals(primitiveType)) {
            encoding = "I";
        }
        else if (LONG_PRIMITIVE.equals(primitiveType)) {
            encoding = "J";
        }
        else if (SHORT_PRIMITIVE.equals(primitiveType)) {
            encoding = "S";
        }
        return encoding;
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
