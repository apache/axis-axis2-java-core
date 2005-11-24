package javax.xml.ws;

import java.lang.annotation.*;

/**
 * Used to annotate methods in the Service Endpoint Interface with the request wrapper bean to be used at runtime. The default
 * value of the localName is the operationName, as defined in WebMethod annotation and the targetNamespace is the target
 * namespace of the SEI.
 * <p>
 *  When starting from Java this annotation is used resolve overloading conflicts in document literal mode. Only the className is
 *  required in this case.
 *  @since JAX-WS 2.0
 * @author shaas02
 * @see javax.jws.WebMethod
 */
@Target(value=ElementType.METHOD)
@Retention(value=RetentionPolicy.RUNTIME)
public @interface RequestWrapper {

	/**
	 * public abstract java.lang.String localName
	 */
	public abstract java.lang.String localName() default "";
	
	/**
	 * Elements namespace name.
	 */
	public abstract java.lang.String targetNamespace() default "";
	
	/**
	 * Request wrapper bean name.
	 */
	public abstract java.lang.String className() default "";
}
