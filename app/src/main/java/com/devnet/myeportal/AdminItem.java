package com.devnet.myeportal;
public class AdminItem {
    private String name;
    private String iconUrl;

    public AdminItem(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}

