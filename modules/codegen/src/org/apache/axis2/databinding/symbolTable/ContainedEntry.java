package org.apache.axis2.databinding.symbolTable;

import javax.xml.namespace.QName;

public class ContainedEntry extends SymTabEntry {    
    protected TypeEntry type;       // TypeEntry for nested type
    
    /**
     * @param qname
     */
    protected ContainedEntry(TypeEntry type, QName qname) {
        super(qname);
        this.type = type;
    }
    
    /**
     * @return Returns the type.
     */
    public TypeEntry getType() {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(TypeEntry type) {
        this.type = type;
    }
}
