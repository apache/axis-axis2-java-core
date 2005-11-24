package javax.xml.ws;

import java.lang.annotation.*;

/**
 * Used to annotate a generated service interface.
 * <p>
 * The information specified in this annotation is sufficient to uniquely identify a wsdl:service  element inside a WSDL document.
 * This wsdl:service  element represents the Web service for which the generated service interface provides a client view.
 * @author shaas02
 * @since JAX-WS 2.0
 */
@Target(value=ElementType.METHOD)
@Retention(value=RetentionPolicy.RUNTIME)
public @interface WebServiceClient {

	/**
	 * The local name of the Web service.
	 */
	public abstract java.lang.String name() default "";
	
	/**
	 * The namespace for the Web service.
	 */
	public abstract java.lang.String targetNamespace() default "";
	
	/**
	 * The location of the WSDL document for the service (a URL).
	 */
	public abstract java.lang.String wsdlLocation() default "";
}
