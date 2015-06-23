package com.zx.j2json.core;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.zx.j2json.exception.JTJsonException;

public class JTJson {
	/*
	 * 转义策略
	 * 	转义程度
	 * 		0:不转义
	 * 		1:快速转义 仅包括"\ / " "
	 * 		2:完全转义  包括1 和 \n \f \b \r \t 
	 */
	private  int op;
	/*
	 * 对象暂存
	 */
	private HashMap<Object,String> map;
	public void setOp(int op) {
		this.op = op;
	}
	public int getOp() {
		return op;
	}
	
	public JTJson() {
		this(1);
	}
	public JTJson(int op) {
		this.op = op;
	}
	/**
	 * 将对象转换成字符串,以StringBuilder的形式返回
	 * 
	 * @param obj 需要转换的字符串
	 * @return 返回转换的结果
	 */
	public StringBuilder getJsonAsStringBuilder(Object obj) {
		if (obj == null)
			return null;
		StringBuilder sb = new StringBuilder(256);
		return getJsonAsStringBuilder(sb, obj);
	}
/**
 *将对象转换成json字符序列
 *
 */
	@SuppressWarnings("unchecked")
	private StringBuilder getJsonAsStringBuilder(StringBuilder sb, Object obj) {
		switch (checkType(obj)) {//检查对象类型,包含重复的对象
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
		case 0:{
			doExist(sb,obj);break;
			}
		default:
			doBasic(sb, obj);
		}
		return sb;
	}
		
	private void doExist(StringBuilder sb, Object obj) {
		String value=map.get(obj);
		sb.append(value);
	}
	/**
	 * @param sb
	 * @param obj
	 */
	private void doArray(StringBuilder sb, Object obj) {
		int length = Array.getLength(obj);
		sb.append('[');
		for (int i = 0; i < length; i++) {
			getJsonAsStringBuilder(sb, Array.get(obj, i));
			sb.append(',');
		}
		removeComma(sb);
		sb.append(']');
	}
	private void removeComma(StringBuilder sb) {
		if (sb.charAt(sb.length() - 1) == ',')
			sb.deleteCharAt(sb.length() - 1);
	}
/**
 * 叫检数据类型
 * 数据类型和返回结果如下
 * null||包装类||字符序列->>1
 * 该对象引用的是json化过得对象->>0
 * 该对象是数组->>16
 * 该对象是集合->>8
 * 该对象是map-->4
 * 其他 -->抛出JTJsonException("unimplementException") 正常情况下不会出现
 * @param obj
 * @return
 */
	private int checkType(Object obj) {
		if (obj == null||obj instanceof Number || obj instanceof Character
				|| obj instanceof Boolean || obj instanceof CharSequence)
			return 1;
		if(!map.containsKey(obj))
			return 0;
		Class<? extends Object> clazz = obj.getClass();
		if (clazz.isArray())
			return 16;
		if (obj instanceof Collection)
			return 8;
		if (obj instanceof Map)
			return 4;
		throw new JTJsonException("unimplementException");
	}
	/**
	 * 对基本类型和字符串和空值的操作
	 * @param sb
	 * @param obj
	 */
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
/**
 * 对字符串的转义操作
 * @param string
 * @param op
 * @return
 */
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
	//	if(op==1)return string.replaceAll("(\\\\|/|\\\")","\\$1");//效率不确定  $1不生效
		//2:
		int length=string.length();
		int lastCopy=0;
		StringBuilder sb=new StringBuilder(length+length>>5);
		for(int i=0;i<length;i++){
			char ch=string.charAt(i);
			if(ch==34||ch==92||ch==47){
			}else if(op==2&&ch<16){
				switch (ch){
				case 8  : ch='b';break;
				case 9  : ch='t';break;
				case 10 : ch='n';break;
				case 12 : ch='f';break;
				case 13 : ch='r';break;
				}
		} else continue;
		sb.append(string, lastCopy, i);// 因jdk对其的实现并不理想,所以未达到最高效率
		sb.append("\\" + ch);
		lastCopy = i+1;
		continue;
		}
		if(lastCopy!=length)sb.append(string, lastCopy, length);// 保证结尾的拷贝
		return sb;
	}
/**
 * 对对象的操作
 * @param sb
 * @param obj
 */
	private void doObject(StringBuilder sb, Object obj) {
		Object key = null;
		String value;
		sb.append('{');
		Class<?> clazz = obj.getClass();
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (methodName.startsWith("get") && !methodName.equals("getClass")) {
				value=Character.toLowerCase(methodName.charAt(3))+ methodName.substring(4);
				sb.append(value);
				sb.append(':');
				try {
					key= method.invoke(obj);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					// never do this;
					e.printStackTrace();
				}
				map.put(key, value);
				getJsonAsStringBuilder(sb,key);
				sb.append(',');
			}
		}
		removeComma(sb);
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
		removeComma(sb);
		sb.append('}');
	}

	private void doCollection(StringBuilder sb, Collection<?> obj) {
		sb.append('[');
		for (Object object : obj) {
			getJsonAsStringBuilder(sb, object);
			sb.append(',');
		}
		removeComma(sb);
		sb.append(']');
	}
}
