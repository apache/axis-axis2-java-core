package org.apache.axis2.schema.typemap;

import org.apache.axis2.schema.SchemaConstants;
import org.apache.axis2.databinding.types.*;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;
import org.apache.axis2.wsdl.codegen.emitter.CTypeInfo;

/**
 * The java type map. uses a static map for caching
 * Most code from Axis 1 Codebase*
 * Most code JavaTypeMap
 */
public class CTypeMap implements TypeMap{

    public Map getTypeMap()
    {
         return CTypeInfo.getTypeMap();
    }

}
