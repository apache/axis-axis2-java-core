package org.apache.axis2.description;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.i18n.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class AxisDescription implements ParameterInclude,
        DescriptionConstants {

    private AxisDescription parent = null;

    private ParameterInclude parameterInclude;

    private PolicyInclude policyInclude = null;

    private HashMap children;


    public AxisDescription() {
        parameterInclude = new ParameterIncludeImpl();
        children = new HashMap();
    }

    public void addParameter(Parameter param) throws AxisFault {

        if (param == null) {
            return;
        }

        if (isParameterLocked(param.getName())) {
            throw new AxisFault(Messages.getMessage("paramterlockedbyparent", param.getName()));
        }

        parameterInclude.addParameter(param);
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameterInclude.removeParameter(param);
    }

    public void deserializeParameters(OMElement parameterElement)
            throws AxisFault {

        parameterInclude.deserializeParameters(parameterElement);

    }

    public Parameter getParameter(String name) {
        Parameter parameter = parameterInclude.getParameter(name);
        if (parameter == null && parent != null) {
            return parent.getParameter(name);
        } else {
            return parameter;
        }
    }

    public ArrayList getParameters() {
        return parameterInclude.getParameters();
    }

    public boolean isParameterLocked(String parameterName) {

        if (getParent() != null && getParent().isParameterLocked(parameterName)) {
            return true;
        }

        return getParameter(parameterName) != null
                && getParameter(parameterName).isLocked();
    }


    public void setParent(AxisDescription parent) {
        this.parent = parent;
    }

    public AxisDescription getParent() {
        return parent;
    }

    public void setPolicyInclude(PolicyInclude policyInclude) {
        this.policyInclude = policyInclude;
    }

    public PolicyInclude getPolicyInclude() {
        if(policyInclude == null) {
            policyInclude = new PolicyInclude(this);
        }
        return policyInclude;
    }

    public void addChild(AxisDescription child) {
        children.put(child.getKey(), child);
    }

    public void addChild(Object key, AxisDescription child) {
        children.put(key, child);
    }

    public Iterator getChildren() {
        return children.values().iterator();
    }

    public AxisDescription getChild(Object key) {
        return (AxisDescription) children.get(key);
    }

    public void removeChild(Object key) {
        children.remove(key);
    }

    public abstract Object getKey();

    /**
     * To check whether a given module has engaged to parenet
     * @param moduleName
     * @return
     */
}
