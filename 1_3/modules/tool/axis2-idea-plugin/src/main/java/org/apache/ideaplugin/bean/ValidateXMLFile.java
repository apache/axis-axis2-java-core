package org.apache.ideaplugin.bean;


import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.apache.axis2.tools.component.WizardPanel;

import java.io.StringReader;


public class ValidateXMLFile {


    /**
     * this calss used for check service xml validation
     */


    public  boolean Validate(String args) {
        try {
            // define the type of schema  get validation driver:
            SchemaFactory schemafactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // create schema by reading it from an XSD file:
            java.net.URL resource = WizardPanel.class.getResource("/resources/service.xsd");
            Schema schema = schemafactory.newSchema(new StreamSource(resource.getPath()));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(args)));

            schema.newValidator().validate(new DOMSource(doc));

            return true;
        }catch (SAXException ex) {
         //   ex.printStackTrace();
            return false;
        } catch (Exception ex) {
          //  ex.printStackTrace();
             return false;
        }

    }
}
