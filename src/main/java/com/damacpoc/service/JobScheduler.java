package com.damacpoc.service;

import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class JobScheduler {
	
	private static final Logger logger = LoggerFactory
			.getLogger(JobScheduler.class);

	public static Scheduler scheduler = null;

	static {
		try {
			Properties properties = new Properties();
			properties.put("org.quartz.jobStore.tablePrefix","qrtz_");
			properties.put("org.quartz.jobStore.isClustered","TRUE");
			properties.put("org.quartz.jobStore.class","org.quartz.impl.jdbcjobstore.JobStoreTX");
			properties.put("org.quartz.dataSource.qzDS.password","mysql");
			properties.put("org.quartz.dataSource.qzDS.URL","jdbc:mysql://localhost/emaildb");
			properties.put("org.quartz.dataSource.qzDS.driver","com.mysql.jdbc.Driver");
			properties.put("org.quartz.jobStore.driverDelegateClass","org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
			properties.put("org.quartz.jobStore.dataSource","qzDS");
			properties.put("org.quartz.scheduler.instanceId","AUTO");
			properties.put("org.quartz.dataSource.qzDS.user","root");
			properties.put("org.quartz.threadPool.threadPriority","5");
			properties.put("org.quartz.jobStore.useProperties","FALSE");
			properties.put("org.quartz.dataSource.qzDS.maxConnections","30");
			properties.put("org.quartz.threadPool.threadCount","5");
			properties.put("org.quartz.threadPool.class","org.quartz.simpl.SimpleThreadPool");
			properties.put("org.quartz.jobStore.misfireThreshold","60000");
			properties.put("org.quartz.scheduler.skipUpdateCheck","TRUE");

			StdSchedulerFactory factory = new StdSchedulerFactory();

			// Initialize the factory with our properties object.
			factory.initialize(properties);

			// Obtain the scheduler object.
			scheduler = factory.getScheduler();
		} catch (Exception e) {
			logger.info("Exception in starting scheduler:" + e);
		}
	}

	public static void start() {

		logger.info("Starting Scheduler...");

		try {
			scheduler.start();
		} catch (Exception e) {
			logger.info("Exception in starting scheduler:" + e);
		}

	}

	public static void shutdown() {

		logger.info("Stoping Scheduler...");

		try {
			scheduler.shutdown(true);
		} catch (Exception e) {
			logger.info("Exception in stoping scheduler:" + e);
		}

	}

	public Scheduler getScheduler() {
		return scheduler;
	}

}
