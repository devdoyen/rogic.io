package com.devdoyen.nemologic.dto;

public class TelemetryStatsResponse {
    private long dailyUniqueVisitors;
    private long totalUniqueVisitors;
    private long totalVisits;
    private long totalAttempts;
    private long totalClears;
    private double uptimeRatio;
    private double mtbf;
    private double mttr;

    public TelemetryStatsResponse() {
    }

    public TelemetryStatsResponse(long dailyUniqueVisitors, long totalUniqueVisitors, long totalVisits,
                                  long totalAttempts, long totalClears, double uptimeRatio,
                                  double mtbf, double mttr) {
        this.dailyUniqueVisitors = dailyUniqueVisitors;
        this.totalUniqueVisitors = totalUniqueVisitors;
        this.totalVisits = totalVisits;
        this.totalAttempts = totalAttempts;
        this.totalClears = totalClears;
        this.uptimeRatio = uptimeRatio;
        this.mtbf = mtbf;
        this.mttr = mttr;
    }

    public long getDailyUniqueVisitors() {
        return dailyUniqueVisitors;
    }

    public void setDailyUniqueVisitors(long dailyUniqueVisitors) {
        this.dailyUniqueVisitors = dailyUniqueVisitors;
    }

    public long getTotalUniqueVisitors() {
        return totalUniqueVisitors;
    }

    public void setTotalUniqueVisitors(long totalUniqueVisitors) {
        this.totalUniqueVisitors = totalUniqueVisitors;
    }

    public long getTotalVisits() {
        return totalVisits;
    }

    public void setTotalVisits(long totalVisits) {
        this.totalVisits = totalVisits;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public long getTotalClears() {
        return totalClears;
    }

    public void setTotalClears(long totalClears) {
        this.totalClears = totalClears;
    }

    public double getUptimeRatio() {
        return uptimeRatio;
    }

    public void setUptimeRatio(double uptimeRatio) {
        this.uptimeRatio = uptimeRatio;
    }

    public double getMtbf() {
        return mtbf;
    }

    public void setMtbf(double mtbf) {
        this.mtbf = mtbf;
    }

    public double getMttr() {
        return mttr;
    }

    public void setMttr(double mttr) {
        this.mttr = mttr;
    }
}
