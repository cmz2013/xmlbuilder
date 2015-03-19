package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.xml.builders.XMLBuilder;

public class TestXmlBuilder {
	
	private static XMLBuilder xmlBuilder = new XMLBuilder();
	
	/**
	 * @Test XMLBuilder.build(File templet, Collection datas)
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({"rawtypes" })
	public static String test1() throws Exception {
		List<People> peoples = new ArrayList<>();
		Address address = new Address();
		File templet = new File(TestXmlBuilder.class.getResource("People.xml").getPath());
		
		address.setPosition("北京海淀区");
		address.setPostcode("100000");
		
		for (int i = 0; i < 2; i++) {
			People people = new People();
			people.setAge(20 + i);
			people.setName("people" + i);
			people.setSex("男");
			people.setAddress(address);
			peoples.add(people);
		}
		Collection[] datas = {peoples, peoples};
		return xmlBuilder.build(templet, datas);
	}
	
	/**
	 * @Test XMLBuilder.build(File templet, Collection datas)
	 * @return
	 * @throws Exception
	 */
	public static String test2() throws Exception {
		File templet = new File(TestXmlBuilder.class.getResource("China.xml").getPath());
		China china = new China();
		china.setLatitude("3°51′N至53°33′N");
		china.setLongitude("73°33′E至135°05′E");
		return xmlBuilder.build(templet, china);
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(test1());
		System.out.println("\r\n\r\n");
		System.out.println(test2());
	}

}
