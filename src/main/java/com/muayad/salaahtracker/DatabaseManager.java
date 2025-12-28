package com.muayad.salaahtracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseManager {
    
    private String getDatabaseUrl() {
        String url = System.getenv("DB_URL");
        if (url == null) {
            return "jdbc:sqlite:salaahtracker.db"; 
        }
        return url;
    }

    public Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC"); 
            conn = DriverManager.getConnection(getDatabaseUrl());
        } catch (ClassNotFoundException e) {
            System.out.println("Driver Error: SQLite library is missing!");
        } catch (SQLException e) {
            System.out.println("Connection Error: " + e.getMessage());
        }
        return conn;
    }

    public void initializeDatabase(){
        String idType = "SERIAL PRIMARY KEY"; 
        if (getDatabaseUrl().contains("sqlite")) {
            idType = "INTEGER PRIMARY KEY AUTOINCREMENT";
        }

        String createUserTable = "CREATE TABLE IF NOT EXISTS users ("
        + " id " + idType + ","
        + " username TEXT UNIQUE NOT NULL,"
        + " password TEXT NOT NULL,"
        + " telegram_chat_id TEXT" 
        + " );";
    
        // UPDATED: Added UNIQUE constraint to prevent duplicate prayers
        String createPrayerLogTable = "CREATE TABLE IF NOT EXISTS prayer_log ("
        + " id " + idType + ","
        + " user_id INTEGER NOT NULL,"
        + " prayer_name TEXT NOT NULL,"
        + " prayer_date TEXT NOT NULL,"
        + " is_completed INTEGER NOT NULL DEFAULT 0,"
        + " FOREIGN KEY (user_id) REFERENCES users (id),"
        + " UNIQUE(user_id, prayer_name, prayer_date)" 
        + " );";
    
        try (Connection conn = this.connect()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createUserTable);
                    stmt.execute(createPrayerLogTable);
                    System.out.println("Database tables checked/created successfully.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Init Error: " + e.getMessage());
        }
     }

     public User searchUser(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"), 
                    rs.getString("username"), 
                    rs.getString("password"),
                    rs.getString("telegram_chat_id")
                );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
     }
    
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
                        return new User(
                            rs.getInt("id"), 
                            rs.getString("username"), 
                            rs.getString("password"),
                            rs.getString("telegram_chat_id")
                        );
                    }
                }
             } catch(SQLException e){
                System.out.println(e.getMessage());
             }
             return null;
     }

     public void linkTelegramUser(int userId, String chatId) {
         String sql = "UPDATE users SET telegram_chat_id = ? WHERE id = ?";
         try (Connection conn = this.connect();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setString(1, chatId);
             pstmt.setInt(2, userId);
             pstmt.executeUpdate();
         } catch (SQLException e) {
             System.out.println("Error linking telegram: " + e.getMessage());
         }
     }

     public List<String> getChatIdsForMissingPrayer(String prayerName, LocalDate date) {
        List<String> chatIds = new ArrayList<>();
        String sql = "SELECT telegram_chat_id FROM users " +
                     "WHERE telegram_chat_id IS NOT NULL " +
                     "AND id NOT IN (" +
                     "  SELECT user_id FROM prayer_log " +
                     "  WHERE prayer_name = ? AND prayer_date = ? AND is_completed = 1" +
                     ")";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prayerName);
            pstmt.setString(2, date.toString());
            
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                chatIds.add(rs.getString("telegram_chat_id"));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching missing prayers: " + e.getMessage());
        }
        return chatIds;
     }

     public List<PrayerLog> getPrayersForToday(int userId, LocalDate date){
        List<PrayerLog> todayPrayers = new ArrayList<>();
        String dateString = date.toString();
        
        // 1. Try to fetch existing prayers first
        String fetchPrayers = "SELECT * FROM prayer_log WHERE user_id = ? AND prayer_date = ?";
        
        try (Connection conn = this.connect()) {
            // First pass: check if they exist
            boolean prayersExist = false;
            try (PreparedStatement fetchstmt = conn.prepareStatement(fetchPrayers)) {
                fetchstmt.setInt(1, userId);
                fetchstmt.setString(2, dateString);
                ResultSet rs = fetchstmt.executeQuery();
                while (rs.next()) {
                    prayersExist = true;
                    PrayerLog prayerlog = new PrayerLog();
                    prayerlog.setId(rs.getInt("id"));
                    prayerlog.setUserId(rs.getInt("user_id"));
                    prayerlog.setPrayerName(rs.getString("prayer_name"));
                    prayerlog.setPrayerDate(LocalDate.parse(rs.getString("prayer_date")));
                    prayerlog.setCompleted(rs.getInt("is_completed") == 1);
                    todayPrayers.add(prayerlog);
                }
            }

            // 2. If they don't exist, create them safely
            if (!prayersExist) {
                List<String> the5Prayers = Arrays.asList("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha");
                // INSERT OR IGNORE is SQLite specific, but standard INSERT works with our catch block
                String insertPrayers = "INSERT INTO prayer_log (user_id, prayer_name, prayer_date, is_completed) VALUES (?, ?, ?, 0)";
                
                try (PreparedStatement instmt = conn.prepareStatement(insertPrayers)){
                    for (String prayer : the5Prayers) {
                        try {
                            instmt.setInt(1, userId);
                            instmt.setString(2, prayer);
                            instmt.setString(3, dateString);
                            instmt.executeUpdate();
                        } catch (SQLException ex) {
                            // Ignore duplicate errors silently
                        }
                    }
                }
                // Recursively fetch them again now that they are created
                return getPrayersForToday(userId, date); 
            }

        } catch (SQLException e) {
            System.out.println("Error managing daily prayers: " + e.getMessage());
        }
        return todayPrayers;
     }

     // === UPDATED: Allows setting status to TRUE (1) or FALSE (0) ===
     public void updatePrayerStatus(int prayerLogId, int userId, boolean isCompleted){
        String sql = "UPDATE prayer_log SET is_completed = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = this.connect();
             PreparedStatement updstmt = conn.prepareStatement(sql)){
                updstmt.setInt(1, isCompleted ? 1 : 0);
                updstmt.setInt(2, prayerLogId);
                updstmt.setInt(3, userId);
                updstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
     }

     public List<PrayerLog> getPrayersForMonth(int userid, int year, int month){
        List<PrayerLog> monthlyPrayers = new ArrayList<>();
        String datePattern = String.format("%d-%02d-%%", year, month);
        String fetchMonthlyPrayers = "SELECT * FROM prayer_log WHERE user_id = ? AND prayer_date LIKE ? ORDER BY prayer_date";
        try (Connection conn = this.connect();
            PreparedStatement fetchMonthlystmt = conn.prepareStatement(fetchMonthlyPrayers)){
                fetchMonthlystmt.setInt(1, userid);
                fetchMonthlystmt.setString(2, datePattern);
                ResultSet rs = fetchMonthlystmt.executeQuery();
                while(rs.next()){
                    PrayerLog prayerLog = new PrayerLog();
                    prayerLog.setId(rs.getInt("id"));
                    prayerLog.setUserId(rs.getInt("user_id"));
                    prayerLog.setPrayerName(rs.getString("prayer_name"));
                    prayerLog.setPrayerDate(LocalDate.parse(rs.getString("prayer_date")));
                    prayerLog.setCompleted(rs.getInt("is_completed") == 1);
                    monthlyPrayers.add(prayerLog);
                }
        } catch (SQLException e) {
            System.out.println("Couldn't fetch prayers for the month: " + e.getMessage());
        }
        return monthlyPrayers;
      }

    public List<PrayerLog> getPrayersBetweenDates(int userId, LocalDate startDate, LocalDate endDate) {
        List<PrayerLog> prayers = new ArrayList<>();
        String sql = "SELECT * FROM prayer_log WHERE user_id = ? AND prayer_date BETWEEN ? AND ? ORDER BY prayer_date";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                PrayerLog prayerLog = new PrayerLog();
                prayerLog.setId(rs.getInt("id"));
                prayerLog.setUserId(rs.getInt("user_id"));
                prayerLog.setPrayerName(rs.getString("prayer_name"));
                prayerLog.setPrayerDate(LocalDate.parse(rs.getString("prayer_date")));
                prayerLog.setCompleted(rs.getInt("is_completed") == 1);
                prayers.add(prayerLog);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching prayers between dates: " + e.getMessage());
        }
        return prayers;
    }
}