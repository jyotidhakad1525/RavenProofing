package com.adobe.raven.dto.workfront;

public class WorkfrontTasks {

	private String ID;
	private String name;
	private String assignedToID;
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
	public String getAssignedToID() {
		return assignedToID;
	}
	public void setAssignedToID(String assignedToID) {
		this.assignedToID = assignedToID;
	}
	
	
}
