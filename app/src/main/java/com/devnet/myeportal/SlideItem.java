package com.devnet.myeportal;

public class SlideItem {
    private String imageUrl;
    private String headline;
    private String story;

    public SlideItem(String imageUrl, String headline, String story) {
        this.imageUrl = imageUrl;
        this.headline = headline;
        this.story = story;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getStory() {
        return story;
    }
}
