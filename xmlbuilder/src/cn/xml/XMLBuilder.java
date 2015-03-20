package cn.xml;

import java.io.File;
import java.lang.reflect.Field;
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
	
	private String xmlEncoding = "UTF-8";
	
	/**
	 * 将数据实例集datas转换成xml字符串，注意：PREFIX_OBJECT节点不可作为根节点
	 * 
	 * @param templet
	 * @param datas
	 * @return
	 * @throws DocumentException
	 * @throws IllegalAccessException
	 * @throws TempletException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes"})
	public String build(File templet, Collection[] datas) throws DocumentException, 
				IllegalAccessException, TempletException, NoSuchFieldException {
		
		oni = 0;
		Document newDoc = DocumentHelper.createDocument();
		newDoc.setXMLEncoding(xmlEncoding);
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
	public String build(File templet, Object data) throws Exception {
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
	 * @throws IllegalAccessException
	 * @throws TempletException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "unchecked"})
	private void buildChildNode(Element newNode, 
			List<Element> elements, Collection<Object>[]  datas) throws
			IllegalAccessException, TempletException, NoSuchFieldException {
		
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
							String text = element.getText();
							if (null != text && !"".equals(text = text.trim())) {
								objectElement.setText(text);
							}
							
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
	 * @throws IllegalAccessException
	 * @throws TempletException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "unchecked" })
	private void buildChildNode(Element newNode, 
			List<Element> elements, Object  data) throws 
			IllegalAccessException, TempletException, NoSuchFieldException {
		
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
					String text = element.getText();
					if (null != text && !"".equals(text = text.trim())) {
						objectElement.setText(text);
					}
					
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
	 * @throws IllegalAccessException
	 * @throws TempletException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void parseObjectElement(
			Element objectElement, List<Element> childElements, Object data) throws 
			IllegalAccessException, TempletException, NoSuchFieldException {
		
		if (null != childElements && childElements.size() > 0) {
			for (Element childElement : childElements) {
				NodeType elementType = parser.getNodeType(childElement.getName());
				
				if (NodeType.PREFIX_MEMOBJECT== elementType) {
					String memoName = childElement.getName().substring(10);
					Element memoe = objectElement.addElement(memoName);
					String text = childElement.getText();
					if (null != text && !"".equals(text = text.trim())) {
						memoe.setText(text);
					}
					
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
								String text = childElement.getText();
								if (null != text && !"".equals(text = text.trim())) {
									memoesi.setText(text);
								}
								
								setElementAttribute(memoesi, result.getAttrMap());
								setElementTextValue(memoesi, result.getTextValue());
								parseObjectElement(memoesi, childElement.elements(), memData);
							}
						} else if (result.getData() instanceof Object[]) {
							Object[] datas = (Object[]) result.getData();
							for (Object memData : datas) {
								Element memoesi = objectElement.addElement(memosName);
								String text = childElement.getText();
								if (null != text && !"".equals(text = text.trim())) {
									memoesi.setText(text);
								}
								
								setElementAttribute(memoesi, result.getAttrMap());
								setElementTextValue(memoesi, result.getTextValue());
								parseObjectElement(memoesi, childElement.elements(), memData);
							}
						}
					} else {
						Element memoesi = objectElement.addElement(memosName);
						String text = childElement.getText();
						if (null != text && !"".equals(text = text.trim())) {
							memoesi.setText(text);
						}
						
						setElementAttribute(memoesi, result.getAttrMap());
						setElementTextValue(memoesi, result.getTextValue());
					}
				} else {
					Element objChildElement = objectElement.addElement(childElement.getName());
					String text = childElement.getText();
					if (null != text && !"".equals(text = text.trim())) {
						objChildElement.setText(text);
					}
					
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
	 * @throws IllegalAccessException
	 * @throws TempletException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "rawtypes" })
	private ObjectAttributeResult parseMemObjectAttribute(
			List<Attribute> attributes, Object data) throws  
			IllegalAccessException, TempletException, NoSuchFieldException {
		
		ObjectAttributeResult result = new ObjectAttributeResult();
		Object memData = null;
		Map<String, String> attrMap = new HashMap<String, String>();
		Collection textValues = null;
		
		if (null != attributes && attributes.size() > 0) {
			for (Attribute attr : attributes) {
				AttributeType attrType = parser.getAttributeType(attr.getName());
				if (AttributeType.TEXT_VALUE == attrType) {
					Object textValue = getAttrValue(data, attr);
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
					Object value = getAttrValue(data, attr);
					attrMap.put(attrName, null == value ? "" : (value + ""));
				} else if (AttributeType.OBJECT_DATA == attrType) {
					List childElements = attr.getParent().elements();
					if (null != childElements && childElements.size() > 0) {
						memData = getAttrValue(data, attr);
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
	 * @return
	 * @throws IllegalAccessException
	 * @throws TempletException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("unchecked")
	private Object parseMemObjectAttribute(
			Element memoe, List<Attribute> attributes, Object data) throws 
			IllegalAccessException, TempletException, NoSuchFieldException {
		
		Object memData = null;
		if (null != attributes && attributes.size() > 0) {
			for (Attribute attr : attributes) {
				AttributeType attrType = parser.getAttributeType(attr.getName());
				if (AttributeType.TEXT_VALUE == attrType) {
					setElementTextValue(memoe, attr, data);
				} else if (AttributeType.PREFIX_ATTRIBUTE == attrType) {
					String attrName = attr.getName().substring(10);
					Object value = getAttrValue(data, attr);
					memoe.addAttribute(attrName, null ==  value ? "" : (value + ""));
				} else if (AttributeType.OBJECT_DATA == attrType) {
					List<Element> childElements = attr.getParent().elements();
					if (null != childElements && childElements.size() > 0) {
						memData = getAttrValue(data, attr);
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
	 * @throws IllegalAccessException
	 * @throws TempletException
	 * @throws NoSuchFieldException
	 */
	private void parseObjectAttribute(
			Element objectElement, List<Attribute> attributes, Object data) throws 
			IllegalAccessException, TempletException, NoSuchFieldException {
		
		if (null != attributes && attributes.size() > 0) {
			for (Attribute attr : attributes) {
				AttributeType attrType = parser.getAttributeType(attr.getName());
				
				if (AttributeType.PREFIX_ATTRIBUTE == attrType) {
					String oattrName = attr.getName().substring(10);
					Object oattrValue = getAttrValue(data, attr);
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
	 * @throws IllegalAccessException
	 * @throws TempletException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("rawtypes")
	private void setElementTextValue(Element element, 
			Attribute textValueAttr, Object data) throws
			IllegalAccessException, TempletException, NoSuchFieldException {
		
		Object textValue = getAttrValue(data, textValueAttr);
		if (null != textValue) {
			if (textValue instanceof Collection) {
				Collection textValues = (Collection) textValue;
				setElementTextValue(element, textValues);
			} else if (textValue instanceof Object[]) {
				Object[] textValues = (Object[]) textValue;
				setElementTextValue(element, textValues);
			} else {
				if (null != element.getText() && !"".equals(element.getText())) {
					element.setText(element.getText() + "\t\n" + textValue);
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
			String ttext = element.getText();
			if (null != ttext && !"".equals(ttext)) {
				element.setText(ttext + "\t\n" + textValues[0]);
			} else {
				element.setText(textValues[0] + "");
			}
			
			if (textValues.length > 1) {
				Element parent = element.getParent();
				for (int i = 1; i < textValues.length; i++) {
					Element newElement = parent.addElement(element.getName());
					copyElementArrt(element, newElement);
					
					if (null != ttext && !"".equals(ttext)) {
						newElement.setText(ttext + "\t\n" + textValues[i]);
					} else {
						newElement.setText(textValues[i] + "");
					}
				}
			}
		}
	}
	
	/**
	 * 获取data.field的值
	 * @param data
	 * @param attr
	 * @return
	 * @throws IllegalAccessException
	 * @throws TempletException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings({ "rawtypes" })
	private Object getAttrValue(Object data, Attribute attr) throws 
			 IllegalAccessException, TempletException, NoSuchFieldException {
		
		String fieldName = attr.getValue();
		if (null == fieldName || "".equals(fieldName = fieldName.trim())) {
			throw new TempletException("The attribute value is null: " +
					attr.getParent().getName() + "/" + attr.getName());
		}
		
		Class clazz = data.getClass(); 
		Field field = clazz.getDeclaredField(fieldName);
		
		if (field.isAccessible()) {
			return  field.get(data);
		} else {
			field.setAccessible(true);
			Object value =  field.get(data);
			field.setAccessible(false);
			return value;
		}
	}
	
	/**
	 * 复制element的属性、文本到newElement
	 * @param rootNode
	 * @return
	 */
	private void copyElement(Element element, Element newElement) {
		String text = element.getText();
		if (null != text && !"".equals(text = text.trim())) {
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

	public String getXmlEncoding() {
		return xmlEncoding;
	}

	public void setXmlEncoding(String xmlEncoding) {
		this.xmlEncoding = xmlEncoding;
	}
	
}