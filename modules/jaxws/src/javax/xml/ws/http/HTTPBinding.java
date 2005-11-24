package javax.xml.ws.http;

import javax.xml.ws.Binding;

/**
 * The HTTPBinding interface is an abstraction for the XML/HTTP binding.
 * @since JAX-WS 2.0
 * @author shaas02
 *
 */
public interface HTTPBinding extends Binding{

	/**
	 * A constant representing the identity of the XML/HTTP binding.
	 */
	static final java.lang.String HTTP_BINDING =	"http://www.w3.org/2004/08/wsdl/http";
	
}
