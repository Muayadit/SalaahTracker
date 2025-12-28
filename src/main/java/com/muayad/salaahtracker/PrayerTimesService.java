package com.muayad.salaahtracker;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class PrayerTimesService {

    private final HttpClient httpClient;
    private String detectedTimezone = "UTC"; 
    
    // --- CACHING VARIABLES ---
    private Map<String, LocalTime> cachedTimings;
    private LocalDate lastFetchDate;
    private String lastCity;
    private String lastCountry;

    public PrayerTimesService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public Map<String, LocalTime> getPrayerTimes(String city, String country) {
        LocalDate today = LocalDate.now();

        // 1. CHECK CACHE
        if (cachedTimings != null && today.equals(lastFetchDate) && 
            city.equalsIgnoreCase(lastCity) && country.equalsIgnoreCase(lastCountry)) {
            return cachedTimings;
        }

        // 2. FETCH FROM API
        try {
            String cleanCity = city.trim().replace(" ", "%20");
            String cleanCountry = country.trim().replace(" ", "%20");

            String url = String.format("https://api.aladhan.com/v1/timingsByCity?city=%s&country=%s&method=4", cleanCity, cleanCountry);
            System.out.println("Fetching URL: " + url); 

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                System.err.println("API Error: " + response.statusCode());
                return null;
            }

            JSONObject json = new JSONObject(response.body());
            JSONObject data = json.getJSONObject("data");
            JSONObject timings = data.getJSONObject("timings");
            JSONObject meta = data.getJSONObject("meta");
            
            this.detectedTimezone = meta.getString("timezone");
            System.out.println("Detected Timezone from API: " + this.detectedTimezone);

            Map<String, LocalTime> prayerMap = new TreeMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            // Added "Sunrise" here so we can track it!
            prayerMap.put("Fajr", parseTime(timings.getString("Fajr"), formatter));
            prayerMap.put("Sunrise", parseTime(timings.getString("Sunrise"), formatter)); 
            prayerMap.put("Dhuhr", parseTime(timings.getString("Dhuhr"), formatter));
            prayerMap.put("Asr", parseTime(timings.getString("Asr"), formatter));
            prayerMap.put("Maghrib", parseTime(timings.getString("Maghrib"), formatter));
            prayerMap.put("Isha", parseTime(timings.getString("Isha"), formatter));

            // 3. SAVE TO CACHE
            this.cachedTimings = prayerMap;
            this.lastFetchDate = today;
            this.lastCity = city;
            this.lastCountry = country;

            return prayerMap;

        } catch (Exception e) {
            System.err.println("Error fetching prayer times: " + e.getMessage());
            e.printStackTrace(); 
            return null;
        }
    }

    private LocalTime parseTime(String timeStr, DateTimeFormatter formatter) {
        String cleanTime = timeStr.split(" ")[0]; 
        return LocalTime.parse(cleanTime, formatter);
    }

    public String getUpcomingPrayerName(Map<String, LocalTime> timings, int exactMinute) {
        ZoneId zoneId = ZoneId.of(this.detectedTimezone);
        LocalTime now = ZonedDateTime.now(zoneId).toLocalTime(); 

        for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
            String prayerName = entry.getKey();
            LocalTime prayerTime = entry.getValue();

            long diff = java.time.Duration.between(now, prayerTime).toMinutes();

            if (diff == exactMinute) {
                return prayerName;
            }
        }
        return null;
    }
}