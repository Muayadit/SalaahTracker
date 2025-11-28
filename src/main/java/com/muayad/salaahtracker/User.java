package com.muayad.salaahtracker;

public class User {
    private int id;
    private String username;
    private String password;
    private String telegramChatId; 

    public User() {}

    public User(int id, String username, String password, String telegramChatId){
        this.id = id;
        this.username = username;
        this.password = password;
        this.telegramChatId = telegramChatId;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getTelegramChatId() { return telegramChatId; } 

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setTelegramChatId(String telegramChatId) { this.telegramChatId = telegramChatId; } 
}