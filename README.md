一个简单的模板引擎。

1.支持4种标签:
 
        迭代标签： 格式：{$ITERATE|集合对象}迭代内容{ITERATE$}
        
          说明：
            1,此标签必须有开始和结束标签。
            2,集合对象必须是实现了Iterable接口的集合对象，如：List,Set。 3,支持嵌套。
 
        迭代子标签： 格式：{$ITEM|属性} 
        
          说明：
            1,属性值表示的是集合对象元素(Map)的Key。
            2,此标签必须嵌套于迭代标签内部使用。
 
        退格标签： 格式：{$BACKSPACE} 
        
           作用: 删除当前结果的最后一个字符
 
        值标签： 格式：{KEY} 
        
           说明：
             1,KEY表示的是用于替换标签的内容的Map对象中的Key
 
        换行标签： 格式：{$BR} 
        
           作用：换行。
 
2.支持标签的嵌套使用;
 
3.使用实例:

         String template="111{AAA}222{$br}{$ITERATE|CON}aa{$ITEM|A}bb{CCC}{$ITERATE|arr}{$ITEM|ITEM}{ITERATE$}ccc,{ITERATE$}{$BACKSPACE}|333{BBB}444"; 
         Map map=new java.util.HashMap();
         map.put("AAA", "+"); 
         map.put("BBB", "-"); 
         map.put("CCC","%");
         List list=new ArrayList();
         Map map1=new java.util.HashMap(); 
         map1.put("A", "你");
         list.add(map1);
         Map map2=new java.util.HashMap(); 
         map2.put("A", "我");
         list.add(map2);
         Map map3=new java.util.HashMap(); 
         map3.put("A", "他");
         list.add(map3);
         List arr=new ArrayList();
         Map map4=new java.util.HashMap(); 
         map4.put("ITEM", "en");
         arr.add(map4); 
         arr.add(map4); 
         arr.add(map4);
         map3.put("ARR",arr);
         map.put("CON", list); 
         TempleteEngine tr=new
         TempleteEngine(); 
         tr.setContent(map);
         tr.setTemplate(template); 
         tr.process();
 
         输出结果:111+222 aa你bb%ccc aa我bb%ccc aa他bb%enenenccc 333-444


