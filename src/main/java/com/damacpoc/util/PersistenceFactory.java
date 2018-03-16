package com.damacpoc.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by IntelliJ IDEA. UserDao: pankaj Date: 4/12/11 Time: 11:42 AM To
 * change this template use File | Settings | File Templates.
 */
public class PersistenceFactory {
	public static final Logger logger = LoggerFactory
			.getLogger(PersistenceFactory.class);
	public static PersistenceManagerFactory pmf = null;
	

	public static PersistenceManager getPersistenceManager(long clientId){
		
		    Properties p=new Properties();		
			p.setProperty("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
			p.setProperty("javax.jdo.option.ConnectionURL", "jdbc:mysql://localhost:3306/emaildb");
			p.setProperty("javax.jdo.option.ConnectionDriverName", "com.mysql.jdbc.Driver");
			p.setProperty("javax.jdo.option.ConnectionUserName", "root");
			p.setProperty("javax.jdo.option.ConnectionPassword", "mysql");
			p.setProperty("datanucleus.autoCreateSchema", "true");
			p.setProperty("datanucleus.schema.autoCreateTables", "true");
			p.setProperty("datanucleus.schema.autoCreateColumns", "true");
			p.setProperty("datanucleus.schema.autoCreateConstraints", "true");
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(p);
			PersistenceManager pm = pmf.getPersistenceManager();
			return pm;
	    
	}
	
}

