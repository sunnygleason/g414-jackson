package org.example;

public class SampleBean implements Example1 {
	private Integer a;
	private String b;
	
	@Override
	public Integer getA() {
		return a;
	}
	
	@Override
	public String getB() {
		return b;
	}
	
	public void setB(String b) {
		this.b = b;
	}
	
	@Override
	public void doIt(Object param1) {	
	}
}
