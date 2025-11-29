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
            String url = String.format("https://api.aladhan.com/v1/timingsByCity?city=%s&country=%s&method=4", city, country);
            url = url.replace(" ", "%20");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            JSONObject data = json.getJSONObject("data");
            JSONObject timings = data.getJSONObject("timings");
            
            // 1. CAPTURE THE TIMEZONE
            JSONObject meta = data.getJSONObject("meta");
            this.detectedTimezone = meta.getString("timezone");
            // System.out.println("Detected Timezone: " + this.detectedTimezone); // Debug

            Map<String, LocalTime> prayerMap = new TreeMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            // 2. SAFE PARSING (Remove " (AST)" suffixes if present)
            prayerMap.put("Fajr", parseTime(timings.getString("Fajr"), formatter));
            prayerMap.put("Dhuhr", parseTime(timings.getString("Dhuhr"), formatter));
            prayerMap.put("Asr", parseTime(timings.getString("Asr"), formatter));
            prayerMap.put("Maghrib", parseTime(timings.getString("Maghrib"), formatter));
            prayerMap.put("Isha", parseTime(timings.getString("Isha"), formatter));

            return prayerMap;

        } catch (Exception e) {
            System.err.println("Error fetching prayer times: " + e.getMessage());
            return null;
        }
    }

    private LocalTime parseTime(String timeStr, DateTimeFormatter formatter) {
        // Aladhan sometimes returns "05:23 (AST)". We only want "05:23".
        String cleanTime = timeStr.split(" ")[0]; 
        return LocalTime.parse(cleanTime, formatter);
    }

    public String getUpcomingPrayerName(Map<String, LocalTime> timings, int minMinutes, int maxMinutes) {
        // 3. USE THE DETECTED TIMEZONE
        // This ensures the server uses "Jeddah Time", not "Server Time"
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