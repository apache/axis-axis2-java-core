package javax.xml.ws;

import java.lang.annotation.*;

/**
 * Used to annotate the getPortName()  methods of a generated service interface.
 * <p>
 * The information specified in this annotation is sufficient to uniquely identify a wsdl:port element inside a wsdl:service. The
 * latter is determined based on the value of the WebServiceClient  annotation on the generated service interface itself.
 * @author shaas02
 * @see <code>WebServiceClient</code>
 */
@Target(value=ElementType.METHOD)
@Retention(value=RetentionPolicy.RUNTIME)
public @interface WebEndpoint {

	/**
	 * The local name of the endpoint.
	 */
	public abstract java.lang.String name() default "";
}
