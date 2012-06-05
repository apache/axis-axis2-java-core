package org.apache.axis2.util;

import java.util.ArrayList;

import org.apache.axis2.description.java2wsdl.XMLSchemaTest;
import org.apache.ws.commons.schema.XmlSchema;

public class SchemaUtilTest extends XMLSchemaTest{
    
    private SchemaUtil schemaUtil;

    @Override
    protected void setUp() throws Exception {
        schemaUtil=new SchemaUtil();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        schemaUtil=null;
        super.tearDown();
    }
    
    public void testGetAllSchemas() throws Exception{
        ArrayList<XmlSchema> schemaList=new ArrayList<XmlSchema>();
        loadSampleSchemaFile(schemaList);
        assertNotNull(schemaList.get(0));
        loadSampleSchemaFile(schemaList);
        XmlSchema[] schemas=SchemaUtil.getAllSchemas(schemaList.get(0));
        XmlSchema schema=schemas[0];
        assertEquals(schema, schemaList.get(0));
    }

    
    
}
