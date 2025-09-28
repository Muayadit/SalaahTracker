package com.muayad.salaahtracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String databaseAddress = "jdbc:sqlite:salaahtracker.db";

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(databaseAddress);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }



public void initializeDatabase(){
    String createUserTable = "CREATE TABLE IF NOT EXISTS users ("
    + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
    + " username TEXT UNIQUE NOT NULL,"
    + " password TEXT NOT NULL "
    + " );";

    String createPrayerLogTable = "CREATE TABLE IF NOT EXISTS prayer_log ("
    + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
    + " user_id INTEGER NOT NULL,"
    + " prayer_name TEXT NOT NULL,"
            + " prayer_date TEXT NOT NULL,"
            + " is_completed INTEGER NOT NULL DEFAULT 0,"
            + " FOREIGN KEY (user_id) REFERENCES users (id)"
            + " );";

    try (Connection conn = this.connect();
            Statement stmt = conn.createStatement()) {

        
        stmt.execute(createUsersTable);
        stmt.execute(createPrayerLogTable);

    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
 }
}