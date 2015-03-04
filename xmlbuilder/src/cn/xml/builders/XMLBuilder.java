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
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * 根据模板将数据对象转换成xml
 * 
 * @author Chongming
 *
 */
public class XMLBuilder {
	
	private int oni = 0;
	
	private TempletParser parser = new TempletParser();
	
	/**
	 * 将数据实例集datas转换成xml字符串，注意：PREFIX_OBJECT节点不可作为根节点
	 * @param templet
	 * @param datas
	 * @return
	 * @throws DocumentException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws TempletException 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public String build(File templet, Collection[] datas) throws DocumentException, 
			NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, TempletException {
		Document newDoc = DocumentHelper.createDocument();
		Document templateDoc = parser.parse(templet);
		
		Element tRootNode = templateDoc.getRootElement();
		NodeType rootType = parser.getNodeType(tRootNode.getName());
        
		if (NodeType.PREFIX_OBJECT != rootType) {
			Element nRootNode = cloneNode(tRootNode);
			buildChildNode(nRootNode, tRootNode.elements(), datas);
			newDoc.setRootElement(nRootNode);
		} else {
			throw new TempletException("\"" + tRootNode.getName(
					) + "\" cannot serve as the root element, please check " + templet.getName());
		}
		
		return newDoc.asXML();
	}	
	
	/**
	 * 将数据对象data转换成xml字符串，注意：PREFIX_OBJECT节点可作为根节点
	 * 
	 * @param templet
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked"})
	public String build(File templet, Object data) throws Exception{
		Document newDoc = DocumentHelper.createDocument();; 
		Document templateDoc = parser.parse(templet);
		
		Element tRootNode = templateDoc.getRootElement();
		NodeType rootType = parser.getNodeType(tRootNode.getName());
		Element nRootNode = null;
		
		if (NodeType.PREFIX_OBJECT != rootType) {
			nRootNode = cloneNode(tRootNode);
			buildChildNode(nRootNode, tRootNode.elements(), data);
		} else {
			String objectName = tRootNode.getName().substring(7);
			nRootNode = DocumentHelper.createElement(objectName);
			parseObjectAttribute(nRootNode, tRootNode.attributes(), data);
			parseObjectElement(nRootNode, tRootNode.elements(), data);
		}
		
		newDoc.setRootElement(nRootNode);
		return newDoc.asXML();
	}
	
	/**
	 * 解析elements，构建newNode的子节点
	 * @param newNode
	 * @param elements
	 * @param datas
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SuppressWarnings({ "unchecked"})
	private void buildChildNode(Element newNode, 
			List<Element> elements, Collection<Object>[]  datas) throws
			NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		if (null != elements && elements.size() > 0) {
			for (Element element : elements) {
				NodeType elementType = parser.getNodeType(element.getName());
		        
				if (NodeType.NORMAL == elementType) {
					Element newElement = newNode.addElement(element.getName());
					copyElement(element, newElement);
					buildChildNode(newElement, element.elements(), datas);
				} else if (NodeType.PREFIX_OBJECT == elementType) {
					if (oni < datas.length) {
						String objectName = element.getName().substring(7);
						for (Object data : datas[oni]) {
							Element objectElement = newNode.addElement(objectName);
							parseObjectAttribute(objectElement, element.attributes(), data);
							parseObjectElement(objectElement, element.elements(), data);
						}
						oni++;
					}
					
				} 
			}
		}
	}
	
	/**
	 * 解析elements，构建newNode的子节点
	 * @param newNode
	 * @param elements
	 * @param data
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SuppressWarnings({ "unchecked" })
	private void buildChildNode(Element newNode, 
			List<Element> elements, Object  data) throws 
			NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		if (null != elements && elements.size() > 0) {
			for (Element element : elements) {
				NodeType elementType = parser.getNodeType(element.getName());
		        
				if (NodeType.NORMAL == elementType) {
					Element newElement = newNode.addElement(element.getName());
					copyElement(element, newElement);
					buildChildNode(newElement, element.elements(), data);
				} else if (NodeType.PREFIX_OBJECT == elementType) {
					String objectName = element.getName().substring(7);
					Element objectElement = newNode.addElement(objectName);
					parseObjectAttribute(objectElement, element.attributes(), data);
					parseObjectElement(objectElement, element.elements(), data);
				} 
			}
		}
	}
	
	/**
	 * 解析object节点的子节点
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
	private void parseObjectElement(
			Element objectElement, List<Element> childElements, Object data) throws 
			NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		if (null != childElements && childElements.size() > 0) {
			for (Element childElement : childElements) {
				NodeType elementType = parser.getNodeType(childElement.getName());
				
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
	 * 设置节点属性
	 * @param element
	 * @param attrMap
	 */
	private void setElementAttribute(Element element, Map<String, String> attrMap) {
		if (null != attrMap && attrMap.size() > 0) {
			for (String attrName : attrMap.keySet()) {
				element.addAttribute(attrName, attrMap.get(attrName));
			}
		}
	}

	/**
	 * 解析memobject节点的属性
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
	private ObjectAttributeResult parseMemObjectAttribute(
			List<Attribute> attributes, Object data) throws NoSuchMethodException, 
			SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		ObjectAttributeResult result = new ObjectAttributeResult();
		Object memData = null;
		Map<String, String> attrMap = new HashMap<String, String>();
		Collection textValues = null;
		
		if (null != attributes && attributes.size() > 0) {
			for (Attribute attr : attributes) {
				AttributeType attrType = parser.getAttributeType(attr.getName());
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
	 * 解析memobject节点的属性
	 * @param memoe
	 * @param attributes
	 * @param data
	 * @return object_data属性对应的数据
	 * 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	@SuppressWarnings("unchecked")
	private Object parseMemObjectAttribute(
			Element memoe, List<Attribute> attributes, Object data) throws 
			NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		Object memData = null;
		if (null != attributes && attributes.size() > 0) {
			for (Attribute attr : attributes) {
				AttributeType attrType = parser.getAttributeType(attr.getName());
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
	 * 解析object节点的属性，需要处理attribute_*、text_value和普通属性
	 * @param objectElement
	 * @param attributes
	 * @param data
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	private void parseObjectAttribute(
			Element objectElement, List<Attribute> attributes, Object data) throws 
			NoSuchMethodException, SecurityException, IllegalAccessException, 
			IllegalArgumentException, InvocationTargetException {
		
		if (null != attributes && attributes.size() > 0) {
			for (Attribute attr : attributes) {
				AttributeType attrType = parser.getAttributeType(attr.getName());
				
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
	 * 设置节点的文本值
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
	private void setElementTextValue(Element element, 
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
	 * 设置element的text value
	 * @param element
	 * @param textValues
	 */
	private void setElementTextValue(
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
	 * 获取data.field的值
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
	private Object getAttrValue(Object data, String field) throws 
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
	 * 复制element的属性、文本到newElement
	 * @param rootNode
	 * @return
	 */
	private void copyElement(Element element, Element newElement) {
		String text = element.getTextTrim();
		if (null != text && !"".equals(text)) {
			newElement.setText(text);
		}
		copyElementArrt(element, newElement);
	}
	
	/**
	 * 复制element的属性
	 * @param element
	 * @param newElement
	 */
	@SuppressWarnings("unchecked")
	private void copyElementArrt(Element element, Element newElement) {
		List<Attribute> attrs = element.attributes();
		if (null != attrs && attrs.size() > 0) {
			newElement.setAttributes(attrs);
		}
	}

	/**
	 * 克隆节点名称、属性、文本，但不包括子节点
	 * @param rootNode
	 * @return
	 */
	private Element cloneNode(Element tnode) {
		Element newNode = DocumentHelper.createElement(tnode.getName());
		copyElement(tnode, newNode);
		return newNode;
	}

}