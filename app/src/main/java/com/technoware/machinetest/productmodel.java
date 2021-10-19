package com.technoware.machinetest;

public class productmodel {

    String title ;
    String price;
    String imageUrl ;
    String description ;


    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
