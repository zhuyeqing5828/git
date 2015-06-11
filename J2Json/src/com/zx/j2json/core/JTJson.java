package com.zx.j2json.core;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class JTJson {
	private  int op;
	public void setOp(int op) {
		this.op = op;
	}
	public int getOp() {
		return op;
	}
	
	public JTJson() {
		op=1;
	}
	public JTJson(int op) {
		this.op = op;
	}
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
		Class<? extends Object> clazz = obj.getClass();
		String className = clazz.getName();
		switch (checkType(obj)) {
		case 16: {
			doArray(sb, obj);
			break;
		}
		case 8: {
			doCollection(sb, (Collection<?>) obj);
			break;
		}
		case 4: {
			doMap(sb, (Map<String, Object>) obj);
			break;
		}
		case 2: {
			doObject(sb, obj);
			break;
		}
		case 0:
			return sb;
		default:
			doBasic(sb, obj);
		}
		return sb;
	}

	private void doArray(StringBuilder sb, Object obj) {
		int length = Array.getLength(obj);
		sb.append('[');
		for (int i = 0; i < length; i++) {
			getJsonAsStringBuilder(sb, Array.get(obj, i));
		}
		sb.append(']');
	}

	private int checkType(Object obj) {
		if (obj == null)
			return 1;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.isArray())
			return 16;
		if (obj instanceof Collection)
			return 8;
		if (obj instanceof Map)
			return 4;
		if (obj instanceof Number || obj instanceof Character
				|| obj instanceof Boolean || obj instanceof CharSequence)
			return 1;
		return 2;
	}
	//TODO TEST 待优化
	private void doBasic(StringBuilder sb, Object obj) {
		if (obj==null||obj instanceof Number||obj instanceof Boolean) 
			sb.append(obj);
			else if (obj instanceof CharSequence||obj instanceof Character){
				sb.append('"');
				sb.append(inverce(obj.toString(),op));
				sb.append('"');
				}
	}

	private CharSequence inverce(String string,int op) {
		/*
		 * 转义策略
		 * 	转义程度
		 * 		0:不转义
		 * 		1:快速转义 仅包括"\ / " "
		 * 		2:完全转义  包括1 和 \n \f \b \r \t 
		 */
		//0:
		if(op==0)return string;
		//1:
		if(op==1)return string.replaceAll("(\\\\|/|\\\")","\\\\$1");//效率不确定
		//2:
		int length=string.length();
		int lastCopy=0;
		StringBuilder sb=new StringBuilder(length+length>>5);
		for(int i=0;i<length;i++){
			char ch=string.charAt(i);
			if(ch==34||ch==92||ch==47){
			}else if(ch<16){
				String t;
				switch (ch){
				case 8  : t="\\b";break;
				case 9  : t="\\t";break;
				case 10 : t="\\n";break;
				case 12 : t="\\f";break;
				case 13 : t="\\r";break;
				}
		} else continue;
		sb.append(string, lastCopy, i);// 因jdk对其的实现并不理想,所以未达到最高效率
		sb.append("\\" + ch);
		lastCopy = i;
		continue;
		}
		if(lastCopy!=length)sb.append(string, lastCopy, length);// 保证结尾的拷贝
		return sb;
	}

	private void doObject(StringBuilder sb, Object obj) {
		sb.append('{');
		Class clazz = obj.getClass();
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (methodName.startsWith("get") && !methodName.equals("getClass")) {
				sb.append(Character.toLowerCase(methodName.charAt(3))
						+ methodName.substring(4));
				sb.append(':');
				try {
					getJsonAsStringBuilder(sb, method.invoke(obj));
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					// never do this;
					e.printStackTrace();
				}
				sb.append(',');
			}
		}
		if (sb.charAt(sb.length() - 1) == ',')
			sb.deleteCharAt(sb.length() - 1);
		sb.append('}');
	}

	private void doMap(StringBuilder sb, Map<String, Object> obj) {
		sb.append('{');
		Set<Entry<String, Object>> entrySet = obj.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			sb.append((String) entry.getKey());
			sb.append(':');
			getJsonAsStringBuilder(sb, entry.getValue());
			sb.append(',');
		}
		if (sb.charAt(sb.length() - 1) == ',')
			sb.deleteCharAt(sb.length() - 1);
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
