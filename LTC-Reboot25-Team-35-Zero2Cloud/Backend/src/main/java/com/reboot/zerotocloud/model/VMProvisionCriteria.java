package com.reboot.zerotocloud.model;

public class VMProvisionCriteria {
	
	private String projectId;
	
	private String zone;
	
	private String instanceName;
	
	private String tags;
	
	private String machineType;
	
	private String disks;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getMachineType() {
		return machineType;
	}

	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}

	public String getDisks() {
		return disks;
	}

	public void setDisks(String disks) {
		this.disks = disks;
	}
	

}
