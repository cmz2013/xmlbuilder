package cn.xml;

import java.util.Map;

/**
 * object�ڵ����Խ��������
 * 
 * @author Chongming
 *
 */
public class ObjectAttributeResult {
	// object�ڵ�����key value
	private Map<String, String> attrMap;
	
	// ����object_data��Ӧ������
	private Object objectData;

	// ����text_valueֵ
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
