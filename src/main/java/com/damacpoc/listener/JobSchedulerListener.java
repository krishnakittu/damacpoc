package com.damacpoc.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.damacpoc.service.JobScheduler;
import com.damacpoc.util.JobUtility;


public class JobSchedulerListener implements ServletContextListener{
	
	@SuppressWarnings("unused")
	private ServletContext context = null;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

		this.context = arg0.getServletContext();
		JobScheduler.shutdown();

	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {

		
		try {
			this.context = arg0.getServletContext();
			JobScheduler.start();
			JobUtility.saveEmailReciveJob();
			// scurve
			JobUtility.scurveJob();

		} catch (Exception e) {
			System.out.println("Exception occured while starting scheduler : "
					+ e.getMessage());
			e.printStackTrace();
		}

	}

}
