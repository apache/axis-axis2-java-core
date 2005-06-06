/*
 * Created on Apr 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.saaj;

import junit.framework.TestCase;

import javax.xml.soap.SOAPElement;
import java.util.List;

/**
 * @author Ashutosh Shahi ashutosh.shahi@gmail.com	
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SOAPElementTest extends TestCase {
	
	private SOAPElement soapElem;
	
    protected void setUp() throws Exception
    {
        soapElem = SOAPFactoryImpl.newInstance().createElement( "Test", "test", "http://test.apache.org/" );
    }

    public void testAddTextNode() throws Exception
    {
        assertNotNull( soapElem );
        final String value = "foo";
        soapElem.addTextNode( value );
        assertEquals( value, soapElem.getValue() );
        TextImpl text = assertContainsText( soapElem );
        assertEquals( value, text.getValue() );
    }

    private TextImpl assertContainsText( SOAPElement soapElem ){
    	assertTrue( soapElem.hasChildNodes() );
    	List childElems = toList( soapElem.getChildElements() );
    	assertTrue( childElems.size() == 1 );
    	NodeImpl node = (NodeImpl) childElems.get( 0 );
    	assertTrue( node instanceof TextImpl );
    	return (TextImpl) node;
    }
    
    private List toList( java.util.Iterator iter )
    {
        List list = new java.util.ArrayList();
        while ( iter.hasNext() )
        {
            list.add( iter.next() );
        }
        return list;
    }
}
