package org.processmining.framework.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;

public class XStreamConverter {

	private Converter converter;
	private String name;
	private Class<?> type;

	public XStreamConverter(Converter converter, String name, Class<?> type) {
		this.converter = converter;
		this.name = name;
		this.type = type;
	}
	
	public void register(XStream xStream) {
		xStream.registerConverter(converter);
		xStream.aliasType(name, type);
	}
}
