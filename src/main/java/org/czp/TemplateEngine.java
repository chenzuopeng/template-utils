package org.czp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

/**
 * 模板解析引擎.
 * 
 *                     a,当前支持4种标签：
 * 
 *                     迭代标签： 格式：{$ITERATE|集合对象}迭代内容{ITERATE$}
 *                     说明：1,此标签必须有开始和结束标签。
 *                     2,集合对象必须是实现了Iterable接口的集合对象，如：List,Set。 3,支持嵌套。
 * 
 *                     迭代子标签： 格式：{$ITEM|属性} 说明：1,属性值表示的是集合对象元素(Map)的Key。
 *                     2,此标签必须嵌套于迭代标签内部使用。
 * 
 *                     退格标签： 格式：{$BACKSPACE} 作用: 删除当前结果的最后一个字符
 * 
 *                     值标签： 格式：{KEY} 说明：1,KEY表示的是用于替换标签的内容的Map对象中的Key
 * 
 *                     换行标签： 格式：{$BR} 作用：换行。
 * 
 *                     b,支持标签的嵌套使用
 * 
 *                     c,使用实例： String template=
 *                     "111{AAA}222{$br}{$ITERATE|CON}aa{$ITEM|A}bb{CCC}{$ITERATE|arr}{$ITEM|ITEM}{ITERATE$}ccc,{ITERATE$}{$BACKSPACE}|333{BBB}444"
 *                     ; Map map=new java.util.HashMap();
 * 
 *                     map.put("AAA", "+"); map.put("BBB", "-"); map.put("CCC",
 *                     "%");
 * 
 *                     List list=new ArrayList();
 * 
 *                     Map map1=new java.util.HashMap(); map1.put("A", "你");
 *                     list.add(map1);
 * 
 *                     Map map2=new java.util.HashMap(); map2.put("A", "我");
 *                     list.add(map2);
 * 
 *                     Map map3=new java.util.HashMap(); map3.put("A", "他");
 *                     list.add(map3);
 * 
 *                     List arr=new ArrayList();
 * 
 *                     Map map4=new java.util.HashMap(); map4.put("ITEM", "en");
 *                     arr.add(map4); arr.add(map4); arr.add(map4);
 *                     map3.put("ARR",arr);
 * 
 *                     map.put("CON", list); TempleteEngine tr=new
 *                     TempleteEngine(); tr.setContent(map);
 *                     tr.setTemplate(template); tr.process();
 * 
 *                     输出结果:111+222 aa你bb%ccc aa我bb%ccc aa他bb%enenenccc 333-444
 * 
 * @author dylan.chen 2008-10-20
 * 
 */
public class TemplateEngine {

	private static Logger logger = Logger.getLogger(TemplateEngine.class);

	// 常量定义

	/**
	 * 查找标签的正则表达式
	 * */
	private final static String TAG_PATTERN = "\\{.*?\\}";

	/**
	 * ITERATE标签
	 * */
	private final static String TAG_ITERATE = "ITERATE";

	/**
	 * ITEM标签
	 * */
	private final static String TAG_ITEM = "ITEM";

	/**
	 * 换行标签
	 * */
	private final static String TAG_BR = "BR";

	/**
	 * BR标签的替换值
	 * */
	private final static String TAG_BR_VALUE = "\r\n";

	/**
	 * BACKSPACE标签
	 * */
	private final static String TAG_BACKSPACE = "BACKSPACE";

	// 常量定义结束

	/**
	 * 解析结果
	 * */
	private StringBuilder result = new StringBuilder();

	/**
	 * 用于替换标签的内容
	 * 
	 * 注：Key必须为大写
	 * */
	private Map<String, Object> content;

	/**
	 * 模板
	 * */
	private String template;

	/**
	 * 迭代标签堆栈 注：用于保持当前遍历到的迭代标签
	 * */
	private Stack<Tag> itTagStack = new Stack<Tag>();

	/**
	 * 标签类
	 *
	 * @Description:标签类
	 * @Copyright: Copyright (c) 2008 FFCS All Rights Reserved
	 * @Company: 北京福富软件有限公司
	 * @author 陈作朋 2008-10-20
	 * @version 1.00.00
	 * @history:
	 * 
	 */
	private class Tag {

		/**
		 * 标签名
		 * */
		private String name;

		/**
		 * 属性列表
		 * */
		private List<String> attributes = new ArrayList<String>();

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getAttributes() {
			return this.attributes;
		}

		@SuppressWarnings("unused")
		public void setAttributes(List<String> attributes) {
			this.attributes = attributes;
		}

		@Override
		public String toString() {
			final String TAB = "    ";

			String retValue = "";

			retValue = "Tag ( " + super.toString() + TAB + "name = " + this.name + TAB + "attributes = " + ArrayUtils.toString(this.attributes) + TAB + " )";

			return retValue;
		}

	}

	/**
	 * 解析模板
	 * 
	 * @description:解析模板
	 * 
	 * @author: 陈作朋 2008-10-20
	 * 
	 * @param template
	 *            模板
	 * @param itContent
	 *            迭代数据
	 * 
	 * @return void
	 * @throws TemplateEngineException
	 */
	@SuppressWarnings("unchecked")
	private void process(String _template, Map<String, Object> itContent) throws TemplateEngineException {
		logger.debug("模板:" + _template);
		logger.debug("数据:" + itContent);

		Pattern p = Pattern.compile(TAG_PATTERN);
		Matcher m = p.matcher(_template);

		// 上次解析的结束位置
		int prePosition = 0;

		while (m.find(prePosition)) {

			// 处理非标签内容
			this.result.append(_template.substring(prePosition, m.start()));

			// 当前标签
			String strTag = m.group();

			logger.debug("标签" + strTag);

			// 解析标签
			Tag tag = this.parseTag(strTag);

			logger.debug("Tag:" + tag);

			if (TAG_ITERATE.equalsIgnoreCase(tag.name)) {// 处理ITERATE标签

				// 属性验证
				if (tag.getAttributes().size() < 1) {
					throw new TemplateEngineException("错误的ITERATE标签");
				}

				// 当前迭代标签进栈
				this.itTagStack.push(tag);

				// 获取替换内容
				Object tmp = itContent.get(tag.getAttributes().get(0));
				Iterable<?> itValue = null;
				// 添加对数组作为迭代内容的支持
				if (tmp != null && tmp instanceof Iterable) {
					itValue = (Iterable<?>) tmp;
				}

				logger.debug("替换值=" + itValue);

				// 获取需要迭代的部分
				int endIndex = this.findEndTag(_template, tag.name);
				if (endIndex == -1) {
					throw new TemplateEngineException("未找到与之配对的结束标签");
				}
				String itTemplete = _template.substring(m.end(), endIndex);

				// 递归解析
				if (itValue != null) {
					for (Object item : itValue) {
						if (item instanceof Map) {
							process(itTemplete, (Map<String, Object>) item);
						}
					}
				} else {
					logger.error("模板解析异常:" + strTag + "标签无替换值");
				}
				// 当前迭代标签出栈
				this.itTagStack.pop();
				// TERATE结束标签的长度
				int endTagLen = ("{" + TAG_ITERATE + "$}").length();
				prePosition = endIndex + endTagLen;
			} else if (TAG_BACKSPACE.equalsIgnoreCase(tag.name)) {
				// 处理BACKSPACE标签,删除当前结果的最后一个字符
				this.result = this.result.deleteCharAt(this.result.length() - 1);
				prePosition = m.end();
			} else if (TAG_ITEM.equalsIgnoreCase(tag.name)) {// 处理ITEM标签

				// 判断是否有父标签
				if (this.itTagStack.empty() || !TAG_ITERATE.equals(this.itTagStack.peek().name)) {
					throw new TemplateEngineException("ITEM标签必须嵌套于ITERATE标签内部");
				}

				// 属性验证
				if (tag.getAttributes().size() < 1) {
					throw new TemplateEngineException("错误的ITEM标签");
				}

				// 获取替换内容
				Object tmp = itContent.get(tag.getAttributes().get(0));
				String value = null;
				if (tmp != null && tmp instanceof String) {
					value = (String) tmp;
				}

				logger.debug("替换值=" + value);

				if (value != null) {
					this.result.append(value);
				} else {
					logger.error("模板解析异常:" + strTag + "标签无替换值");
				}
				prePosition = m.end();
			} else if (TAG_BR.equalsIgnoreCase(tag.name)) {// 处理换行标签

				logger.debug("替换值=" + TAG_BR_VALUE);

				this.result.append(TAG_BR_VALUE);
				prePosition = m.end();
			} else {// 处理值标签

				// 获取替换内容
				Object tmp = this.content.get(tag.getName());
				String value = null;
				if (tmp != null && tmp instanceof String) {
					value = (String) tmp;
				}

				logger.debug("替换值=" + value);

				if (value != null) {
					this.result.append(value);
				} else {
					logger.error("模板解析异常:" + strTag + "标签无替换值");
				}
				prePosition = m.end();
			}
			logger.debug("解析结果:" + this.result);
		}
		this.result.append(_template.substring(prePosition));
	}

	/**
	 * 解析模板
	 * 
	 * @description:解析模板
	 * 
	 * @author: 陈作朋 2008-10-20
	 * 
	 * @return void
	 * @throws TemplateEngineException
	 */
	public void process() throws TemplateEngineException {
		logger.debug("模板解析开始");

		process(this.template, this.content);

		logger.debug("解析结果:" + this.result);
		logger.debug("模板解析完成");
	}

	/**
	 * 查找结束标签
	 * 
	 * @description:查找结束标签
	 * 
	 * @author: 陈作朋 2008-10-20
	 * 
	 * @param _template
	 *            模板
	 * @param tagName
	 *            标签名
	 * 
	 * @return int
	 */
	private int findEndTag(String _template, String tagName) {
		String endTag = ("{" + tagName + "$}").toUpperCase();
		return _template.toUpperCase().lastIndexOf(endTag);
	}

	/**
	 * 解析标签
	 * 
	 * @description:解析标签
	 * 
	 * @author: 陈作朋 2008-10-20
	 * 
	 * @param strTag
	 *            原始标签文本
	 * 
	 * @return Tag
	 * @throws TemplateEngineException
	 */
	private Tag parseTag(String strTag) throws TemplateEngineException {
		Tag tag = new Tag();
		String[] arr = this.splitTag(strTag);
		if (arr == null || arr.length == 0 || "".equals(arr[0])) {
			throw new TemplateEngineException("模板解析失败:空标签");
		}
		tag.setName(arr[0]);
		if (arr.length > 1) {
			for (int i = 1; i < arr.length; i++) {
				tag.getAttributes().add(arr[i]);
			}
		}
		return tag;
	}

	/**
	 * 拆分标签
	 * 
	 * @description:拆分标签文本
	 * 
	 *                     注：标签各部分之间用"|"分割
	 * 
	 * @author: 陈作朋 2008-10-20
	 * 
	 * @param strTag
	 *            原始标签文本
	 * 
	 * @return String[]
	 */
	private String[] splitTag(String strTag) {
		int index = 1;
		if (strTag.startsWith("{$")) {
			index = 2;
		}
		// 去除标签边界
		strTag = strTag.substring(index, strTag.length() - 1);
		// 格式化为大写
		strTag = strTag.toUpperCase();
		return strTag.split("\\|");
	}

	public Map<String, Object> getContent() {
		return this.content;
	}

	public void setContent(Map<String, Object> content) {
		this.content = content;
	}

	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public StringBuilder getResult() {
		return this.result;
	}

}
