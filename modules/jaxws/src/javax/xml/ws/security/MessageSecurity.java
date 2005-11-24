package javax.xml.ws.security;

import java.lang.annotation.*;

/**
 * The MessageSecurity annotation can be used to specify message-level security requirements on a class or method.
 * <p>
 * @since JAX-WS 2.0
 * @author shaas02
 *
 */
@Target(value={ElementType.TYPE,ElementType.METHOD})
public @interface MessageSecurity {
	
	/**
	 * Requested security features for outbound messages.
	 * default = {INTEGRITY, CONFIDENTIALITY}
	 */
	public abstract SecurityConfiguration.SecurityFeature[] outboundSecurityFeatures();
	
	/**
	 * Requested security features for inbound messages.
	 * default = {INTEGRITY, CONFIDENTIALITY}
	 */
	public abstract SecurityConfiguration.SecurityFeature[] inboundSecurityFeatures();
	
	/**
	 * Logical identifier of the configuration entry that describes how to fulfil the requested security features for inbound
	 * messages.
	 * default = "javax.xml.ws.security.default"
	 */
	public abstract java.lang.String inboundSecurityConfigId() default "javax.xml.ws.security.default";
	
	/**
	 * Logical identifier of the configuration entry that describes how to fulfil the requested security features for outbound
	 * messages.
	 * default = "javax.xml.ws.security.default"
	 */
	public abstract java.lang.String outboundSecurityConfigId() default "javax.xml.ws.security.default";

}
