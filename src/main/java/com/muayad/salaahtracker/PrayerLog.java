package com.muayad.salaahtracker;

import java.time.LocalDate;

public class PrayerLog {
    private int id;
    private int userId;
    private String prayerName;
    private LocalDate prayerDate;
    private boolean isCompleted;


    public PrayerLog(){

    }


    //Getters

    public int getId(){
        return id;
    }
    
    public int getUserId(){
        return userId;
    }

    public String getPrayerName(){
        return prayerName;
    }

    public LocalDate getPrayerDate(){
        return prayerDate;
    }

    public boolean isCompleted(){
        return isCompleted;
    }

    //Setters

    public void setId(int id){
        this.id = id;
    }

    public void setUserId(int userId){
        this.userId = userId;
    }
    
    public void setPrayerName(String prayerName){
        this.prayerName = prayerName;
    }
    
    public void setPrayerDate(LocalDate prayerDate){
        this.prayerDate = prayerDate;
    }
    
    public void setCompleted(boolean isCompleted){
        this.isCompleted = isCompleted;
    }

}
