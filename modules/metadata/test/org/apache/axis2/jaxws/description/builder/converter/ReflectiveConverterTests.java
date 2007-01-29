package org.apache.axis2.jaxws.description.builder.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.jws.*;
import javax.xml.ws.*;

import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.builder.WebParamAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;

import junit.framework.TestCase;


public class ReflectiveConverterTests extends TestCase {
	private static DescriptionBuilderComposite implDBC;
	private static DescriptionBuilderComposite seiDBC;
	
	public void setUp() {
		JavaClassToDBCConverter converter = new JavaClassToDBCConverter(SimpleServiceImpl.class);
		HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
		assertNotNull(dbcMap);
		implDBC = dbcMap.get(
				"org.apache.axis2.jaxws.description.builder.converter." +
				"ReflectiveConverterTests$SimpleServiceImpl");
		seiDBC = dbcMap.get(
				"org.apache.axis2.jaxws.description.builder.converter." +
				"ReflectiveConverterTests$SimpleService");
	}
	
	public static void testCreateImplDBC() {
		assertNotNull(implDBC);
		WebServiceAnnot wsAnnot = implDBC.getWebServiceAnnot();
		assertNotNull(wsAnnot);
		assertEquals("SimpleService", wsAnnot.serviceName());
	}
	
	public static void testImplMethods() {
		assertNotNull(implDBC);
		List<MethodDescriptionComposite> mdcList = implDBC.getMethodDescriptionsList();
		assertNotNull(mdcList);
		assertEquals(mdcList.size(), 2);
		MethodDescriptionComposite mdc = mdcList.get(0);
		assertNotNull(mdc);
		assertEquals("invoke", mdc.getMethodName());
		assertEquals("java.lang.String", mdc.getReturnType());
		mdc = mdcList.get(1);
		assertNotNull(mdc);
		assertEquals("invoke2", mdc.getMethodName());
		assertEquals("int", mdc.getReturnType());
	}
	
	public static void testImplParams() {
		assertNotNull(implDBC);
		List<MethodDescriptionComposite> mdcList = implDBC.getMethodDescriptionsList();
		assertNotNull(mdcList);
		assertEquals(mdcList.size(), 2);
		MethodDescriptionComposite mdc = mdcList.get(0);
		assertNotNull(mdc);
		List<ParameterDescriptionComposite> pdcList = mdc.getParameterDescriptionCompositeList();
		assertNotNull(pdcList);
		assertEquals(pdcList.size(), 1);
		ParameterDescriptionComposite pdc = pdcList.get(0);
		assertEquals("java.util.List<java.lang.String>", pdc.getParameterType());
	 	mdc = mdcList.get(1);
	 	pdcList = mdc.getParameterDescriptionCompositeList();
	 	assertNotNull(pdcList);
	 	assertEquals(pdcList.size(), 2);
	 	pdc = pdcList.get(0);
	 	assertEquals("int", pdc.getParameterType());
	 	pdc = pdcList.get(1);
	 	assertNotNull(pdc);
	 	assertEquals("int", pdc.getParameterType());
	}
	
	public static void testCreateSEIDBC() {
		assertNotNull(seiDBC);
		WebServiceAnnot wsAnnot = seiDBC.getWebServiceAnnot();
		assertNotNull(wsAnnot);
		assertEquals("SimpleServicePort", wsAnnot.name());
	}
	
	public static void testSEIMethods() {
		assertNotNull(seiDBC);
		List<MethodDescriptionComposite> mdcList = seiDBC.getMethodDescriptionsList();
		assertNotNull(mdcList);
		assertEquals(mdcList.size(), 2);
		MethodDescriptionComposite mdc = mdcList.get(0);
		assertEquals("invoke", mdc.getMethodName());
		assertEquals("java.lang.String", mdc.getReturnType());
		assertNotNull(mdc.getWebMethodAnnot());
		WebMethodAnnot wmAnnot = mdc.getWebMethodAnnot();
		assertEquals("invoke", wmAnnot.operationName());
		mdc = mdcList.get(1);
		assertEquals("invoke2", mdc.getMethodName());
		assertEquals("int", mdc.getReturnType());
	}
	
	public static void testSEIParams() {
		assertNotNull(seiDBC);
		List<MethodDescriptionComposite> mdcList = seiDBC.getMethodDescriptionsList();
		assertNotNull(mdcList);
		assertEquals(mdcList.size(), 2);
		MethodDescriptionComposite mdc = mdcList.get(0);
		assertNotNull(mdc);
		List<ParameterDescriptionComposite> pdcList = mdc.getParameterDescriptionCompositeList();
		assertNotNull(pdcList);
		assertEquals(pdcList.size(), 1);
		ParameterDescriptionComposite pdc = pdcList.get(0);
		assertNotNull(pdc);
		assertEquals("java.util.List<java.lang.String>", pdc.getParameterType());
		WebParamAnnot wpAnnot = pdc.getWebParamAnnot();
		assertNotNull(wpAnnot);
		assertEquals("echoString", wpAnnot.name());
		mdc = mdcList.get(1);
		assertNotNull(mdc);
		pdcList = mdc.getParameterDescriptionCompositeList();
		assertNotNull(pdcList);
		assertEquals(pdcList.size(), 2);
		pdc = pdcList.get(0);
		assertNotNull(pdc);
		assertEquals("int", pdc.getParameterType());
		assertNull(pdc.getWebParamAnnot());
		pdc = pdcList.get(1);
		assertNotNull(pdc);
		assertEquals("int", pdc.getParameterType());
		assertNull(pdc.getWebParamAnnot());
	}
	
	public void testDBCHierarchy() {
		JavaClassToDBCConverter converter = new JavaClassToDBCConverter(ChildClass.class);
		HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();
		DescriptionBuilderComposite dbc = dbcMap.get("org.apache.axis2.jaxws.description.builder.converter." +
				"ReflectiveConverterTests$ChildClass");
		assertNotNull(dbc);
		List<MethodDescriptionComposite> mdcList = dbc.getMethodDescriptionsList();
		assertNotNull(mdcList);
		assertEquals(mdcList.size(), 2);
		assertEquals("doAbstract", mdcList.get(0).getMethodName());
		assertEquals("extraMethod", mdcList.get(1).getMethodName());
		dbc = dbcMap.get("org.apache.axis2.jaxws.description.builder.converter." +
		"ReflectiveConverterTests$ParentClass");
		assertNotNull(dbc);
		mdcList = dbc.getMethodDescriptionsList();
		assertNotNull(mdcList);
		assertEquals(mdcList.size(), 1);
		assertEquals("doParentAbstract", mdcList.get(0).getMethodName());
		dbc = dbcMap.get("org.apache.axis2.jaxws.description.builder.converter." +
		"ReflectiveConverterTests$ServiceInterface");
		assertNotNull(dbc);
		mdcList = dbc.getMethodDescriptionsList();
		assertNotNull(mdcList);
		assertEquals(mdcList.size(), 1);
		assertEquals("doAbstract", mdcList.get(0).getMethodName());
		dbc = dbcMap.get("org.apache.axis2.jaxws.description.builder.converter." +
		"ReflectiveConverterTests$CommonService");
		assertNotNull(dbc);
		mdcList = dbc.getMethodDescriptionsList();
		assertNotNull(mdcList);
		assertEquals(mdcList.size(), 1);
		assertEquals("extraMethod", mdcList.get(0).getMethodName());
		dbc = dbcMap.get("org.apache.axis2.jaxws.description.builder.converter." +
		"ReflectiveConverterTests$ParentServiceInterface");
		assertNotNull(dbc);
		mdcList = dbc.getMethodDescriptionsList();
		assertNotNull(mdcList);
		assertEquals(mdcList.size(), 1);
		assertEquals("doParentAbstract", mdcList.get(0).getMethodName());
		dbc = dbcMap.get("org.apache.axis2.jaxws.description.builder.converter." +
		"ReflectiveConverterTests$AbstractService");
		assertNotNull(dbc);
		mdcList = dbc.getMethodDescriptionsList();
		assertNotNull(mdcList);
		assertEquals(mdcList.size(), 1);
		assertEquals("someAbstractMethod", mdcList.get(0).getMethodName());
		
	}

	@WebService(name="SimpleServicePort")
	public interface SimpleService {
		@WebMethod(operationName="invoke")
		public String invoke(@WebParam(name="echoString")List<String> arg1);
		public int invoke2(int arg1, int arg2);
	}
	
	@WebService(serviceName="SimpleService", endpointInterface="org.apache.axis2.jaxws." +
			"description.builder.converter.ReflectiveConverterTests$SimpleService")
	public class SimpleServiceImpl {
		public String invoke(List<String> myParam) {
			return myParam.get(0);
		}
		public int invoke2(int num1, int num2) {
			return num1 + num2;
		}
	}
	
	@WebService(serviceName="InheritanceTestChild")
	public class ChildClass extends ParentClass implements ServiceInterface, CommonService {
		public void doAbstract(){};
		public void extraMethod(){};
	}
	
	@WebService(serviceName="InhertianceTestParent") 
	public class ParentClass extends AbstractService implements ParentServiceInterface {
		public void doParentAbstract(){};
	}
	
	public interface ServiceInterface {
		public void doAbstract();
	}
	
	public interface CommonService {
		public void extraMethod();
	}
	
	public interface ParentServiceInterface {
		public void doParentAbstract();
	}
	
	public class AbstractService {
		public void someAbstractMethod() {};
	}
}
