package com.adobe.raven.dto.workfront;

import lombok.Data;

import java.util.ArrayList;

public @Data
class WorkfrontResponseData {
	
	private String ID;
	private String name;
	private WorkfrontOwner owner;
	private WorkfrontOwner sponsor;
	private String plannedCompletionDate;
	private String enteredByID;
	private String status;
	private String description;
	private WorkfrontCustomForm parameterValues;
	private String templateID;
	private ArrayList<WorkfrontTasks> tasks;
	public String getID() {
		return ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public WorkfrontOwner getOwner() {
		return owner;
	}
	public void setOwner(WorkfrontOwner owner) {
		this.owner = owner;
	}
	public String getPlannedCompletionDate() {
		return plannedCompletionDate;
	}
	public void setPlannedCompletionDate(String plannedCompletionDate) {
		this.plannedCompletionDate = plannedCompletionDate;
	}
	public String getEnteredByID() {
		return enteredByID;
	}
	public void setEnteredByID(String enteredByID) {
		this.enteredByID = enteredByID;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public WorkfrontCustomForm getParameterValues() {
		return parameterValues;
	}
	public void setParameterValues(WorkfrontCustomForm parameterValues) {
		this.parameterValues = parameterValues;
	}
	public String getTemplateID() {
		return templateID;
	}
	public void setTemplateID(String templateID) {
		this.templateID = templateID;
	}
	public ArrayList<WorkfrontTasks> getTasks() {
		return tasks;
	}
	public void setTasks(ArrayList<WorkfrontTasks> tasks) {
		this.tasks = tasks;
	}

	
}
