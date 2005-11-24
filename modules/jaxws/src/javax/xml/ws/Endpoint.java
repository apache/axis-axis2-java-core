package javax.xml.ws;

/**
 * A Web service endpoint.
 * <p>
 * Endpoints are created using the EndpointFactory  class. An endpoint is always tied to one Binding  and one implementor,
 * both set at endpoint creation time.
 * <p>
 * An endpoint is either in a published or an unpublished state. The publish methods can be used to start publishing an endpoint,
 * at which point it starts accepting incoming requests. Conversely, the stop method can be used to stop accepting incoming
 * requests and take the endpoint down.
 * <p>
 * An Executor may be set on the endpoint in order to gain better control over the threads used to dispatch incoming requests.
 * For instance, thread pooling with certain parameters can be enabled by creating a ThreadPoolExecutor and registering it with
 * the endpoint.
 * <p>
 * Handler chains can be set using the contained Binding.
 * <p>
 * An endpoint may have a list of metadata documents, such as WSDL and XMLSchema documents, bound to it. At publishing
 * time, the JAX-WS implementation will try to reuse as much of that metadata as possible instead of generating new one based on
 *  the annotations present on the implementor.
 *  <p>
 *  @since JAX-WS 2.0
 * @author shaas02
 * @see     EndpointFactory.createEndpoint(java.net.URI, java.lang.Object), Binding, Executor
 */
/**
 * @author shaas02
 *
 */
public interface Endpoint {

	static final java.lang.String WSDL_SERVICE = "javax.xml.ws.wsdl.service";
	
	static final java.lang.String WSDL_PORT = "javax.xml.ws.wsdl.port";
	
	/**
	 * Returns the binding for this endpoint.
	 * @return The binding for this endpoing
	 */
	Binding getBinding();
	
	/**
	 * Returns the implementation object for this endpoint.
	 * @return The implementor for this endpoint
	 */
	java.lang.Object getImplementor();
	
	/**
	 * Publishes this endpoint at the given address. The necessary server infrastructure will be created and configured by the
	 * JAX-WS implementation using some default configuration. In order to get more control over the server configuration,
	 * please use the javax.xml.ws.Endpoint#publish(Object) method instead.
	 * @param address - A URI specifying the address and transport/protocol to use. By default, a http: URI results in a
	 * SOAP/HTTP binding being used. Implementations may support other URI schemes.
	 */
	void publish(java.lang.String address);
	
	/**
	 * Publishes this endpoint at the provided server context. A server context encapsulates the server infrastructure and
	 * addressing information for a particular transport. For a call to this method to succeed, the server context passed as an
	 * argument to it must be compatible with the endpoint's binding.
	 * @param serverContext - An object representing a server context to be used for publishing the endpoint.
	 * @throws java.lang.IllegalArgumentException - If the provided server context is not supported by the
	 * implementation or turns out to be unusable in conjunction with endpoint's binding.
	 */
	void publish(java.lang.Object serverContext) throws java.lang.IllegalArgumentException;
	
	/**
	 * Stops publishing this endpoint.
	 */
	void stop();
	
	/**
	 * Returns true if the endpoint has been published.
	 * @return true if the endpoint has been published
	 */
	boolean isPublished();
	
	/**
	 * Returns a list of metadata documents for the service.
	 * @return List<javax.xml.transform.Source> A list of metadata documents for the service
	 */
	java.util.List<javax.xml.transform.Source> getMetadata();
	
	/**
	 * Sets the metadata for this endpoint.
	 * @param metadata - A list of XML document sources containing metadata information for the endpoint (e.g. WSDL or
	 * XML Schema documents)
	 * @throws java.lang.IllegalStateException - If the endpoint has already been published.
	 */
	void setMetadata(java.util.List<javax.xml.transform.Source> metadata) throws java.lang.IllegalStateException;
	
	/**
	 * Returns the executor for this Endpointinstance. The executor is used to dispatch an incoming request to the implementor
	 * object.
	 * @return The java.util.concurrent.Executor to be used to dispatch a request.
	 * @see Executor
	 */
	java.util.concurrent.Executor getExecutor();
	
	/**
	 * Sets the executor for this Endpoint instance. The executor is used to dispatch an incoming request to the implementor
	 * object. If this Endpoint is published using the publish(Object) method and the specified server context defines its
	 * own threading behavior, the executor may be ignored.
	 * @param executor - The java.util.concurrent.Executor  to be used to dispatch a request.
	 * @throws java.lang.SecurityException - If the instance does not support setting an executor for security reasons (e.g.
	 *  the necessary permissions are missing).
	 *  @see Executor
	 */
	void setExecutor(java.util.concurrent.Executor executor) throws java.lang.SecurityException;
	
	/**
	 * Returns the property bag for this Endpoint instance.
	 * @return Map<String,Object> The property bag associated with this instance.
	 */
	java.util.Map<java.lang.String,java.lang.Object> getProperties();
	
	/**
	 * Sets the property bag for this Endpoint instance.
	 * @param properties - The property bag associated with this instance.
	 */
	void setProperties(java.util.Map<java.lang.String,java.lang.Object> properties);
}
