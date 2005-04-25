package org.apache.axis.description;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.context.MEPContext;
import org.apache.axis.context.MEPContextFactory;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.impl.WSDLOperationImpl;

/**
 * @author chathura@opensource.lk
 *
 */
public class AxisOperation extends WSDLOperationImpl implements
		ParameterInclude, WSDLOperation,DescriptionConstants {
	
	
	public AxisOperation(){
		this.setComponentProperty(PARAMETER_KEY, new ParameterIncludeImpl());
	}
	
	public AxisOperation(QName name){
		this();
		this.setName(name);
	}

	/**
     * Method addParameter
     *
     * @param param Parameter that will be added
     */
    public void addParameter(Parameter param) {
        if (param == null) {
            return;
        }
        ParameterIncludeImpl paramInclude =
                (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
        paramInclude.addParameter(param);
    }

   
    /**
     * Method getParameter
     *
     * @param name Name of the parameter
     * @return 
     */
    public Parameter getParameter(String name) {
        ParameterIncludeImpl paramInclude =
                (ParameterIncludeImpl) this.getComponentProperty(PARAMETER_KEY);
        return (Parameter) paramInclude.getParameter(name);
    }
    
    /**
	 * This method is responsible for finding a MEPContext for an incomming
	 * messages. An incomming message can be of two states.
	 * 
	 * 1)This is a new incomming message of a given MEP. 2)This message is a
	 * part of an MEP which has already begun.
	 * 
	 * The method is special cased for the two MEPs
	 * 
	 * #IN_ONLY #IN_OUT
	 * 
	 * for two reasons. First reason is the wide usage and the second being that
	 * the need for the MEPContext to be saved for further incomming messages.
	 * 
	 * In the event that MEP of this operation is different from the two MEPs
	 * deafulted above the decession of creating a new or this message relates
	 * to a MEP which already in business is decided by looking at the WSA
	 * Relates TO of the incomming message.
	 * 
	 * @param msgContext
	 * @return
	 */
	public MEPContext findMEPContext(MessageContext msgContext, boolean serverside)
			throws AxisFault {

		MEPContext mepContext = null;


		if (null == msgContext.getRelatesTo()) {
			//Its a new incomming message so get the factory to create a new
			// one
			mepContext = MEPContextFactory.createMEP(this
					.getMessageExchangePattern(), serverside);

		} else {
			// So this message is part of an ongoing MEP
			mepContext = this
					.getSavedMEPContextFromComponentProperties(msgContext
							.getRelatesTo().getAddress());
			if (null == mepContext) {
				throw new AxisFault(
						"Cannot relate the message in the operation :"
								+ this.getName() + " :Invalid RelatedTO value");
			}

		}

		this.addMEPContext(mepContext, msgContext.getMessageID());
		mepContext.addMessageContext(msgContext);
		return mepContext;

	}

	private void addMEPContext(MEPContext mepContext, String messageID) {
		((Map) this.getComponentProperty(MEP_MAP)).put(messageID, mepContext);
	}

	private MEPContext getSavedMEPContextFromComponentProperties(String messageID) {
		return (MEPContext) ((Map) this.getComponentProperty(MEP_MAP)).get(messageID);

	}
    
}

