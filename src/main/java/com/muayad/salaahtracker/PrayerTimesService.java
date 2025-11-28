package com.muayad.salaahtracker;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class PrayerTimesService {

    private final HttpClient httpClient;

    public PrayerTimesService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    // Fetches today's prayer times for a specific City
    public Map<String, LocalTime> getPrayerTimes(String city, String country) {
        try {
            // Build the URL (Method 4 = Umm Al-Qura, Makkah)
            String url = String.format("http://api.aladhan.com/v1/timingsByCity?city=%s&country=%s&method=4", city, country);
            
            // Allow spaces in city names (e.g. "New York")
            url = url.replace(" ", "%20");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse the JSON response
            JSONObject json = new JSONObject(response.body());
            JSONObject timings = json.getJSONObject("data").getJSONObject("timings");

            // Store times in a Map (Prayer Name -> LocalTime)
            Map<String, LocalTime> prayerMap = new TreeMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            prayerMap.put("Fajr", LocalTime.parse(timings.getString("Fajr"), formatter));
            prayerMap.put("Dhuhr", LocalTime.parse(timings.getString("Dhuhr"), formatter));
            prayerMap.put("Asr", LocalTime.parse(timings.getString("Asr"), formatter));
            prayerMap.put("Maghrib", LocalTime.parse(timings.getString("Maghrib"), formatter));
            prayerMap.put("Isha", LocalTime.parse(timings.getString("Isha"), formatter));

            return prayerMap;

        } catch (Exception e) {
            System.err.println("Error fetching prayer times: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if we are roughly 'minutesBefore' minutes away from a prayer.
     * Returns the name of the UPCOMING prayer if true.
     */
    public String getUpcomingPrayerName(Map<String, LocalTime> timings, int minutesBefore) {
        LocalTime now = LocalTime.now(); 
        // Note: This uses the Server's time. 
        // For Render, this is usually UTC. We might need to adjust for Saudi time (UTC+3) logic later 
        // or ensure the server timezone is set. For now, we assume simple comparison.

        for (Map.Entry<String, LocalTime> entry : timings.entrySet()) {
            String prayerName = entry.getKey();
            LocalTime prayerTime = entry.getValue();

            // Check the gap
            // If Now is 17:00 and Maghrib is 17:20, the gap is 20 mins.
            // We give a small buffer window (e.g., between 18 and 22 mins) so the cron job doesn't miss it.
            long diff = java.time.Duration.between(now, prayerTime).toMinutes();

            if (diff >= (minutesBefore - 2) && diff <= (minutesBefore + 2)) {
                return prayerName;
            }
        }
        return null;
    }
}