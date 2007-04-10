package org.apache.axis2.jaxws.polymorphic.shape.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import javax.wsdl.WSDLException;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.polymorphic.shape.sei.PolymorphicShapePortType;
import org.apache.axis2.jaxws.polymorphic.shape.sei.PolymorphicShapeService;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.jaxws.wsdl.SchemaReaderException;
import org.apache.axis2.jaxws.wsdl.impl.SchemaReaderImpl;
import org.test.shape.Shape;
import org.test.shape.Square;
import org.test.shape.threed.ThreeDSquare;

public class PolymorphicTests extends TestCase {

	public void testFormalAndActualTypeInDifferentPackages(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		PolymorphicShapeService service = new PolymorphicShapeService();
		PolymorphicShapePortType port = service.getPolymorphicShapePort();
		Shape shapeType;
		
		System.out.println("Sending Request to draw Square");
		Square shape = new Square();
		shape.setXAxis(1);
		shape.setYAxis(1);
		shape.setLength(10);
		shapeType = port.draw(shape);
		assertTrue(shapeType instanceof Square);
		System.out.println("Square was drawn");
		
		System.out.println("Sending Request to draw 3D Square");
		ThreeDSquare threeDshape = new ThreeDSquare();
		threeDshape.setXAxis(1);
		threeDshape.setYAxis(1);
		threeDshape.setLength(10);
		threeDshape.setWidth(10);
		threeDshape.setBredth(10);
		shapeType = port.draw3D(threeDshape);
		assertTrue(shapeType instanceof ThreeDSquare);
		System.out.println("3D Square was drawn");
		System.out.println("-------------------------------");
	}
	
	public void testInlineUseOfJAXBBinding(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		String schemaBindingPkgName = "org.test.echomessage";
		String standardPkgName= "org.test.complextype.nonanonymous";
		String wsdlLocation="test-resources/wsdl/JAXB_Customization_Sample.wsdl";
		URL url = null;
		try{
			try{
				String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
				wsdlLocation = new File(baseDir +File.separator+ wsdlLocation).getAbsolutePath();
			}catch(Exception e){
				e.printStackTrace();
				fail();
			}
			File file = new File(wsdlLocation);
			url = file.toURL();
			WSDLWrapper wsdlWrapper = new WSDL4JWrapper(url);
			org.apache.axis2.jaxws.wsdl.SchemaReader sr= new SchemaReaderImpl();
			Set<String> set= sr.readPackagesFromSchema(wsdlWrapper.getDefinition());
			assertNotNull(set);
			Iterator<String> iter = set.iterator();
			while(iter.hasNext()){
				String pkg = iter.next();
				System.out.println("Package = " +pkg);
			}
			System.out.println("------------------------------");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}catch(FileNotFoundException e) {
			e.printStackTrace();
			fail();
		}catch(UnknownHostException e) {
			e.printStackTrace();
			fail();
		}catch(ConnectException e) {
			e.printStackTrace();
			fail();
		}catch(IOException e) {
		    e.printStackTrace();
            fail();
        }catch(WSDLException e){
			e.printStackTrace();
			fail();
		}catch(SchemaReaderException e){
			e.printStackTrace();
			fail();
		}
	}
	
	public void testSchemaReader(){
		System.out.println("------------------------------");
		System.out.println("Test : "+getName());
		String wsdlLocation="test-resources/wsdl/shapes.wsdl";
		URL url = null;
		try{
			try{
				String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
				wsdlLocation = new File(baseDir +File.separator+ wsdlLocation).getAbsolutePath();
			}catch(Exception e){
				e.printStackTrace();
				fail();
			}
			File file = new File(wsdlLocation);
			url = file.toURL();
			WSDLWrapper wsdlWrapper = new WSDL4JWrapper(url);
			org.apache.axis2.jaxws.wsdl.SchemaReader sr= new SchemaReaderImpl();
			Set<String> set= sr.readPackagesFromSchema(wsdlWrapper.getDefinition());
			assertNotNull(set);
			Iterator<String> iter = set.iterator();
			while(iter.hasNext()){
				System.out.println("Package ="+iter.next());
			}
			System.out.println("------------------------------");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}catch(FileNotFoundException e) {
			e.printStackTrace();
			fail();
		}catch(UnknownHostException e) {
			e.printStackTrace();
			fail();
		}catch(ConnectException e) {
			e.printStackTrace();
			fail();
        }catch(IOException e) {
            e.printStackTrace();
            fail();
		}catch(WSDLException e){
			e.printStackTrace();
			fail();
		}catch(SchemaReaderException e){
			e.printStackTrace();
			fail();
		}
		
		
		        
	}
}
