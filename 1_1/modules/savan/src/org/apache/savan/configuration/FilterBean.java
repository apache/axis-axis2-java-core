package org.apache.savan.configuration;

public class FilterBean {

	String name;
	String identifier;
	String clazz;
	
	public String getClazz() {
		return clazz;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getName() {
		return name;
	}
	
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
