package org.apache.axis2.cluster.interceptor;

import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ServiceContextInterceptor {

	SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss.SSSZ");
	
	@Pointcut("(execution(ServiceContext.new(AxisService,ServiceGroupContext)" +
			" && this(ctx)")
	public void createContext(ServiceContext ctx) {
	}
	
	@Pointcut("(execution(ServiceContext.setProperties(Map)" +
			  " && this(ctx) && args(map)")
    public void setProperties(ServiceContext ctx, Map map) {
    }
	
	@Pointcut("(execution(ServiceContext.setProperty(String, Object)" +
	          " && this(ctx) && args(key,value)")
	public void setProperty(ServiceContext ctx, String key, Object value) {
	}

	@AfterReturning ("createContext(ctx)")
	public void afterCreateContext(ServiceContext ctx) {
		System.out.println("[" + getTimeStamp() + "] Service Context was created" + ctx);
	}
		
	@AfterReturning ("setProperties(ctx,map)")
	public void afterSetProperties(ServiceContext ctx, Map map) {
		System.out.println("[" + getTimeStamp() + "] Setting properties in Service Context " + map );		
	}
	
	@AfterReturning ("setProperties(ctx,key,value)")
	public void afterSetProperty(ServiceContext ctx, String key, Object value) {
		System.out.println("[" + getTimeStamp() + "] Setting property in Service Context key " + key+ " value " + value );		
	}	
	
	private String getTimeStamp(){
		return dateFormatter.format(new Date(System.currentTimeMillis()));
	}
	
}
