package com.devnet.myeportal;

public class LeaveRequest {
    public String userId;
    public String username;
    public String destination;
    public String duration;
    public String leaveType;
    public String startDate;
    public String endDate;
    public String reason;
    public String status;
    public long timestamp;

    public LeaveRequest(String userId, String username, String destination, String duration, String leaveType, String startDate, String endDate, String reason, String status, long timestamp) {
        this.userId = userId;
        this.username = username;
        this.destination = destination;
        this.duration = duration;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.timestamp = timestamp;
    }
}

