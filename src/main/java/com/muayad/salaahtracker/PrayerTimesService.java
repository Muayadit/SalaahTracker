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
    // Default to UTC, but will be overwritten by API
    private String detectedTimezone = "UTC"; 

    public PrayerTimesService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public Map<String, LocalTime> getPrayerTimes(String city, String country) {
        try {
            String cleanCity = city.trim().replace(" ", "%20");
            String cleanCountry = country.trim().replace(" ", "%20");

            // Use HTTPS and Method 4 (Umm Al-Qura)
            String url = String.format("https://api.aladhan.com/v1/timingsByCity?city=%s&country=%s&method=4", cleanCity, cleanCountry);
            
            System.out.println("Fetching URL: " + url); 

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            if (response.statusCode() != 200) {
                System.err.println("API Error: " + response.statusCode());
                return null;
            }

            JSONObject json = new JSONObject(responseBody);
            JSONObject data = json.getJSONObject("data");
            JSONObject timings = data.getJSONObject("timings");
            
            // 1. CAPTURE THE TIMEZONE
            JSONObject meta = data.getJSONObject("meta");
            this.detectedTimezone = meta.getString("timezone");
            System.out.println("Detected Timezone from API: " + this.detectedTimezone);

            Map<String, LocalTime> prayerMap = new TreeMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            prayerMap.put("Fajr", parseTime(timings.getString("Fajr"), formatter));
            prayerMap.put("Dhuhr", parseTime(timings.getString("Dhuhr"), formatter));
            prayerMap.put("Asr", parseTime(timings.getString("Asr"), formatter));
            prayerMap.put("Maghrib", parseTime(timings.getString("Maghrib"), formatter));
            prayerMap.put("Isha", parseTime(timings.getString("Isha"), formatter));

            return prayerMap;

        } catch (Exception e) {
            System.err.println("Error fetching prayer times: " + e.getMessage());
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
        // 2. USE THE DETECTED TIMEZONE FOR "NOW"
        ZoneId zoneId = ZoneId.of(this.detectedTimezone);
        LocalTime now = ZonedDateTime.now(zoneId).toLocalTime(); 
        
        // Debug: Check Server Time vs Prayer Time
        // System.out.println("Server thinks Local Time is: " + now);

        for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
            String prayerName = entry.getKey();
            LocalTime prayerTime = entry.getValue();

            // Calculate difference in minutes
            long diff = java.time.Duration.between(now, prayerTime).toMinutes();

            if (diff > minMinutes && diff <= maxMinutes) {
                return prayerName;
            }
        }
        return null;
    }
}