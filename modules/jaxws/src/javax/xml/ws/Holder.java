package javax.xml.ws;

/**
 * Holds a value of type T.
 * @author shaas02
 * @since JAX-WS 2.0
 */
public class Holder<T> {

	public T value;
	
	/**
	 * Creates a new holder with a null value.
	 */
	public Holder(){
		
	}
	
	/**
	 * Create a new holder with the specified value.
	 * @param value - The value to be stored in the holder.
	 */
	public Holder(T value){
		this.value = value;
	}
}
