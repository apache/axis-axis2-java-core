package org.apache.axis2.jaxws.handler;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.ws.handler.MessageContext;

public class MessageContextImpl implements MessageContext {
	
	/**
	 * No scope information is present in Axis2, so am just adding it in
	 * a separate HashMap
	 */
	protected HashMap<String, Scope> propertyScopes = new HashMap<String, Scope>();
	
	protected org.apache.axis2.context.MessageContext axisMC;
	
	/*protected class PropVals{
		Object value;
		Scope propScope;
		
		PropVals(Object value, Scope scope){
			this.value = value;
			this.propScope = scope;
		}
	}*/
	
	
	/**
	 * The JAX-RPC MessageContext is prepared from Axis2 MessageContext, we
	 * delegate all the setters and getters methods to Axis2 MC whenever
	 * possible
	 */
	public MessageContextImpl(org.apache.axis2.context.MessageContext amc){
		axisMC = amc;
		//Set of check conditions to find out is the message is out/in bound message
		//at server or client
		if(axisMC.isResponseWritten() == false && axisMC.isServerSide() == false)
			setProperty(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true);
		else if(axisMC.isResponseWritten() == true && axisMC.isServerSide() == false)
			setProperty(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);
		else if(axisMC.isResponseWritten() == false && axisMC.isServerSide() == true)
			setProperty(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);
		else
			setProperty(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true);
			
		// Security Configuration not dealt with yet
		setProperty(MessageContext.MESSAGE_SECURITY_CONFIGURATION, null);
	}

	public void setPropertyScope(String name, Scope scope)
			throws IllegalArgumentException {
		//No scope information is present in Axis2, so am just adding it in
		// a separate HashMap
		propertyScopes.put(name, scope);

	}

	public Scope getPropertyScope(String name) throws IllegalArgumentException {
		return propertyScopes.get(name);
	}

	public void setProperty(String name, Object value)
			throws IllegalArgumentException, UnsupportedOperationException {
		
		axisMC.setProperty(name, value);
		propertyScopes.put(name, Scope.HANDLER); //Using HANDLER as the default scope
			//	  not sure which one should be default or if scope can be null
	}

	public void setProperty(String name, Object value, Scope scope)
			throws IllegalArgumentException, UnsupportedOperationException {
		
		axisMC.setProperty(name, value);
		propertyScopes.put(name, scope);
	}

	public Object getProperty(String name) throws IllegalArgumentException {
		
		return axisMC.getProperty(name);
	}

	public void removeProperty(String name) throws IllegalArgumentException {
		
		throw new UnsupportedOperationException("removing a property not supported" +
		" in underlying Axis2 MessageContext");
	}

	public boolean containsProperty(String name) {
		
		Object value = axisMC.getProperty(name);
		if(value != null)
			return true;
		else
			return false;
	}

	public Iterator getPropertyNames() {
		// Returning by reading values from the propertyScope for now, may not be correct
		return propertyScopes.keySet().iterator();
	}

}
