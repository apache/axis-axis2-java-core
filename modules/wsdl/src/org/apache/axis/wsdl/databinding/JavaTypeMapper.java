package org.apache.axis.wsdl.databinding;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.namespace.QName;


/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 *
 *  Java type mapper implementation. It adds the following default mappings
 * String	xsd:string
 * Boolean	xsd:boolean
 * boolean	xsd:boolean
 * Double	xsd:double
 * double	xsd:double
 * Long	xsd:long
 * long	xsd:long
 * Float	xsd:float
 * float	xsd:float
 * Integer	xsd:int
 * int	xsd:int
 * Short	xsd:short
 * short	xsd:short
 * Byte	xsd:byte
 * byte	xsd:byte
 * BigDecimal	xsd:decimal
 * GregorianCalendar	xsd:timeInstant!
 * Date	xsd:date
 * QName	xsd:QName

 */
public class JavaTypeMapper extends TypeMappingAdapter{

    public JavaTypeMapper() {
       //add the basic types to the table 
       this.map.put(new QName(XSD_SCHEMA_URL,"string"),String.class);
       this.map.put(new QName(XSD_SCHEMA_URL,"boolean"),Boolean.class);
       this.map.put(new QName(XSD_SCHEMA_URL,"double"),Double.class);
       this.map.put(new QName(XSD_SCHEMA_URL,"long"),Long.class);
       this.map.put(new QName(XSD_SCHEMA_URL,"float"),Float.class);
       this.map.put(new QName(XSD_SCHEMA_URL,"int"),Integer.class);
       this.map.put(new QName(XSD_SCHEMA_URL,"short"),Short.class);
       this.map.put(new QName(XSD_SCHEMA_URL,"byte"),Byte.class);
       this.map.put(new QName(XSD_SCHEMA_URL,"decimal"),BigDecimal.class);
       this.map.put(new QName(XSD_SCHEMA_URL,"date"),Date.class);
       this.map.put(new QName(XSD_SCHEMA_URL,"QName"),QName.class);
    }

    public String toString() {
        return this.map.toString();
    }
}
