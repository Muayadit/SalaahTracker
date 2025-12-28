package com.muayad.salaahtracker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();

        TelegramBot bot = new TelegramBot();
        PrayerTimesService prayerService = new PrayerTimesService();

        var app = Javalin.create(config -> {
            config.staticFiles.add("public");
        }).start(7070);

        // --- AUTH ---
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

        app.post("/api/logout", ctx -> {
            ctx.req().getSession().invalidate();
            ctx.result("{\"status\":\"success\", \"message\":\"Logged out successfully\"}");
            ctx.contentType("application/json");
        });

        // --- DATA ---
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

        app.put("/api/prayers/{id}", ctx -> {
            User currentUser = ctx.sessionAttribute("currentUser");
            if (currentUser == null) {
                ctx.status(403);
                ctx.result("{\"status\":\"failure\", \"message\":\"You must be logged in\"}");
                ctx.contentType("application/json");
                return;
            }
            try {
                int prayerLogId = Integer.parseInt(ctx.pathParam("id"));
                boolean isCompleted = Boolean.parseBoolean(ctx.queryParam("completed"));
                
                dbManager.updatePrayerStatus(prayerLogId, currentUser.getId(), isCompleted);
                ctx.result("{\"status\":\"success\", \"message\":\"Prayer status updated\"}");
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

        // --- TELEGRAM ---
        app.post("/api/telegram/link", ctx -> {
            User currentUser = ctx.sessionAttribute("currentUser");
            if (currentUser == null) {
                ctx.status(403);
                ctx.result("{\"status\":\"failure\", \"message\":\"Not logged in\"}");
                ctx.contentType("application/json");
                return;
            }
            String chatId = ctx.formParam("chatId");
            dbManager.linkTelegramUser(currentUser.getId(), chatId);
            bot.sendMessage(chatId, "✅ Connected! You will now receive prayer reminders here.");
            ctx.result("{\"status\":\"success\", \"message\":\"Telegram connected successfully!\"}");
            ctx.contentType("application/json");
        });

        app.post("/api/telegram/test", ctx -> {
            User currentUser = ctx.sessionAttribute("currentUser");
            if (currentUser != null) {
                User freshUser = dbManager.searchUser(currentUser.getUsername());
                if (freshUser.getTelegramChatId() != null) {
                    bot.sendMessage(freshUser.getTelegramChatId(), "🔔 Test: Salaah Tracker notifications are working!");
                    ctx.result("{\"status\":\"success\", \"message\":\"Message sent to Telegram!\"}");
                } else {
                    ctx.result("{\"status\":\"failure\", \"message\":\"No Telegram ID linked yet.\"}");
                }
                ctx.contentType("application/json");
            }
        });

        // --- EXACT MINUTE REMINDERS (UPDATED WITH SUNRISE LOGIC) ---
        app.get("/api/check-reminders", ctx -> {
            String city = ctx.queryParam("city");
            String country = ctx.queryParam("country");

            if (city == null || country == null) {
                ctx.result("Please provide city and country");
                return;
            }

            Map<String, LocalTime> timings = prayerService.getPrayerTimes(city, country);
            
            if (timings == null) {
                ctx.result("Could not fetch prayer times.");
                return;
            }

            StringBuilder log = new StringBuilder();
            int[] milestones = {20, 10, 5};

            for (int minutes : milestones) {
                // Check if ANY event (Prayer OR Sunrise) is exactly 'minutes' away
                String upcomingEvent = prayerService.getUpcomingPrayerName(timings, minutes);

                if (upcomingEvent != null) {
                    String prayerToCheck = "";
                    
                    // --- ISLAMIC LOGIC UPDATE ---
                    switch (upcomingEvent) {
                        case "Sunrise": prayerToCheck = "Fajr"; break; // Warning for Fajr is before Sunrise
                        case "Dhuhr": prayerToCheck = null; break; // No prayer ends exactly at Dhuhr
                        case "Asr": prayerToCheck = "Dhuhr"; break;
                        case "Maghrib": prayerToCheck = "Asr"; break;
                        case "Isha": prayerToCheck = "Maghrib"; break;
                        default: prayerToCheck = null; 
                    }

                    if (prayerToCheck != null) {
                        List<String> chatIds = dbManager.getChatIdsForMissingPrayer(prayerToCheck, LocalDate.now());
                        
                        for (String chatId : chatIds) {
                            String msg = "";
                            
                            if (minutes == 20) {
                                msg = "ℹ️ REMINDER: " + upcomingEvent + " is in exactly 20 mins.\n" +
                                      "Have you prayed " + prayerToCheck + " yet?";
                            }
                            else if (minutes == 10) {
                                msg = "⚠️ WARNING: Time for " + prayerToCheck + " is ending!\n" +
                                      upcomingEvent + " begins in 10 minutes.";
                            }
                            else if (minutes == 5) {
                                msg = "🚨 URGENT: " + prayerToCheck + " will be missed in less than 5 minutes!\n" +
                                      "Pray before " + upcomingEvent + "!";
                            }
                            
                            bot.sendMessage(chatId, msg);
                        }
                        log.append("Sent " + minutes + "m warning for " + prayerToCheck + " (due to " + upcomingEvent + "). ");
                    }
                }
            }

            if (log.length() == 0) {
                ctx.result("Checked. No exact reminders needed right now.");
            } else {
                ctx.result(log.toString());
            }
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