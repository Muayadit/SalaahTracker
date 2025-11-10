package com.muayad.salaahtracker;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;

// These are the only Javalin imports we need.
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class App {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();

        // Create the Javalin app (simple version)
        var app = Javalin.create(config -> {
            // Tell Javalin where our HTML/CSS files are
            config.staticFiles.add("public");
        }).start(7070);

        // --- /api/login endpoint (Manual JSON) ---
        app.post("/api/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");

            User loggedInUser = dbManager.loginUser(username, password);

            if (loggedInUser != null) {
                ctx.sessionAttribute("currentUser", loggedInUser);
                // Manually build JSON and set content type
                ctx.result("{\"status\":\"success\", \"username\":\"" + loggedInUser.getUsername() + "\"}");
                ctx.contentType("application/json"); 
            } else {
                ctx.status(401);
                // Manually build JSON and set content type
                ctx.result("{\"status\":\"failure\", \"message\":\"Wrong username or password\"}");
                ctx.contentType("application/json");
            }
        });

        // --- /api/register endpoint (Manual JSON) ---
        app.post("/api/register", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            User existingUser = dbManager.searchUser(username);

            if (existingUser != null) {
                ctx.status(409); // Conflict
                ctx.result("{\"status\":\"failure\", \"message\":\"Username is already taken\"}");
                ctx.contentType("application/json");
            } else {
                dbManager.registerUser(username, password);
                ctx.result("{\"status\":\"success\", \"message\":\"Registration successful! Please log in.\"}");
                ctx.contentType("application/json");
            }
        });

        // --- /api/prayers/today endpoint (Manual JSON) ---
        app.get("/api/prayers/today", ctx -> {
            User currentUser = ctx.sessionAttribute("currentUser");

            if (currentUser == null) {
                ctx.status(403); // Forbidden
                ctx.result("{\"status\":\"failure\", \"message\":\"You must be logged in\"}");
                ctx.contentType("application/json");
                return;
            }

            List<PrayerLog> todayPrayers = dbManager.getPrayersForToday(currentUser.getId(), LocalDate.now());

            // Manually build the JSON Array
            StringBuilder jsonArray = new StringBuilder();
            jsonArray.append("[");
            
            for (int i = 0; i < todayPrayers.size(); i++) {
                PrayerLog p = todayPrayers.get(i);
                
                jsonArray.append("{");
                jsonArray.append("\"id\":").append(p.getId()).append(",");
                jsonArray.append("\"prayerName\":\"").append(p.getPrayerName()).append("\",");
                jsonArray.append("\"prayerDate\":\"").append(p.getPrayerDate().toString()).append("\",");
                jsonArray.append("\"completed\":").append(p.isCompleted());
                jsonArray.append("}"); 
                
                if (i < todayPrayers.size() - 1) {
                    jsonArray.append(",");
                }
            }
            
            jsonArray.append("]");
            
            ctx.result(jsonArray.toString());
            ctx.contentType("application/json");
        });

        app.put("/api/prayers/complete/{id}", ctx -> {
            User currentUser = ctx.sessionAttribute("currentUser");

            if (currentUser == null) {
                ctx.status(403); // Forbidden
                ctx.result("{\"status\":\"failure\", \"message\":\"You must be logged in\"}");
                ctx.contentType("application/json");
                return;
            }

            try {
                // 1. Get the ID from the URL (it's a String)
                String idString = ctx.pathParam("id");
                
                // 2. Convert it to an integer
                int prayerLogId = Integer.parseInt(idString);
                
                // 3. Get the user's ID
                int userId = currentUser.getId();

                // 4. USE YOUR EXISTING DATABASEMANAGER METHOD!
                // This is the beautiful part. The logic was already written.
                dbManager.markPrayerAsCompleted(prayerLogId, userId);

                // 5. Send a simple success response
                ctx.result("{\"status\":\"success\", \"message\":\"Prayer marked as complete\"}");
                ctx.contentType("application/json");

            } catch (NumberFormatException e) {
                // This happens if the ID in the URL is not a number
                ctx.status(400); // Bad Request
                ctx.result("{\"status\":\"failure\", \"message\":\"Invalid Prayer ID\"}");
                ctx.contentType("application/json");
            }
        });

        app.post("/api/logout", ctx -> {
            
            // This invalidates the entire session, forgetting the user.
            ctx.req().getSession().invalidate();
            
            // Send a success response
            ctx.result("{\"status\":\"success\", \"message\":\"Logged out successfully\"}");
            ctx.contentType("application/json");
        });


        System.out.println("========================================");
        System.out.println("Salaah Tracker Web Server Started!");
        System.out.println("Go to http://localhost:7070 in a browser.");
        System.out.println("========================================");
    }
}