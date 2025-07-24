package com.reboot.zerotocloud.model;

public class GcpVmInfo {
    private String name;
    private String status;
    private String machineType;
    private String zone;
    private String os;
    private String createdTime;
    private String memoryUsage;

    // Constructors
    public GcpVmInfo() {}

    public GcpVmInfo(String name, String status, String machineType, String zone, String os, String createdTime, String memoryUsage) {
        this.name = name;
        this.status = status;
        this.machineType = machineType;
        this.zone = zone;
        this.os = os;
        this.createdTime = createdTime;
        this.memoryUsage = memoryUsage;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }

    public String getCreatedTime() { return createdTime; }
    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }

    public String getMemoryUsage() { return memoryUsage; }
    public void setMemoryUsage(String memoryUsage) { this.memoryUsage = memoryUsage; }
}
