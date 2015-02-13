package cn.xml.builders;

import java.util.Collection;
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
	private Object data;

	// ����text_valueֵ
	@SuppressWarnings("rawtypes")
	private Collection textValue;

	public Map<String, String> getAttrMap() {
		return attrMap;
	}

	public void setAttrMap(Map<String, String> attrMap) {
		this.attrMap = attrMap;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@SuppressWarnings("rawtypes")
	public Collection getTextValue() {
		return textValue;
	}

	@SuppressWarnings("rawtypes")
	public void setTextValue(Collection textValue) {
		this.textValue = textValue;
	}

}
