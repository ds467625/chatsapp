package com.example.chatsapp.Model;

public class UserModel  {

    private String image,title;
    private String userId,phone,status;

    public UserModel(String userId,String image, String title,String phone,String status) {
        this.image = image;
        this.title = title;
        this.userId = userId;
        this.phone = phone;
        this.status = status;
    }
    public UserModel(){

    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
