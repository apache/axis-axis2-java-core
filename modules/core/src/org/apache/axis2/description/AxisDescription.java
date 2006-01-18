package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.om.OMElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class AxisDescription implements ParameterInclude,
        DescriptionConstants {

    private AxisDescription parent = null;

    private ParameterInclude parameterInclude;

    private PolicyInclude policyInclude;

    private HashMap children = new HashMap();

    public AxisDescription() {
        parameterInclude = new ParameterIncludeImpl();
        policyInclude = new PolicyInclude(this);
    }

    public void addParameter(Parameter param) throws AxisFault {

        if (param == null) {
            return;
        }

        if (isParameterLocked(param.getName())) {
            throw new AxisFault("Parameter:" + param.getName()
                    + " is already locked, hence value cannot be overridden");
        }

        parameterInclude.addParameter(param);
    }

    public void deserializeParameters(OMElement parameterElement)
            throws AxisFault {

        parameterInclude.deserializeParameters(parameterElement);

    }

    public Parameter getParameter(String name) {
        return parameterInclude.getParameter(name);
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
        if (parent.getChild(getKey()) == null
                || parent.getChild(getKey()) != this) {
            parent.addChild(this);
        }
    }

    public AxisDescription getParent() {
        return parent;
    }

    public void setPolicyInclude(PolicyInclude policyInclude) {
        this.policyInclude = policyInclude;
    }

    public PolicyInclude getPolicyInclude() {
        return policyInclude;
    }

    public void addChild(AxisDescription child) {
        children.put(child.getKey(), child);
        if (child.getParent() == null || child.getParent() != this) {
            child.setParent(this);
        }
    }

    public Iterator getChildren() {
        return children.values().iterator();
    }

    public AxisDescription getChild(Object key) {
        return (AxisDescription) children.get(key);
    }

    public abstract Object getKey();
}
