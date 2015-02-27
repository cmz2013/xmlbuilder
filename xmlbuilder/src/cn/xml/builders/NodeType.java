package cn.xml.builders;
/**
 * 模板节点类型
 * 
 * @author Chongming
 *
 */
public enum NodeType {
	NORMAL, 
	/*
	 * 节点名称以"memobjects_"为前缀
	 */
	PREFIX_MEMOBJECTS, 
	/*
	 * 节点名称以"memobject_"为前缀
	 */
	PREFIX_MEMOBJECT, 
	/*
	 * 节点名称以"object_"为前缀
	 */
	PREFIX_OBJECT
}
