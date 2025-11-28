package com.muayad.salaahtracker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TelegramBot {

    // REPLACE THIS WITH YOUR ACTUAL TOKEN FROM BOTFATHER
    private static final String BOT_TOKEN = "8264445809:AAF4AG9uCLEAsgUR1Gi6TEUVJnVmCKa2X_A"; 
    
    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot" + BOT_TOKEN;
    private final HttpClient httpClient;

    public TelegramBot() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // This method sends a message to a specific user
    public void sendMessage(String chatId, String text) {
        if (chatId == null || chatId.isEmpty()) {
            System.out.println("Cannot send Telegram message: Chat ID is empty.");
            return;
        }

        try {
            // We manually build the JSON payload to avoid library dependency issues
            // Format: {"chat_id": "12345", "text": "Hello"}
            // We escape quotes in the text to prevent errors
            String safeText = text.replace("\"", "\\\"").replace("\n", "\\n");
            String jsonPayload = String.format("{\"chat_id\":\"%s\",\"text\":\"%s\"}", chatId, safeText);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TELEGRAM_API_URL + "/sendMessage"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            // Send async so we don't block the app while waiting for Telegram
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        // System.out.println("Telegram Response: " + response); // Uncomment to debug
                    });

        } catch (Exception e) {
            System.err.println("Failed to send Telegram message: " + e.getMessage());
        }
    }
}