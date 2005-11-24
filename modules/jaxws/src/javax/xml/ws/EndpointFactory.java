package javax.xml.ws;

/**
 * An abstract class that provides a factory for the creation of instances of the type javax.xml.ws.Endpoint. This abstract class
 * follows the abstract static factory design pattern. This enables a J2SE based client to create a Endpoint instance in a
 * portable manner without using the constructor of the Endpoint  implementation class.
 * <p>
 * The EndpointFactory implementation class is set using the system property ENDPOINTFACTORY_PROPERTY.
 * <p>
 * @author shaas02
 * @since JAX-WS 2.0
 * @see <code>Endpoint</code>
 */
public abstract class EndpointFactory {

	public static final java.lang.String ENDPOINTFACTORY_PROPERTY = "javax.xml.ws.EndpointFactory";
	
	private static EndpointFactory endpointFactoryImpl = null;
	
	/**
	 * 
	 */
	protected EndpointFactory(){
		
	}
	
	/**
	 * Gets an instance of the EndpointFactory
	 * <p>
	 * Only one copy of a factory exists and is returned to the application each time this method is called.
	 * <p>
	 *  The implementation class to be used can be overridden by setting the javax.xml.ws.EndpointFactory system property.
	 * @return
	 */
	public static EndpointFactory newInstance(){
		if (endpointFactoryImpl != null)
			return endpointFactoryImpl;
		
		try{
			String endpointFactoryImplName;
			
			endpointFactoryImplName = "org.apache.axis2.jaxws.EndpointFactoryImpl";
			Class loadedClass;
			
			loadedClass = Thread.currentThread().getContextClassLoader().loadClass(endpointFactoryImplName);
			endpointFactoryImpl = (EndpointFactory)loadedClass.newInstance();
		}catch(Exception e){
			
		}
		return endpointFactoryImpl;
	}
	
	/**
	 * Creates and publishes an endpoint at the provided address and using the given implementation object. Returns the created
	 *  endpoint. The JAX-WS implementation will create all the necessary server infrastructure using some default configuration.
	 *  In order to get more control over the server configuration, please use the javax.xml.ws.Endpoint#publish(Object) method
	 *  instead.
	 * @param address - A URI specifying the address and transport/protocol to use. By default, a http: URI results in a
	 * SOAP/HTTP binding being used. Implementations may support other URI schemes.
	 * @param implementor - A service implementation object to which incoming requests will be dispatched. The
	 * corresponding class must be annotated with all the necessary Web service annotations.
	 * @return The newly created endpoint.
	 */
	public abstract Endpoint publish(java.lang.String address,
			java.lang.Object implementor);
	
	/**
	 * Creates an endpoint object with the provided binding and implementation object. Once published, all requests will be
	 * dispatched to the latter.
	 * @param bindingId - A URI specifying the desired binding (e.g. SOAP/HTTP)
	 * @param implementor - A service implementation object to which incoming requests will be dispatched. The
	 * corresponding class must be annotated with all the necessary Web service annotations.
	 * @return
	 */
	public abstract Endpoint createEndpoint(java.net.URI bindingId,
			 java.lang.Object implementor);
}
