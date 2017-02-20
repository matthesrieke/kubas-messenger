package org.n52.kubas.messenger;

public class Message {

    private String email;
    private String phone;
    private String userId;
    private boolean response;
    private String details;

    public Message(){}

    public Message(String email, String phone, String userId, boolean response, String details){
        this.email = email;
        this.phone = phone;
        this.userId = userId;
        this.response = response;
        this.details = details;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
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
    public boolean isResponse() {
        return response;
    }
    public void setResponse(boolean response) {
        this.response = response;
    }
    public String getDetails() {
        return details;
    }
    public void setDetails(String details) {
        this.details = details;
    }

}
