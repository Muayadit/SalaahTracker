package com.muayad.salaahtracker;

public class App 
{
    public static void main( String[] args ){
    DatabaseManager dbManager = new DatabaseManager();
    dbManager.initializeDatabase(); 

    System.out.println("===========================");
    System.out.println("Salaah Tracker Initialized.");
    System.out.println("===========================");
    }
}
