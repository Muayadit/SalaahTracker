package com.muayad.salaahtracker;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class PrayerTimesService {

    private final HttpClient httpClient;
    // We will store the timezone from the API here (e.g., "Asia/Riyadh")
    private String detectedTimezone = "UTC"; 

    public PrayerTimesService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public Map<String, LocalTime> getPrayerTimes(String city, String country) {
        try {
            // Ensure spaces are encoded (e.g. "Saudi Arabia" -> "Saudi%20Arabia")
            String cleanCity = city.trim().replace(" ", "%20");
            String cleanCountry = country.trim().replace(" ", "%20");

            // CRITICAL FIX: Changed http to https
            String url = String.format("https://api.aladhan.com/v1/timingsByCity?city=%s&country=%s&method=4", cleanCity, cleanCountry);
            
            System.out.println("Fetching URL: " + url); // Debug URL

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    // Add User-Agent just in case they block Java default agents
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            // Debug logs to see what's happening
            System.out.println("API Status: " + response.statusCode());
            // System.out.println("API Raw Response: " + responseBody); 

            if (response.statusCode() != 200) {
                System.err.println("API Returned Error Code: " + response.statusCode());
                return null;
            }

            JSONObject json = new JSONObject(responseBody);
            JSONObject data = json.getJSONObject("data");
            JSONObject timings = data.getJSONObject("timings");
            
            JSONObject meta = data.getJSONObject("meta");
            this.detectedTimezone = meta.getString("timezone");

            Map<String, LocalTime> prayerMap = new TreeMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            prayerMap.put("Fajr", parseTime(timings.getString("Fajr"), formatter));
            prayerMap.put("Dhuhr", parseTime(timings.getString("Dhuhr"), formatter));
            prayerMap.put("Asr", parseTime(timings.getString("Asr"), formatter));
            prayerMap.put("Maghrib", parseTime(timings.getString("Maghrib"), formatter));
            prayerMap.put("Isha", parseTime(timings.getString("Isha"), formatter));

            return prayerMap;

        } catch (Exception e) {
            System.err.println("CRASH in getPrayerTimes: " + e.getMessage());
            e.printStackTrace(); 
            return null;
        }
    }

    private LocalTime parseTime(String timeStr, DateTimeFormatter formatter) {
        // Aladhan sometimes returns "05:23 (AST)". We only want "05:23".
        String cleanTime = timeStr.split(" ")[0]; 
        return LocalTime.parse(cleanTime, formatter);
    }

    public String getUpcomingPrayerName(Map<String, LocalTime> timings, int minMinutes, int maxMinutes) {
        // Use the API's timezone to ensure we are calculating "local time" correctly
        ZoneId zoneId = ZoneId.of(this.detectedTimezone);
        LocalTime now = ZonedDateTime.now(zoneId).toLocalTime(); 

        for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
            String prayerName = entry.getKey();
            LocalTime prayerTime = entry.getValue();

            long diff = java.time.Duration.between(now, prayerTime).toMinutes();

            if (diff > minMinutes && diff <= maxMinutes) {
                return prayerName;
            }
        }
        return null;
    }
}