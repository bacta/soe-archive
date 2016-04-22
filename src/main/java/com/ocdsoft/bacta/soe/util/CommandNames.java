package com.ocdsoft.bacta.soe.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CommandNames {

	private static Logger logger = LoggerFactory.getLogger("CommandNames");

	private static Properties prop;
	
	static {
		InputStream stream = CommandNames.class.getResourceAsStream("/commandnames.txt");
		prop = new Properties();
		try {
			prop.load(stream);
		} catch (IOException e) {
			logger.error("Error Loading CommandNames", e);
		}
	}

	public static String get(String propertyName) {
		if(!prop.containsKey(propertyName.toUpperCase()))
			return "Unknown";
		
		return prop.getProperty(propertyName.toUpperCase());
	}
	
	public static String get(int propertyName) {
		return get(Integer.toHexString(propertyName));
	}
}
