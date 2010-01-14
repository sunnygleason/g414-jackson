package org.example;

import org.codehaus.jackson.map.deser.BeanHelper.BeanBuilder;

public class TestBeanBuilder {
	public static void main(String[] args) throws Exception {
		String className = "$org.codehaus.jackson.generated$"
				+ Example1.class.getName();

		BeanBuilder builder = new BeanBuilder(className);
		builder.implement(Example1.class);
		Class ok = builder.load();

		Example1 eok = (Example1) ok.newInstance();

		ok.getMethod("setA", Integer.class).invoke(eok, 10101);
		ok.getMethod("setB", String.class).invoke(eok, "yay101");

		System.out.println(eok);
		System.out.println(eok.getA());
		System.out.println(eok.getB());
	}
}
