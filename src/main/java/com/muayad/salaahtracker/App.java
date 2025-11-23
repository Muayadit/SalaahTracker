package com.muayad.salaahtracker;

import java.time.LocalDate;
import java.util.List;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();

        var app = Javalin.create(config -> {
            config.staticFiles.add("public");
        }).start(7070);

        app.post("/api/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            User loggedInUser = dbManager.loginUser(username, password);

            if (loggedInUser != null) {
                ctx.sessionAttribute("currentUser", loggedInUser);
                ctx.result("{\"status\":\"success\", \"username\":\"" + loggedInUser.getUsername() + "\"}");
                ctx.contentType("application/json"); 
            } else {
                ctx.status(401);
                ctx.result("{\"status\":\"failure\", \"message\":\"Wrong username or password\"}");
                ctx.contentType("application/json");
            }
        });

        app.post("/api/register", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            User existingUser = dbManager.searchUser(username);

            if (existingUser != null) {
                ctx.status(409);
                ctx.result("{\"status\":\"failure\", \"message\":\"Username is already taken\"}");
                ctx.contentType("application/json");
            } else {
                dbManager.registerUser(username, password);
                ctx.result("{\"status\":\"success\", \"message\":\"Registration successful! Please log in.\"}");
                ctx.contentType("application/json");
            }
        });

        app.get("/api/prayers/today", ctx -> {
            User currentUser = ctx.sessionAttribute("currentUser");
            if (currentUser == null) {
                ctx.status(403);
                ctx.result("{\"status\":\"failure\", \"message\":\"You must be logged in\"}");
                ctx.contentType("application/json");
                return;
            }
            List<PrayerLog> todayPrayers = dbManager.getPrayersForToday(currentUser.getId(), LocalDate.now());
            String jsonResult = serializePrayerList(todayPrayers);
            ctx.result(jsonResult);
            ctx.contentType("application/json");
        });

        app.put("/api/prayers/complete/{id}", ctx -> {
            User currentUser = ctx.sessionAttribute("currentUser");
            if (currentUser == null) {
                ctx.status(403);
                ctx.result("{\"status\":\"failure\", \"message\":\"You must be logged in\"}");
                ctx.contentType("application/json");
                return;
            }
            try {
                int prayerLogId = Integer.parseInt(ctx.pathParam("id"));
                int userId = currentUser.getId();
                dbManager.markPrayerAsCompleted(prayerLogId, userId);
                ctx.result("{\"status\":\"success\", \"message\":\"Prayer marked as complete\"}");
                ctx.contentType("application/json");
            } catch (NumberFormatException e) {
                ctx.status(400);
                ctx.result("{\"status\":\"failure\", \"message\":\"Invalid Prayer ID\"}");
                ctx.contentType("application/json");
            }
        });

        app.get("/api/summary/monthly", ctx -> {
            User currentUser = ctx.sessionAttribute("currentUser");
            if (currentUser == null) {
                ctx.status(403); 
                ctx.result("{\"status\":\"failure\", \"message\":\"You must be logged in\"}");
                ctx.contentType("application/json");
                return;
            }
            try {
                int year = Integer.parseInt(ctx.queryParam("year"));
                int month = Integer.parseInt(ctx.queryParam("month"));
                List<PrayerLog> monthlyPrayers = dbManager.getPrayersForMonth(currentUser.getId(), year, month);
                String jsonResult = serializePrayerList(monthlyPrayers);
                ctx.result(jsonResult);
                ctx.contentType("application/json");
            } catch (NumberFormatException e) {
                ctx.status(400); 
                ctx.result("{\"status\":\"failure\", \"message\":\"Invalid year or month\"}");
                ctx.contentType("application/json");
            }
        });

        app.get("/api/summary/weekly", ctx -> {
            User currentUser = ctx.sessionAttribute("currentUser");
            if (currentUser == null) {
                ctx.status(403);
                ctx.result("{\"status\":\"failure\", \"message\":\"You must be logged in\"}");
                ctx.contentType("application/json");
                return;
            }
            try {
                String startParam = ctx.queryParam("start");
                LocalDate startDate = LocalDate.parse(startParam);
                LocalDate endDate = startDate.plusDays(6);
                List<PrayerLog> weeklyPrayers = dbManager.getPrayersBetweenDates(currentUser.getId(), startDate, endDate);
                String jsonResult = serializePrayerList(weeklyPrayers);
                ctx.result(jsonResult);
                ctx.contentType("application/json");
            } catch (Exception e) {
                ctx.status(400);
                ctx.result("{\"status\":\"failure\", \"message\":\"Invalid date format\"}");
                ctx.contentType("application/json");
            }
        });

        app.post("/api/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.result("{\"status\":\"success\", \"message\":\"Logged out successfully\"}");
            ctx.contentType("application/json");
        });

        System.out.println("========================================");
        System.out.println("Salaah Tracker Web Server Started!");
        System.out.println("Go to http://localhost:7070 in a browser.");
        System.out.println("========================================");
    }

    private static String serializePrayerList(List<PrayerLog> prayers) {
        StringBuilder jsonArray = new StringBuilder();
        jsonArray.append("[");
        for (int i = 0; i < prayers.size(); i++) {
            PrayerLog p = prayers.get(i);
            jsonArray.append("{");
            jsonArray.append("\"id\":").append(p.getId()).append(",");
            jsonArray.append("\"prayerName\":\"").append(p.getPrayerName()).append("\",");
            jsonArray.append("\"prayerDate\":\"").append(p.getPrayerDate().toString()).append("\",");
            jsonArray.append("\"completed\":").append(p.isCompleted());
            jsonArray.append("}"); 
            if (i < prayers.size() - 1) {
                jsonArray.append(",");
            }
        }
        jsonArray.append("]");
        return jsonArray.toString();
    }
}