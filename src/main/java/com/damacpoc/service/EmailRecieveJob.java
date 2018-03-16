package com.damacpoc.service;

import java.io.IOException;

import javax.mail.MessagingException;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class EmailRecieveJob implements PocJob {
	private static Logger logger = LoggerFactory
			.getLogger(EmailRecieveJob.class);



	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		logger.info("Receive emails called...");
		JobDetail job = context.getJobDetail();
		EmailReci rs=new EmailReci();
		try {
			rs.receiveMails();
		} catch (MessagingException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MDC.put("job", job.getKey().getGroup() + "_" + job.getKey().getName()
				+ "_job");
		logger.info("Receive emails completed...");
	}


}

