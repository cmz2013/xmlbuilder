package cn.xml.builders;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * �����û������XMLģ��
 * 
 * @author Chongming
 *
 */
public class TempletParser {
	
	/**
	 * DOM����������xmlģ��
	 * @param templet
	 * @return
	 * @throws DocumentException 
	 */
	public Document parse(File templet) throws DocumentException  {
		SAXReader saxReader = new SAXReader();
        return saxReader.read(templet);
	}
	
	/**
	 * ����ģ��ڵ�����
	 * @param node
	 * @return
	 */
	public static NodeType getNodeType(String nodeName) {
		if (nodeName.startsWith("object_")) {
			return NodeType.PREFIX_OBJECT;
		} else if (nodeName.startsWith("memobjects_")) {
			return NodeType.PREFIX_MEMOBJECTS;
		} else if (nodeName.startsWith("memobject_")) {
			return NodeType.PREFIX_MEMOBJECT;
		} else {
			return NodeType.NORMAL;
		}
	}
	
	/**
	 * ����ģ��ڵ���������
	 * @param attributeName
	 * @return
	 */
	public static AttributeType getAttributeType(String attributeName) {
		if ("text_value".equals(attributeName)) {
			return AttributeType.TEXT_VALUE;
		}  else if ("object_data".equals(attributeName)) {
			return AttributeType.OBJECT_DATA;
		} else if (attributeName.startsWith("attribute_")) {
			return AttributeType.PREFIX_ATTRIBUTE;
		} else {
			return AttributeType.NORMAL;
		}
	}

}