package com.zx.j2json.core;

import java.io.Writer;

import org.omg.CORBA_2_3.portable.OutputStream;

/**
 * 主接口,该接口包含java对象转换为json对象的所有方法(包含数组,对象,基本类型)
 * @version0.1
 * @author acer
 *
 */
public interface ToJson {
	StringBuilder getJsonAsStringBuilder(Object obj);
	 void getJsonAsWriter(Object obj,Writer writer);
	 String getJsonAsString(Object obj);
	 void getJsonAsStream(Object obj,OutputStream out);
	 char[] getJsonAsCharsequence(Object obj);
}
