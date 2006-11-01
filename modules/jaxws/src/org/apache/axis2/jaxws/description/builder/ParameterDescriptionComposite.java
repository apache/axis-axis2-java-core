/**
 * 
 */
package org.apache.axis2.jaxws.description.builder;

import java.lang.reflect.Type;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;

public class ParameterDescriptionComposite {
	
	private String					parameterType;
	private Class					parameterTypeClass;
	private Type					parameterGenericType;
	private WebParamAnnot			webParamAnnot;
	private WebServiceRefAnnot 		webServiceRefAnnot;
	private WebServiceContextAnnot	webServiceContextAnnot;
	private int 					listOrder;
	private ClassLoader 			classLoader;

	public ParameterDescriptionComposite () {
		
	}
	
	public ParameterDescriptionComposite (	
			String					parameterType,
			Class 					parameterTypeClass,
			Type					parameterGenericType,
			WebParamAnnot 			webParamAnnot,
			WebServiceRefAnnot 		webServiceRefAnnot,
			WebServiceContextAnnot	webServiceContextAnnot) {

		this.parameterType 			= parameterType;
		this.parameterTypeClass		= parameterTypeClass;
		this.parameterGenericType	= parameterGenericType;
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
	 * @return Returns the parameterTypeClass.
	 * Do lazy loading
	 */
	public Class getParameterTypeClass() {
		if (parameterTypeClass == null) {
			if (getParameterType() != null) {
				parameterTypeClass = getPrimitiveClass(getParameterType());
				if (parameterTypeClass == null) {
					
					if (classLoader != null) {			
						try {
							parameterTypeClass = Class.forName(parameterType, false, classLoader);
							
						} catch (ClassNotFoundException ex) {
							throw ExceptionFactory.makeWebServiceException("ParameterDescriptionComposite: Class not found for parameter: " +parameterType);
						}
					} else {
						//Use the default classloader to get this strings class
						try {
							parameterTypeClass = Class.forName(parameterType);
						} catch (ClassNotFoundException ex) {
							throw ExceptionFactory.makeWebServiceException("ParameterDescriptionComposite: Class not found for parameter: " +parameterType);
						}
					}
				}
			}
		}
		return parameterTypeClass;
	}

	/**
	 * @return Returns the parameterGenericType.
	 * Do lazy loading
	 */
	public Type getParameterGenericType() {
		
		//TODO: Determine if this is a parameterized generic type
		//TODO: Need to set this based on the parameterTypeClass ...hmmm
		return parameterGenericType;
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

	/*
	 * @return Returns the classloader to use
	 */
	public ClassLoader getClassLoader() {
		return this.classLoader;
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
	 * @param parameterGenericType The parameterGenericType to set.
	 */
	private void setParameterGenericType(Type parameterGenericType) {
		this.parameterGenericType = parameterGenericType;
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

	/*
	 * @param classLoader the class loader to set
	 */
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	private Class getPrimitiveClass(String classType) {
		
		Class paramClass = null;
System.out.println("classType: " +classType);
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
}
