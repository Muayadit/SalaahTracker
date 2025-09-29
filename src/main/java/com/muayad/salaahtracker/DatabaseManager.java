package com.muayad.salaahtracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

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


 //////////////////////////////////////////// USER MANAGEMENT METHODS /////////////////////////////////////////////////////////////


 public void registerUser(String username, String password){
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

    String insertUser = "INSERT INTO users(username, password) VALUES (?, ?)";
    try(Connection conn = this.connect();
         PreparedStatement pstmt = conn.prepareStatement(insertUser)){
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
         } catch(SQLException e){
            System.out.println(e.getMessage());
         }
 }

 public User loginUser(String username, String password){

    String checkUser = "SELECT * FROM users WHERE username = ?";
    try(Connection conn = this.connect();
         PreparedStatement pstmt = conn.prepareStatement(checkUser)){
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if(rs.next()){
                String storedHash = rs.getString("password");

                if(BCrypt.checkpw(password, storedHash)){
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(storedHash);
                return user;
                }
            }
         } catch(SQLException e){
            System.out.println(e.getMessage());
         }

         return null;
 }

 //////////////////////////////////////////// PRAYER MANAGEMENT METHODS /////////////////////////////////////////////////////////////

 public List<PrayerLog> getPrayersForToday(int userId, LocalDate date){
    List<PrayerLog> todayPrayers = new ArrayList<>();
    int checkNumOfPrayersAvailable = "SELECT COUNT(*) FROM prayer_log WHERE user_id = ? AND prayer_date = ?";
    try (Connection conn = this.connect();
         PreparedStatement pstmt = conn.prepareStatement(checkNumOfPrayersAvailable)){
            pstmt.setInt(1, userId);

            LocalDate todayDate = LocalDate.now();
            pstmt.setString(2, todayDate);
        
    } catch (Exception e) {
        // TODO: handle exception
    }
    if(checkNumOfPrayersAvailable == 0){
        list<String> the5Prayers = List.of("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha");
        String insertPrayers = "INSERT INTO prayer_log (user_id, prayer_name, prayer_date, is_completed) VALUES (?, ?, ?, 0)"
    }
 }




}