package com.zx.j2json.core;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JTJson {
	/**
	 * 将对象转换成字符串,以StringBuilder的形式输出
	 * 
	 * @param obj
	 * @return
	 */
	public StringBuilder getJsonAsStringBuilder(Object obj) {
		if (obj == null)
			return null;
		StringBuilder sb = new StringBuilder(256);
		return getJsonAsStringBuilder(sb, obj);
	}

	@SuppressWarnings("unchecked")
	private StringBuilder getJsonAsStringBuilder(StringBuilder sb, Object obj) {
		Class<? extends Object> clazz=obj.getClass();
		String className=clazz.getName();
		switch(checkType(obj)){
		case 16: {doArray(sb,obj);break;}
		case 8 : {doCollection(sb,(Collection<?>)obj);break;}
		case 4 : {doMap(sb,(Map<String, Object>)obj);break;}
		case 2 : {doObject(sb,obj);break;}
		default :doBasic(sb,obj); 
		}
		return sb;
	}

	private void doArray(StringBuilder sb, Object obj) {
		int length=Array.getLength(obj);
		sb.append('[');
		for (int i = 0; i < length; i++) {
			getJsonAsStringBuilder(sb,Array.get(obj, i));
		}
		sb.append(']');
		
	}


	private int checkType(Object obj) {
		Class<? extends Object> clazz=obj.getClass();
		if(clazz.isArray())return 16;
		if(obj instanceof Collection) return 8;
		if(obj instanceof Map) return 4;
		String className=clazz.getName();
		//TODO obj为基本类型和字符串类型时的操作
		return 4;
	}

	private void doBasic(Object obj, Object obj2) {
		// TODO Auto-generated method stub
		
	}

	private void doObject(StringBuilder sb, Object obj) {
		sb.append('{');
		Class clazz=obj.getClass();
		for (Method method : clazz.getMethods()) {
			String methodName=method.getName();
			if(methodName.startsWith("get")&&!methodName.equals("getClass")){
				sb.append(Character.toLowerCase(methodName.charAt(3))+methodName.substring(4));
				sb.append(':');
				try {
					getJsonAsStringBuilder(sb, method.invoke(obj));
				} catch (IllegalAccessException | IllegalArgumentException| InvocationTargetException e) {
					//never do this;
					e.printStackTrace();
				}
				sb.append(',');
			}
		}
		if(sb.charAt(sb.length()-1)==',')
		sb.deleteCharAt(sb.length()-1);
		sb.append('}');
	}

	private void doMap(StringBuilder sb, Map<String, Object> obj) {
		sb.append('{');
		Set<Entry<String,Object>> entrySet=obj.entrySet();
		for (Entry<String,Object> entry : entrySet) {
			sb.append((String)entry.getKey());
			sb.append(':');
			getJsonAsStringBuilder(sb, entry.getValue());
			sb.append(',');
		}
		if(sb.charAt(sb.length()-1)==',')
		sb.deleteCharAt(sb.length()-1);
		sb.append('}');
		
	}

	private void doCollection(StringBuilder sb, Collection<?> obj) {
		sb.append('[');
		for (Object object : obj) {
			getJsonAsStringBuilder(sb, object);
		}
		sb.append(']');
		
	}
}
