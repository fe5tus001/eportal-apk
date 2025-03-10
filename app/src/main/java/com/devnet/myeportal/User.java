package com.devnet.myeportal;

public class User {
    private String firstName;
    private String surname;
    private String rank;
    private String svcNumber;
    private String trade;
    private String formation;
    private String profileImageUrl;

    // Constructor
    public User(String firstName, String surname, String rank, String svcNumber,
                String trade, String formation, String profileImageUrl) {
        this.firstName = firstName;
        this.surname = surname;
        this.rank = rank;
        this.svcNumber = svcNumber;
        this.trade = trade;
        this.formation = formation;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and Setters
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getSvcNumber() {
        return svcNumber;
    }

    public void setSvcNumber(String svcNumber) {
        this.svcNumber = svcNumber;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public String getFormation() {
        return formation;
    }

    public void setFormation(String formation) {
        this.formation = formation;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
