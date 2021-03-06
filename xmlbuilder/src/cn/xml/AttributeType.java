package cn.xml;
/**
 * 模板节点属性类型
 * 
 * @author Chongming
 *
 */
public enum AttributeType {
	NORMAL, 
	/*
	 * 属性名称为"text_value"
	 */
	TEXT_VALUE, 
	/*
	 * 属性名称以"attribute_"为前缀
	 */
	PREFIX_ATTRIBUTE, 
	/*
	 * 属性名称为"object_data"
	 */
	OBJECT_DATA
}
