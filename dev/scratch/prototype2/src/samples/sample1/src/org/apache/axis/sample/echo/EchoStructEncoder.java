/*
 * Created on Jan 29, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.axis.sample.echo;

import java.io.IOException;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.encoding.Encoder;
import org.apache.axis.encoding.SimpleTypeEncodingUtils;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMException;
import org.apache.axis.om.OutObject;
import org.xml.sax.ContentHandler;

public class EchoStructEncoder implements OutObject,Encoder{
	private ContentHandler contentHandler;
	private EchoStruct struct;
	
	public EchoStructEncoder(EchoStruct struct){
		this.struct = struct;
	}
	
	public Object deSerialize(XMLStreamReader xpp)throws AxisFault{
		EchoStruct struct = new EchoStruct();
        
		try{
			int event = xpp.next();
			while(true){
				if(XMLStreamConstants.START_ELEMENT == event){
					if("value1".equals(xpp.getLocalName())){
						struct.setValue1(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value2".equals(xpp.getLocalName())){
						struct.setValue2(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value3".equals(xpp.getLocalName())){
						struct.setValue3(SimpleTypeEncodingUtils.deserializeInt(xpp));
					}else if("value4".equals(xpp.getLocalName())){
						struct.setValue4(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value5".equals(xpp.getLocalName())){
						struct.setValue5(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value6".equals(xpp.getLocalName())){
						struct.setValue6(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value7".equals(xpp.getLocalName())){
						struct.setValue7(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value8".equals(xpp.getLocalName())){
						struct.setValue8(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value9".equals(xpp.getLocalName())){
						struct.setValue9(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value10".equals(xpp.getLocalName())){
						struct.setValue10(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value11".equals(xpp.getLocalName())){
						struct.setValue11(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value12".equals(xpp.getLocalName())){
						struct.setValue12(SimpleTypeEncodingUtils.deserializeString(xpp));
					}else if("value13".equals(xpp.getLocalName())){
						struct.setValue13(SimpleTypeEncodingUtils.deserializeString(xpp));
					}
				}if(XMLStreamConstants.END_ELEMENT == event){
					break;
				}if(XMLStreamConstants.END_DOCUMENT == event){
					throw new AxisFault("premature and of file");
				}
				event = xpp.next();
			}

			return struct;
		} catch (XMLStreamException e) {
			throw AxisFault.makeFault(e);
		}

	}
	
	public void serialize(XMLStreamWriter writer)throws IOException, XMLStreamException{
		writer.writeStartElement("value1");
		
		writer.writeEndElement();
	}


	public void startBuilding(ContentHandler contentHandler) throws OMException {
		if(contentHandler == null){
			throw new OMException("Please set the content Handler");
		}
		try {
			contentHandler.startPrefixMapping("s", "http://axis.apache.org");
			String value = "value1";
			contentHandler.startElement("http://axis.apache.org",value,"s:value1",null);
			String strVal =  struct.getValue1();
			char[] str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value1");

			value = "value2";
			contentHandler.startElement("http://axis.apache.org",value,"s:value2",null);
			strVal =  struct.getValue2();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value2");

			value = "value3";
			contentHandler.startElement("http://axis.apache.org",value,"s:value3",null);
			int intVal  = struct.getValue3();
			str = String.valueOf(intVal).toCharArray();
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value3");

			value = "value4";
			contentHandler.startElement("http://axis.apache.org",value,"s:value4",null);
			strVal =  struct.getValue4();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value4");

			value = "value5";
			contentHandler.startElement("http://axis.apache.org",value,"s:value5",null);
			strVal =  struct.getValue5();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value5");

			value = "value6";
			contentHandler.startElement("http://axis.apache.org",value,"s:value6",null);
			strVal =  struct.getValue6();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value6");

			value = "value7";
			contentHandler.startElement("http://axis.apache.org",value,"s:value7",null);
			strVal =  struct.getValue7();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value7");

			value = "value8";
			contentHandler.startElement("http://axis.apache.org",value,"s:value8",null);
			strVal =  struct.getValue8();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value8");

			value = "value9";
			contentHandler.startElement("http://axis.apache.org",value,"s:value9",null);
			strVal =  struct.getValue9();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value9");

			value = "value10";
			contentHandler.startElement("http://axis.apache.org",value,"s:value10",null);
			strVal =  struct.getValue10();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value10");

			value = "value11";
			contentHandler.startElement("http://axis.apache.org",value,"s:value11",null);
			strVal =  struct.getValue11();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value11");

			value = "value12";
			contentHandler.startElement("http://axis.apache.org",value,"s:value12",null);
			strVal =  struct.getValue12();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value12");

			value = "value13";
			contentHandler.startElement("http://axis.apache.org",value,"s:value13",null);
			strVal =  struct.getValue13();
			str = ((strVal ==null)?new char[]{}: strVal.toCharArray());
			contentHandler.characters(str, 0, str.length);
			contentHandler.endElement("http://axis.apache.org",value,"s:value13");

		} catch (Exception e) {
			throw new OMException(e);
		}

		
		
	}

	/* (non-Javadoc)
	 * @see org.apache.axis.encoding.Encoder#setObject(java.lang.Object)
	 */
	public void setObject(Object obj) {
		this.struct = (EchoStruct)obj;

	}

}
