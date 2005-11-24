package javax.xml.ws.handler.soap;

import javax.xml.ws.handler.Handler;

/**
 * public interface SOAPHandler<T extends SOAPMessageContext>
 * extends Handler<T>
 * <p>
 * The SOAPHandler class extends Handler  to provide typesafety for the message context parameter and add a method to obtain 
 *  access to the headers that may be processed by the handler.
 * 
 * @since JAX-WS 2.0
 * @author shaas02
 *
 * @param <T>
 */
public interface SOAPHandler<T extends SOAPMessageContext>
				extends Handler<T> {
	
	/**
	 * Gets the header blocks that can be processed by this Handler instance.
	 * @return Set of QNames of header blocks processed by this handler 
	 * instance. QName is the qualified name of the outermost element of the 
	 * Header block.
	 */
	public java.util.Set<javax.xml.namespace.QName> getHeaders();
}
