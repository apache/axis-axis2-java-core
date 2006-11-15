package org.apache.axis2.jibx;

import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

public class NullBindingFactory implements IBindingFactory {
    
    private static final String[] EMPTY_STRINGS = new String[0];
    
    public IMarshallingContext createMarshallingContext() throws JiBXException {
        return new MarshallingContext(EMPTY_STRINGS, EMPTY_STRINGS, EMPTY_STRINGS, this);
    }

    public IUnmarshallingContext createUnmarshallingContext()
        throws JiBXException {
        return new UnmarshallingContext(0, EMPTY_STRINGS, EMPTY_STRINGS, EMPTY_STRINGS, EMPTY_STRINGS, this);
    }

    public String getCompilerDistribution() {
        // normally only used by BindingDirectory code, so okay to punt
        return "";
    }

    public int getCompilerVersion() {
        // normally only used by BindingDirectory code, so okay to punt
        return 0;
    }

    public String[] getElementNames() {
        return EMPTY_STRINGS;
    }

    public String[] getElementNamespaces() {
        return EMPTY_STRINGS;
    }

    public String[] getMappedClasses() {
        return EMPTY_STRINGS;
    }

    public String[] getNamespaces() {
        return EMPTY_STRINGS;
    }

    public String[] getPrefixes() {
        return EMPTY_STRINGS;
    }

    public int getTypeIndex(String type) {
        return -1;
    }
}