
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xpath.XPathAPI;
import groovy.xml.*
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;

class GroovyRcv{

	//public static void main(args){
   //	  println "hi"

	Object parseXML(arg){

	 addNodeValues = []
	 nameNodeValues = []
	 countEmp = -1

	 factory = DocumentBuilderFactory.newInstance()
	 builder = factory.newDocumentBuilder()
	 doc = builder.parse(arg)
	 //println doc
	 detNodes = XPathAPI.selectNodeList(doc,"ADDRESS/DET")
	 for(detNodeIndex in 0..(detNodes.getLength()-1)) {
	 nameNodes = XPathAPI.selectNodeList(detNodes.item(detNodeIndex),"NAME")

	 for(nameNodeIndex in 0..(nameNodes.getLength()-1)) {
	 nameNodeValue = nameNodes.item( nameNodeIndex ).getFirstChild()
	 nameNodeValues += [nameNodeValue]
	 }

	 addNodes = XPathAPI.selectNodeList(detNodes.item(detNodeIndex),"ADD")
	 for(addNodeIndex in 0..(addNodes.getLength()-1)) {
	 addNodeValue = addNodes.item(addNodeIndex).getFirstChild()
	 addNodeValues += [addNodeValue]
	 }
	 countEmp++
	 }//for

	writer = new StringWriter()
    xmlBuilder = new MarkupBuilder(writer)
	xmlBuilder.person(){
	 	for(x in 0..countEmp){
	 	employee(){
	 	name(nameNodeValues[x])
	 	address(addNodeValues[x])
	 	tdate(new java.util.Date())
	 	}
	  }
	 }//person

	 //new File("Output.xml").withPrintWriter{ pwriter |
     //pwriter.println writer.toString()}

    //ProcessedDoc = builder.parse(new File("Output.xml"))
    //ProcessedDoc = builder.parse(writer)
   return writer
   //	return ProcessedDoc

  }//mtd
}//class

