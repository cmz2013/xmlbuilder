package cn.xml;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * �����û������XMLģ��
 * 
 * @author Chongming
 *
 */
public class TempletParser {
	
	/**
	 * ����ģ���Ӧ��Documentʵ��
	 * @param templet
	 * @return
	 * @throws TempletException 
	 */
	public Document parse(File templet) throws TempletException  {
		try {
			SAXReader reader = new SAXReader();
	        return reader.read(templet);
		} catch (Exception e) {
			throw new TempletException(e);
		}
	}
	
	/**
	 * ����ģ��ڵ�����
	 * @param node
	 * @return
	 */
	public NodeType getNodeType(String nodeName) {
		if (nodeName.startsWith("object_")) {
			return NodeType.PREFIX_OBJECT;
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
	public AttributeType getAttributeType(String attributeName) {
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
