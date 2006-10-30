import java.io.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xpath.XPathAPI;
import groovy.xml.*
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import org.xml.sax.*;

class GroovyRcv{

	public static void main(args){
        String value = "<ADDRESS><DET><NAME>Ponnampalam Thayaparan</NAME> <OCC>Student</OCC><ADD>3-2/1,Hudson Road,Colombo-03</ADD><GENDER>Male</GENDER></DET><DET><NAME>Eranka Samaraweera</NAME><OCC>Student</OCC><ADD>Martara</ADD><GENDER>Male</GENDER></DET><DET><NAME>Sriskantharaja Ahilan</NAME><OCC>Student</OCC><ADD>Trincomalee</ADD><GENDER>Male</GENDER></DET></ADDRESS>"
		println new GroovyRcv().echo(new StringReader(value))
    }

	Object echo(StringReader arg){

	 List addNodeValues = []
	 List nameNodeValues = []
	 int countEmp = -1

	 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance()
	 DocumentBuilder builder = factory.newDocumentBuilder()
	 Document doc = builder.parse(new InputSource(arg))

	 NodeList detNodes = XPathAPI.selectNodeList(doc,"ADDRESS/DET")
	 for(detNodeIndex in 0..(detNodes.getLength()-1)) {
		 NodeList nameNodes = XPathAPI.selectNodeList(detNodes.item(detNodeIndex),"NAME")
	
		 for(nameNodeIndex in 0..(nameNodes.getLength()-1)) {
			 Node nameNodeValue = nameNodes.item( nameNodeIndex ).getFirstChild()
			 nameNodeValues += [nameNodeValue]
		 }
	
		 NodeList addNodes = XPathAPI.selectNodeList(detNodes.item(detNodeIndex),"ADD")
		 for(addNodeIndex in 0..(addNodes.getLength()-1)) {
			 Node addNodeValue = addNodes.item(addNodeIndex).getFirstChild()
			 addNodeValues += [addNodeValue]
		 }
		 countEmp++
	 }//for

	StringWriter writer = new StringWriter()
    MarkupBuilder xmlBuilder = new MarkupBuilder(writer)
	xmlBuilder.person(){
	 	for(x in 0..countEmp){
			employee(){
			name(nameNodeValues[x])
			address(addNodeValues[x])
			tdate(new java.util.Date())
	 	}
	  }
	 }

   return writer
  }
}