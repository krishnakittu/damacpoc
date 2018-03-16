package com.damacpoc.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.damacpoc.service.JobScheduler;
import com.dmacpoc.entities.ScheduledJob;



public class JobUtility {
	
	private static final Logger logger = LoggerFactory.getLogger(JobUtility.class);

	public static List<ScheduledJob> getJobs() {
		List<ScheduledJob> jobs = new ArrayList<ScheduledJob>();
		try {
			List<String> groupNames = JobScheduler.scheduler.getJobGroupNames();
			if (groupNames != null) {
				for (String group : groupNames) {
					for (JobKey key : JobScheduler.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
						JobDetail detail = JobScheduler.scheduler.getJobDetail(key);
						ScheduledJob job = new ScheduledJob();
						job.setName(detail.getKey().getName());
						job.setGroup(group);
						job.setJobDescription(detail.getDescription());
						job.setDurable(detail.isDurable());
						job.setRecoverable(detail.requestsRecovery());
						job.setJobClass(detail.getJobClass().getSimpleName());
						JobDataMap jdm = detail.getJobDataMap();
						Map<String, Object> map = new HashMap<String, Object>();
						String[] keys = jdm.getKeys();
						for (String key1 : keys) {
							map.put(key1, jdm.get(key1));
						}
						job.setParametersValues(map);
						List<? extends Trigger> triggers = JobScheduler.scheduler.getTriggersOfJob(key);
						if (triggers.size() > 0) {
							if (triggers.get(0) instanceof CronTrigger) {
								CronTrigger trigger = (CronTrigger) triggers.get(0);
								job.setCronExpression(trigger.getCronExpression());
								job.setState(
										getTriggerState(JobScheduler.scheduler.getTriggerState(trigger.getKey())));
								job.setEndDate(trigger.getEndTime());
								job.setStartDate(trigger.getStartTime());
							} else {
								SimpleTrigger trigger = (SimpleTrigger) triggers.get(0);
								job.setState(
										getTriggerState(JobScheduler.scheduler.getTriggerState(trigger.getKey())));
								job.setEndDate(trigger.getEndTime());
								job.setStartDate(trigger.getStartTime());
							}
						}
						jobs.add(job);

					}
				}
			}
		} catch (Exception e) {
			logger.info("exception while getting jobs from scheduler:" + e);
		}
		return jobs;
	}

	public static void saveJob(ScheduledJob job, boolean edit, long clientId) {
		JobDataMap map = new JobDataMap();
		Map<String, Object> values = job.getParametersValues();
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			map.put(entry.getKey(), entry.getValue());
		}
		map.put("Client Id", clientId);
		@SuppressWarnings("rawtypes")
		Class jobClass = null;
		try {
			jobClass = Class.forName("com.tbitsglobal.dms.scheduler." + job.getJobClass());
		} catch (Exception e) {
			logger.info("exception in finding class:" + e);
		}

		@SuppressWarnings("unchecked")
		JobDetail detail = JobBuilder.newJob(jobClass).withIdentity(job.getName(), job.getGroup())
				.withDescription(job.getJobDescription()).requestRecovery(job.getRecoverable())
				.storeDurably(job.getDurable()).usingJobData(map).build();

		Trigger trigger = TriggerBuilder.newTrigger()
				.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression())).endAt(job.getEndDate())
				.startAt(job.getStartDate()).forJob(detail).build();

		try {

			if (edit) {
				JobScheduler.scheduler.deleteJob(new JobKey(job.getName(), job.getGroup()));
			}

			logger.info("trigger:" + trigger);

			logger.info("added");

			JobScheduler.scheduler.scheduleJob(detail, trigger);
			logger.info("scheduled");

		} catch (Exception e) {
			logger.info("Exception while scheduling job" + e);
		}
	}

	public static void saveEmailReciveJob() {

		@SuppressWarnings("rawtypes")
		Class jobClass = null;
		try {
			jobClass = Class.forName("com.damacpoc.service." + "EmailRecieveJob");
		} catch (Exception e) {
			logger.info("exception in finding class:" + e);
		}

		@SuppressWarnings("unchecked")
		JobDetail detail = JobBuilder.newJob(jobClass).withIdentity("EmailRecieveJob", "Emailer")
				.withDescription("This job calls emailer to recieve emails").requestRecovery(true).storeDurably(true)
				.build();

		Trigger trigger = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(2))
				.forJob(detail).build();

		try {

			logger.info("trigger:" + trigger);

			logger.info("added");

			JobScheduler.scheduler.scheduleJob(detail, trigger);
			logger.info("scheduled");

		} catch (Exception e) {
			logger.info("Exception while scheduling job" + e);
		}

	}

	// scurve
	public static void scurveJob() {

		@SuppressWarnings("rawtypes")
		Class jobClass = null;
		try {
			jobClass = Class.forName("com.damacpoc.service." + "ScurveJob");
		} catch (Exception e) {
			logger.info("exception in finding class for scurve:" + e);
		}

		// checking whether already defined or not
		List<ScheduledJob> jobs = getJobs();
		for (ScheduledJob job : jobs) {
			if (job.getName().equalsIgnoreCase("ScurveExecutionJob") && job.getGroup().equalsIgnoreCase("SCurve"))
				deleteJob("ScurveExecutionJob", "SCurve");
		}

		// bheem clearing jobs after use
		if (jobs != null)
			jobs.clear();

		@SuppressWarnings("unchecked")
		JobDetail detail = JobBuilder.newJob(jobClass).withIdentity("ScurveExecutionJob", "SCurve")
				.withDescription("This job calls SCurve to execute").requestRecovery(true).storeDurably(true).build();

		Trigger trigger = TriggerBuilder.newTrigger().withSchedule(SimpleScheduleBuilder.repeatHourlyForever(24))
				.startNow().forJob(detail).build();

		try {

			logger.info("trigger:" + trigger);

			logger.info("added");

			JobScheduler.scheduler.scheduleJob(detail, trigger);
			logger.info("SCURVE scheduled");

		} catch (Exception e) {
			logger.info("Exception while scheduling scurve job" + e);
		}

	}

	public static void deleteJob(String name, String group) {
		try {
			if (JobScheduler.scheduler.isStarted()) {
				JobScheduler.scheduler.interrupt(new JobKey(name, group));
			}
			JobScheduler.scheduler.deleteJob(new JobKey(name, group));
		} catch (Exception e) {
			logger.info("Exception while deleting job:" + e);
			e.printStackTrace();
		}
	}

	/**
	 * This method delete all jobs from scheduler one by one
	 * 
	 * @param jobs
	 * @author Nitin Gupta
	 * @return return successfully delete jobs
	 */
	public static List<ScheduledJob> deleteMultipleJobs(ScheduledJob[] jobs) {
		List<ScheduledJob> successfullyDeleteJobs = new ArrayList<ScheduledJob>();

		try {
			for (ScheduledJob job : jobs) {
				if (JobScheduler.scheduler.isStarted()) {
					JobScheduler.scheduler.interrupt(new JobKey(job.getName(), job.getGroup()));
				}
				JobScheduler.scheduler.deleteJob(new JobKey(job.getName(), job.getGroup()));
				successfullyDeleteJobs.add(job);
			}
			return successfullyDeleteJobs;
		} catch (Exception e) {
			logger.info("Exception while deleting ALL job:" + e);
			e.printStackTrace();
			return successfullyDeleteJobs;
		}
	}

	public static void pauseJob(String name, String group) {
		try {
			JobScheduler.scheduler.pauseJob(new JobKey(name, group));
		} catch (Exception e) {
			logger.info("Exception while pausing job:" + e);
		}
	}

	public static void resumeJob(String name, String group) {
		try {
			JobScheduler.scheduler.resumeJob(new JobKey(name, group));
		} catch (Exception e) {
			logger.info("Exception while resuming job:" + e);
		}
	}

	public static void executeJob(String name, String group) {
		try {
			JobScheduler.scheduler.triggerJob(new JobKey(name, group));
			logger.info("Job start executing");
		} catch (Exception e) {
			logger.info("Exception while executing job:" + e);
		}
	}

	
	public static void main(String[] args) {
		JobUtility.deleteJob("local ldap integration", "default");
	}

	public static ScheduledJob.TriggerState getTriggerState(Trigger.TriggerState state) {
		switch (state) {
		case NONE:
			return ScheduledJob.TriggerState.NONE;

		case NORMAL:
			return ScheduledJob.TriggerState.NORMAL;

		case PAUSED:
			return ScheduledJob.TriggerState.COMPLETE;
		case ERROR:
			return ScheduledJob.TriggerState.ERROR;
		case BLOCKED:
			return ScheduledJob.TriggerState.BLOCKED;
		default:
			return ScheduledJob.TriggerState.NONE;
		}
	}
	
	
	
}
