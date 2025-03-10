package com.devnet.myeportal;

public class AdvanceRequest {
    public String userId;
    public String amount;
    public String reason;
    public String startDate;
    public String duration;
    public String endDate;
    public String username;
    public String status;
    public long timestamp;

    public AdvanceRequest(String userId, String amount, String username, String duration, String startDate, String endDate, String reason, String status, long timestamp) {

        this.userId = userId;
        this.username = username;
        this.duration = duration;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.timestamp = timestamp;
    }


}
