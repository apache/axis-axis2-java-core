/*
* Copyright 2006 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.axis2.addressing;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AddressingHelper {

    private static final Log log = LogFactory.getLog(AddressingHelper.class);

    /**
     * Returns true if the ReplyTo address does not match one of the supported
     * anonymous urls. If the ReplyTo is not set, anonymous is assumed, per the Final
     * spec. The AddressingInHandler should have set the ReplyTo to non-null in the
     * 2004/08 case to ensure the different semantics. (per AXIS2-885)
     *
     * @param messageContext
     */
    public static boolean isReplyRedirected(MessageContext messageContext) {
        EndpointReference replyTo = messageContext.getReplyTo();
        if (replyTo == null) {
            if (log.isDebugEnabled()) {
                log.debug("isReplyRedirected: ReplyTo is null. Returning false");
            }
            return false;
        } else {
            return !replyTo.hasAnonymousAddress();
        }
    }

    /**
     * Returns true if the FaultTo address does not match one of the supported
     * anonymous urls. If the FaultTo is not set, the ReplyTo is checked per the
     * spec.
     *
     * @param messageContext
     * @see isReplyRedirected
     */
    public static boolean isFaultRedirected(MessageContext messageContext) {
        EndpointReference faultTo = messageContext.getFaultTo();
        if (faultTo == null) {
            if (log.isDebugEnabled()) {
                log.debug("isReplyRedirected: FaultTo is null. Returning isReplyRedirected");
            }
            return isReplyRedirected(messageContext);
        } else {
            return !faultTo.hasAnonymousAddress();
        }
    }

    /**
     * Extract the parameter representing the Anonymous flag from the AxisOperation
     * and return the String value. Return the default of "optional" if not specified.
     *
     * @param axisOperation
     */
    public static String getAnonymousParameterValue(AxisOperation axisOperation) {
        String value = "";
        if (axisOperation != null) {
            value = Utils.getParameterValue(axisOperation.getParameter(AddressingConstants.WSAW_ANONYMOUS_PARAMETER_NAME));
            if (log.isDebugEnabled()) {
                log.debug("getAnonymousParameterValue: value: '" + value + "'");
            }
        }

        if (value == null || "".equals(value.trim())) {
            value = "optional";
        }
        return value.trim();
    }

    /**
     * Set the value of an existing unlocked Parameter representing Anonymous or add a new one if one
     * does not exist. If a locked Parameter of the same name already exists the method will trace and
     * return.
     *
     * @param axisOperation
     * @param value
     */
    public static void setAnonymousParameterValue(AxisOperation axisOperation, String value) {
        if (value == null) {
            if (log.isDebugEnabled()) {
                log.debug("setAnonymousParameterValue: value passed in is null. return");
            }
            return;
        }

        Parameter param = axisOperation.getParameter(AddressingConstants.WSAW_ANONYMOUS_PARAMETER_NAME);
        // If an existing parameter exists
        if (param != null) {
            if (log.isDebugEnabled()) {
                log.debug("setAnonymousParameterValue: Parameter already exists");
            }
            // and is not locked
            if (!param.isLocked()) {
                if (log.isDebugEnabled()) {
                    log.debug("setAnonymousParameterValue: Parameter not locked. Setting value: " + value);
                }
                // set the value
                param.setValue(value);
            }
        } else {
            // otherwise, if no Parameter exists
            if (log.isDebugEnabled()) {
                log.debug("setAnonymousParameterValue: Parameter does not exist");
            }
            // Create new Parameter with correct name/value
            param = new Parameter();
            param.setName(AddressingConstants.WSAW_ANONYMOUS_PARAMETER_NAME);
            param.setValue(value);
            try {
                if (log.isDebugEnabled()) {
                    log.debug("setAnonymousParameterValue: Adding parameter with value: " + value);
                }
                // and add it to the AxisOperation object
                axisOperation.addParameter(param);
            } catch (AxisFault af) {
                // This should not happen. AxisFault is only ever thrown when a locked Parameter
                // of the same name already exists and this should be dealt with by the outer
                // if statement.
                if (log.isDebugEnabled()) {
                    log.debug("setAnonymousParameterValue: addParameter failed: " + af.getMessage());
                }
            }
        }
    }
}
