package cn.xml.builders;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * 解析用户定义的XML模板
 * 
 * @author Chongming
 *
 */
public class TempletParser {
	
	/**
	 * 返回模板对应的Document实例
	 * @param templet
	 * @return
	 * @throws DocumentException 
	 */
	public Document parse(File templet) throws DocumentException  {
		SAXReader reader = new SAXReader();
        return reader.read(templet);
	}
	
	/**
	 * 返回模板节点类型
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
	 * 返回模板节点属性类型
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
