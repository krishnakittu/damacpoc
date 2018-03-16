package com.damacpoc.util;


import java.util.Properties;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: pankaj : pankaj.g@tbitsglobal.com Date: 5/30/12 this is utility
 *          class across the application.
 */
public class AppUtil {

	private static Logger logger = LoggerFactory.getLogger(AppUtil.class);
	private static Properties properties = null; 

	public static String getApplicationProperties(String key){
		if(properties == null){
			try{
				PropertiesConfiguration conf = new PropertiesConfiguration("application.properties");
				properties = ConfigurationConverter.getProperties(conf);
			}catch(Exception e){
				logger.error("Exception occured: ",e);
			}
		}
		return properties.getProperty(key);
	}

}
