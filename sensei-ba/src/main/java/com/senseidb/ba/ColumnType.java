package com.senseidb.ba;

import org.apache.avro.Schema.Type;

public enum ColumnType {
	INT,LONG, STRING, FLOAT; 
	 
	public static ColumnType valueOfStr(String name) {
		name = name.toUpperCase();
		if ("DOUBLE".equals(name)) {
			return FLOAT;
		}
		return valueOf(name);
	}
	public static ColumnType valueOf(Class<?> cls) {
		if (cls == int.class || cls == Integer.class) {
			return INT;
		}
		if (cls == long.class || cls == Long.class) {
			return LONG;
		}
		if (cls == String.class) {
			return STRING;
		}
		if (cls == Double.class || cls == Float.class || cls == float.class || cls == double.class) {
			return FLOAT;
		}
		throw new UnsupportedOperationException(cls.toString());
	}
	public static ColumnType valueOf(Type type) {
	    if (type == Type.INT) {
			return INT;
		}
		if (type == Type.LONG) {
			return LONG;
		}
		if (type == Type.STRING) {
			return STRING;
		}
		if (type == Type.DOUBLE || type == Type.FLOAT) {
			return FLOAT;
		}
		throw new UnsupportedOperationException(type.toString());
	}
}
