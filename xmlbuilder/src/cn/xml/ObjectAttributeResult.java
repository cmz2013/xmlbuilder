package cn.xml;

import java.util.Map;

/**
 * object节点属性解析结果集
 * 
 * @author Chongming
 *
 */
public class ObjectAttributeResult {
	// object节点属性key value
	private Map<String, String> attrMap;
	
	// 属性object_data对应的数据
	private Object objectData;

	// 属性text_value值
	private Object[] textValues;

	public Map<String, String> getAttrMap() {
		return attrMap;
	}

	public void setAttrMap(Map<String, String> attrMap) {
		this.attrMap = attrMap;
	}

	public Object getObjectData() {
		return objectData;
	}

	public void setObjectData(Object objectData) {
		this.objectData = objectData;
	}

	public Object[] getTextValues() {
		return textValues;
	}

	public void setTextValues(Object[] textValues) {
		this.textValues = textValues;
	}

}
