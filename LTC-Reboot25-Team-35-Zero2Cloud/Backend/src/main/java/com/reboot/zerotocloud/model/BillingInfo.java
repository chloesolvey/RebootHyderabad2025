package com.reboot.zerotocloud.model;

import java.util.Map;

public class BillingInfo {
    private double currentMonthSpend;
    private double previousMonthSpend;
    private double projectedSpend;
    private Map<String, Double> serviceWiseCosts;

    public BillingInfo() {}

    public BillingInfo(double currentMonthSpend, double previousMonthSpend, double projectedSpend, Map<String, Double> serviceWiseCosts) {
        this.currentMonthSpend = currentMonthSpend;
        this.previousMonthSpend = previousMonthSpend;
        this.projectedSpend = projectedSpend;
        this.serviceWiseCosts = serviceWiseCosts;
    }

    public double getCurrentMonthSpend() {
        return currentMonthSpend;
    }

    public void setCurrentMonthSpend(double currentMonthSpend) {
        this.currentMonthSpend = currentMonthSpend;
    }

    public double getPreviousMonthSpend() {
        return previousMonthSpend;
    }

    public void setPreviousMonthSpend(double previousMonthSpend) {
        this.previousMonthSpend = previousMonthSpend;
    }

    public double getProjectedSpend() {
        return projectedSpend;
    }

    public void setProjectedSpend(double projectedSpend) {
        this.projectedSpend = projectedSpend;
    }

    public Map<String, Double> getServiceWiseCosts() {
        return serviceWiseCosts;
    }

    public void setServiceWiseCosts(Map<String, Double> serviceWiseCosts) {
        this.serviceWiseCosts = serviceWiseCosts;
    }
}