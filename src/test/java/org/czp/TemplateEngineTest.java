package org.czp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.czp.TemplateEngine;
import org.czp.TemplateEngineException;
import org.junit.Test;


/**
 *
 *
 * @author dylan.chen 2008-10-20
 * 
 */
public class TemplateEngineTest {

	@Test
	public void test1() throws TemplateEngineException{
		String template="111{AAA}222{$br}{$ITERATE|CON}aa{$ITEM|A}bb{CCC}{$ITERATE|arr}{$ITEM|ITEM}{ITERATE$}ccc,{ITERATE$}{$BACKSPACE}|333{BBB}444".toLowerCase();
		Map<String, Object> map=new java.util.HashMap<String, Object>();
		
		map.put("AAA", "+");
		map.put("BBB", "-");
		map.put("CCC", "%");
		
		List<Map<String, Object>> list=new ArrayList<Map<String, Object>>();
		
        Map<String, Object> map1=new java.util.HashMap<String, Object>();		
		map1.put("A", "你");
		list.add(map1);
		
		Map<String, Object> map2=new java.util.HashMap<String, Object>();
		map2.put("A", "我");
		list.add(map2);
		
		Map<String, Object> map3=new java.util.HashMap<String, Object>();
		map3.put("A", "他");
		list.add(map3);
		
		List<Map<String, Object>> arr=new ArrayList<Map<String, Object>>();
		
		Map<String, Object> map4=new java.util.HashMap<String, Object>();
		map4.put("ITEM", "en");
		arr.add(map4);
		arr.add(map4);
		arr.add(map4);
		map3.put("ARR",arr);
		
		map.put("CON", list);
		TemplateEngine tr=new TemplateEngine();
		tr.setContent(map);
		tr.setTemplate(template);
		tr.process();
        System.out.println("--------"+tr.getResult());
	}
	
}
