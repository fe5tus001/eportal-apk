package com.devnet.myeportal;

public class Menu {
    private String name;
    private String iconUrl;


    public Menu(String name, String iconUrl) {
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

