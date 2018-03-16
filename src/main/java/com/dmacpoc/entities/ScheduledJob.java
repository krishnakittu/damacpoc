package com.dmacpoc.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ScheduledJob implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum TriggerState {
		NONE, NORMAL, PAUSED, COMPLETE, ERROR, BLOCKED
	};

	private String jobClass = "";

	private String name = "";

	private String group = "";

	private String jobDescription = "";

	private boolean durable = false;

	private boolean volatileProperty = false;

	private boolean recoverable = false;

	private Map<String, Object> parametersValues;// bheem= new HashMap<String,
													// Object>();

	private String cronExpression = "";

	private TriggerState state = TriggerState.NONE;

	private Date startDate = new Date();

	private Date endDate = new Date();

	private String evaluation = "";

	public ScheduledJob() {
		jobClass = "Select Class";
		name = "";
		group = "";
		jobDescription = "";
		durable = false;
		volatileProperty = false;
		recoverable = false;
		parametersValues = new HashMap<String, Object>();
		cronExpression = "";
		state = TriggerState.NONE;
		startDate = new Date();
		endDate = new Date();
		evaluation = "";
	}

	public ScheduledJob(String jobClass, String name, String group,
			String jobDescription, boolean durable, boolean volatileProperty,
			boolean recoverable, Map<String, Object> parametersValues,
			String cronExpression, TriggerState state, Date startDate,
			Date endDate, String evaluation) {
		super();
		this.jobClass = jobClass;
		this.name = name;
		this.group = group;
		this.jobDescription = jobDescription;
		this.durable = durable;
		this.volatileProperty = volatileProperty;
		this.recoverable = recoverable;
		this.parametersValues = parametersValues;
		this.cronExpression = cronExpression;
		this.state = state;
		this.startDate = startDate;
		this.endDate = endDate;
		this.evaluation = evaluation;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(String evaluation) {
		this.evaluation = evaluation;
	}

	public TriggerState getState() {
		return state;
	}

	public void setState(TriggerState state) {
		this.state = state;
	}

	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	// public boolean isDurable() {
	// return durable;
	// }

	public boolean getDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	// public boolean isVolatileProperty() {
	// return volatileProperty;
	// }

	public boolean getVolatileProperty() {
		return volatileProperty;
	}

	public void setVolatileProperty(boolean volatileProperty) {
		this.volatileProperty = volatileProperty;
	}

	// public boolean isRecoverable() {
	// return recoverable;
	// }

	public boolean getRecoverable() {
		return recoverable;
	}

	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}

	public Map<String, Object> getParametersValues() {
		return parametersValues;
	}

	public void setParametersValues(Map<String, Object> parametersValues) {
		this.parametersValues = parametersValues;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public void updateFrom(ScheduledJob job) {

		jobClass = job.getJobClass();

		name = job.getName();

		group = job.getGroup();

		jobDescription = job.getJobDescription();

		durable = job.getDurable();

		volatileProperty = job.getVolatileProperty();

		recoverable = job.getRecoverable();

		parametersValues = job.getParametersValues();

		cronExpression = job.getCronExpression();

		state = job.getState();

		startDate = job.getStartDate();

		endDate = job.getEndDate();

		evaluation = job.getEvaluation();

	}

}