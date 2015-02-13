package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.xml.builders.XMLBuilder;

public class TestXmlBuilder {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		List<People> peoples = new ArrayList<>();
		Address address = new Address();
		File templet = new File(TestXmlBuilder.class.getResource("People.xml").getPath());
		
		address.setPosition("����������");
		address.setPostcode("100000");
		
		for (int i = 0; i < 2; i++) {
			People people = new People();
			people.setAge(20 + i);
			people.setName("people" + i);
			people.setSex("��");
			people.setAddress(address);
			peoples.add(people);
		}
		
		System.out.println(XMLBuilder.build(peoples, templet));
	}

}
