package cn.xml.builders;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * ����ģ�彫���ݶ���ת����xml
 * 
 * @author Chongming
 *
 */
public class XMLBuilder {
	
	/**
	 * �����ݶ���ת����xml�ַ���
	 * @param datas
	 * @param templet
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String build(List datas, File templet) throws Exception{
		TempletParser parser = new TempletParser();
		Document newDoc = DocumentHelper.createDocument();; 
		Document templateDoc = parser.parse(templet);
		
		Element rootNode = templateDoc.getRootElement();
		NodeType rootType = TempletParser.getNodeType(rootNode.getName());
        
		if (NodeType.PREFIX_OBJECT != rootType) {
			Element newNode = cloneNode(rootNode);
			buildChildNode(newNode, rootNode.elements(), datas);
			newDoc.setRootElement(newNode);
		} else {
			throw new Exception("The root element (" + rootNode.getName(
					) + ") name is wrong, please check " + templet.getName());
		}

		return newDoc.asXML();
	}	
	
	/**
	 * ����elements������newNode���ӽڵ�
	 * @param newNode
	 * @param elements
	 * @param datas
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SuppressWarnings("unchecked")
	private static void buildChildNode(Element newNode, 
			List<Element> elements, List<Object> datas) throws 
			NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		if (null != elements && elements.size() > 0) {
			for (Element element : elements) {
				NodeType elementType = TempletParser.getNodeType(element.getName());
		        
				if (NodeType.NORMAL == elementType) {
					Element newElement = newNode.addElement(element.getName());
					copyElement(element, newElement);
					buildChildNode(newElement, element.elements(), datas);
				} else if (NodeType.PREFIX_OBJECT == elementType) {
					String objectName = element.getName().substring(7);
					for (Object data : datas) {
						Element objectElement = newNode.addElement(objectName);
						parseObjectAttribute(objectElement, element.attributes(), data);
						parseObjectElement(objectElement, element.elements(), data);
					}
				} 
			}
		}
	}
	
	/**
	 * ����object�ڵ���ӽڵ�
	 * @param objectElement
	 * @param childElements
	 * @param data
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void parseObjectElement(
			Element objectElement, List<Element> childElements, Object data) throws 
			NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		if (null != childElements && childElements.size() > 0) {
			for (Element childElement : childElements) {
				NodeType elementType = 
						TempletParser.getNodeType(childElement.getName());
				
				if (NodeType.PREFIX_MEMOBJECT== elementType) {
					String memoName = childElement.getName().substring(10);
					Element memoe = objectElement.addElement(memoName);
					Object memData =
							parseMemObjectAttribute(memoe, childElement.attributes(), data);
					
					if (null != memData) {
						parseObjectElement(memoe, childElement.elements(), memData);
					}
				} else if (NodeType.PREFIX_MEMOBJECTS == elementType) {
					String memosName = childElement.getName().substring(11);
					ObjectAttributeResult result = 
							parseMemObjectAttribute(childElement.attributes(), data);
					
					if (null != result.getData()) {
						if (result.getData() instanceof Collection) {
							Collection datas = (Collection) result.getData();
							for (Object memData : datas) {
								Element memoesi = objectElement.addElement(memosName);
								setElementAttribute(memoesi, result.getAttrMap());
								List childes = childElement.elements();
								if (null == childes || childes.size() == 0) {
									setElementTextValue(memoesi, result.getTextValue());
								} else {
									parseObjectElement(memoesi, childes, memData);
								}
							}
						} else if (result.getData() instanceof Object[]) {
							Object[] datas = (Object[]) result.getData();
							for (Object memData : datas) {
								Element memoesi = objectElement.addElement(memosName);
								setElementAttribute(memoesi, result.getAttrMap());
								List childes = childElement.elements();
								if (null == childes || childes.size() == 0) {
									setElementTextValue(memoesi, result.getTextValue());
								} else {
									parseObjectElement(memoesi, childes, memData);
								}
							}
						}
					} else {
						Element memoesi = objectElement.addElement(memosName);
						setElementAttribute(memoesi, result.getAttrMap());
						List childes = childElement.elements();
						if (null == childes || childes.size() == 0) {
							setElementTextValue(memoesi, result.getTextValue());
						}
					}
				} else {
					Element objChildElement = objectElement.addElement(childElement.getName());
					parseObjectAttribute(objChildElement, childElement.attributes(), data);
					parseObjectElement(objChildElement, childElement.elements(), data);
				}
			}
		}
	}

	/**
	 * ���ýڵ�����
	 * @param element
	 * @param attrMap
	 */
	private static void setElementAttribute(Element element, Map<String, String> attrMap) {
		if (null != attrMap && attrMap.size() > 0) {
			for (String attrName : attrMap.keySet()) {
				element.addAttribute(attrName, attrMap.get(attrName));
			}
		}
	}

	/**
	 * ����memobject�ڵ������
	 * @param attributes
	 * @param data
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SuppressWarnings({ "rawtypes" })
	private static ObjectAttributeResult parseMemObjectAttribute(
			List<Attribute> attributes, Object data) throws NoSuchMethodException, 
			SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		ObjectAttributeResult result = new ObjectAttributeResult();
		Object memData = null;
		Map<String, String> attrMap = new HashMap<>();
		Collection textValues = null;
		
		if (null != attributes && attributes.size() > 0) {
			for (Attribute attr : attributes) {
				AttributeType attrType = TempletParser.getAttributeType(attr.getName());
				if (AttributeType.TEXT_VALUE == attrType) {
					Object textValue = getAttrValue(data, attr.getValue());
					if (null != textValue) {
						if (textValue instanceof Collection) {
							textValues = (Collection) textValue;
						} else if (textValue instanceof Object[]) {
							textValues = Arrays.asList( (Object[]) textValue); 
						} else {
							textValues = Arrays.asList(textValue);
						}
					}
				} else if (AttributeType.PREFIX_ATTRIBUTE == attrType) {
					String attrName = attr.getName().substring(10);
					Object value = getAttrValue(data, attr.getValue());
					attrMap.put(attrName, null == value ? "" : (value + ""));
				} else if (AttributeType.OBJECT_DATA == attrType) {
					List childElements = attr.getParent().elements();
					if (null != childElements && childElements.size() > 0) {
						memData = getAttrValue(data, attr.getValue());
					}
				} else {
					attrMap.put(attr.getName(), attr.getValue());
				}
			}
		}
		
		result.setAttrMap(attrMap);
		result.setData(memData);
		result.setTextValue(textValues);
		
		return result;
	}

	/**
	 * ����memobject�ڵ������
	 * @param memoe
	 * @param attributes
	 * @param data
	 * @return object_data���Զ�Ӧ������
	 * 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SuppressWarnings("unchecked")
	private static Object parseMemObjectAttribute(
			Element memoe, List<Attribute> attributes, Object data) throws 
			NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		Object memData = null;
		if (null != attributes && attributes.size() > 0) {
			for (Attribute attr : attributes) {
				AttributeType attrType = TempletParser.getAttributeType(attr.getName());
				if (AttributeType.TEXT_VALUE == attrType) {
					setElementTextValue(memoe, attr, data);
				} else if (AttributeType.PREFIX_ATTRIBUTE == attrType) {
					String attrName = attr.getName().substring(10);
					Object value = getAttrValue(data, attr.getValue());
					memoe.addAttribute(attrName, null ==  value ? "" : (value + ""));
				} else if (AttributeType.OBJECT_DATA == attrType) {
					List<Element> childElements = attr.getParent().elements();
					if (null != childElements && childElements.size() > 0) {
						memData = getAttrValue(data, attr.getValue());
					}
				} else {
					memoe.addAttribute(attr.getName(), attr.getValue());
				}
			}
		}
		return memData;
	}

	/**
	 * ����object�ڵ�����ԣ���Ҫ����attribute_*��text_value����ͨ����
	 * @param objectElement
	 * @param attributes
	 * @param data
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	private static void parseObjectAttribute(
			Element objectElement, List<Attribute> attributes, Object data) throws 
			NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		if (null != attributes && attributes.size() > 0) {
			for (Attribute attr : attributes) {
				AttributeType attrType = TempletParser.getAttributeType(attr.getName());
				
				if (AttributeType.PREFIX_ATTRIBUTE == attrType) {
					String oattrName = attr.getName().substring(10);
					Object oattrValue = getAttrValue(data, attr.getValue());
					objectElement.addAttribute(oattrName, null == oattrValue ? "" : (oattrValue + ""));
				} else if (AttributeType.TEXT_VALUE == attrType) {
					setElementTextValue(objectElement, attr, data);
				} else {
					objectElement.addAttribute(attr.getName(), attr.getValue());
				}
			}
		}
	}
	
	/**
	 * ���ýڵ���ı�ֵ
	 * @param element
	 * @param textValueAttr
	 * @param data
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings("rawtypes")
	private static void setElementTextValue(Element element, 
			Attribute textValueAttr, Object data) throws NoSuchMethodException, 
			SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		List childElements = textValueAttr.getParent().elements();
		if (null == childElements || childElements.size() == 0) {
			Object textValue = getAttrValue(data, textValueAttr.getValue());
			if (null != textValue) {
				if (textValue instanceof Collection) {
					Collection textValues = (Collection) textValue;
					setElementTextValue(element, textValues);
				} else if (textValue instanceof Object[]) {
					Object[] textValues = (Object[]) textValue;
					setElementTextValue(element, textValues);
				} else {
					element.setText(textValue + "");
				}
			}
		}
	}

	/**
	 * ����element��text value
	 * @param element
	 * @param textValues
	 */
	private static void setElementTextValue(
			Element element, Object...textValues) {
		
		if (null != textValues && textValues.length > 0) {
			element.setText(textValues[0] + "");
			if (textValues.length > 1) {
				Element parent = element.getParent();
				for (int i = 1; i < textValues.length; i++) {
					Element newElement = parent.addElement(element.getName());
					copyElementArrt(element, newElement);
					newElement.setText(textValues[i] + "");
				}
			}
		}
	}
	
	/**
	 * ��ȡdata.field��ֵ
	 * @param data
	 * @param field
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object getAttrValue(Object data, String field) throws 
			NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		field = field.trim();
		Class clazz = data.getClass(); 
		String getAttr = "get";
		if (1 == field.length()) {
			getAttr += field.toUpperCase();
		} else {
			getAttr += (field.substring(0, 1).toUpperCase() + field.substring(1));
		}
		
        Method method = clazz.getDeclaredMethod(getAttr); 
       return method.invoke(data);
	}
	
	/**
	 * ����element�����ԡ��ı���newElement
	 * @param rootNode
	 * @return
	 */
	private static void copyElement(Element element, Element newElement) {
		String text = element.getTextTrim();
		if (null != text && !"".equals(text)) {
			newElement.setText(text);
		}
		copyElementArrt(element, newElement);
	}
	
	/**
	 * ����element������
	 * @param element
	 * @param newElement
	 */
	@SuppressWarnings("unchecked")
	private static void copyElementArrt(Element element, Element newElement) {
		List<Attribute> attrs = element.attributes();
		if (null != attrs && attrs.size() > 0) {
			newElement.setAttributes(attrs);
		}
	}

	/**
	 * ��¡�ڵ����ơ����ԡ��ı������������ӽڵ�
	 * @param rootNode
	 * @return
	 */
	private static Element cloneNode(Element tnode) {
		Element newNode = DocumentHelper.createElement(tnode.getName());
		copyElement(tnode, newNode);
		return newNode;
	}

}