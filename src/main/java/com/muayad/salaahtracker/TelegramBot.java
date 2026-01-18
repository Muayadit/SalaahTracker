package com.muayad.salaahtracker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TelegramBot {

    
    private static final String BOT_TOKEN = System.getenv("TELEGRAM_BOT_TOKEN");
    
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot" + BOT_TOKEN;
    private final HttpClient httpClient;

    public TelegramBot() {
        // Safety check: specific error if token is missing
        if (BOT_TOKEN == null || BOT_TOKEN.isEmpty()) {
            System.err.println("❌ FATAL ERROR: TELEGRAM_BOT_TOKEN is missing from Environment Variables!");
        }

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void sendMessage(String chatId, String text) {
        if (chatId == null || chatId.isEmpty()) {
            System.out.println("Cannot send Telegram message: Chat ID is empty.");
            return;
        }

        try {
            
            String safeText = text.replace("\"", "\\\"").replace("\n", "\\n");
            String jsonPayload = String.format("{\"chat_id\":\"%s\",\"text\":\"%s\"}", chatId, safeText);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TELEGRAM_API_URL + "/sendMessage"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        // System.out.println("Telegram Response: " + response); 
                    });

        } catch (Exception e) {
            System.err.println("Failed to send Telegram message: " + e.getMessage());
        }
    }
}