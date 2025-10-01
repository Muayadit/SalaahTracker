package com.muayad.salaahtracker;

import java.io.Console;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initializeDatabase();

        System.out.println("===========================");
        System.out.println("Salaah Tracker Initialized.");
        System.out.println("===========================");

        Scanner input = new Scanner(System.in);
        User currentUser = null;

        // --- AUTHENTICATION LOOP ---
        while (true) {
            System.out.println();
            System.out.println("******************************************");
            System.out.println("Welcome to the Salaah Tracker application!");
            System.out.println("******************************************");
            System.out.println();
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.println();

            System.out.print("Enter the number of the command: ");
            int choice = input.nextInt();
            input.nextLine(); // Consume the newline character

            if (choice == 1) { // LOGIN
                System.out.print("Enter your Username: ");
                String username = input.nextLine();
                System.out.print("Enter your Password: ");
                String password = input.nextLine();

                User loggedInUser = dbManager.loginUser(username, password);

                if (loggedInUser != null) {
                    currentUser = loggedInUser;
                    System.out.println("\n--- Login successful! ---");
                    break; // Exit the authentication loop and start the main app
                } else {
                    System.out.println("------------------------------");
                    System.out.println("Wrong username or password :(");
                    System.out.println("------------------------------");
                }
            } else if (choice == 2) { // REGISTER
                System.out.print("Enter a new Username: ");
                String username = input.nextLine();

                if (dbManager.searchUser(username) == null) {
                    System.out.print("Enter a new Password: ");
                    String password = input.nextLine();
                    
                    dbManager.registerUser(username, password);
                    currentUser = dbManager.loginUser(username, password); // Log in automatically

                    System.out.println("----------------------------");
                    System.out.println("You Registered successfully!");
                    System.out.println("----------------------------");
                    break; // Exit the authentication loop and start the main app
                } else {
                    System.out.println("------------------------------------------------------");
                    System.out.println("This username is already used, please try another one.");
                    System.out.println("------------------------------------------------------");
                }
            } else if (choice == 3) { // EXIT
                System.out.println();
                System.out.println("----Exiting the Salaah Tracker app. Goodbye!----");
                System.exit(0); // Terminate the application
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }

        // --- MAIN APPLICATION LOOP ---
        while (true) {
            int countOfCompletedPrayers = 0;
            List<PrayerLog> todayPrayers = dbManager.getPrayersForToday(currentUser.getId(), LocalDate.now());

            System.out.println("\n--- Your Prayers for Today ---");
            for (int i = 0; i < todayPrayers.size(); i++) {
                if (todayPrayers.get(i).isCompleted()) {
                    System.out.print("[X] ");
                    countOfCompletedPrayers++;
                } else {
                    System.out.print("[ ] ");
                }
                System.out.println(todayPrayers.get(i).getPrayerName());
            }
            System.out.println("----------------------------");


            if (countOfCompletedPrayers == 5) {
                System.out.println("|| Masha'Allah! You have completed all your prayers for today! :) ||");
            }

            System.out.println("\n--- Actions Menu ---");
            System.out.println("1. Mark Fajr as complete");
            System.out.println("2. Mark Dhuhr as complete");
            System.out.println("3. Mark Asr as complete");
            System.out.println("4. Mark Maghrib as complete");
            System.out.println("5. Mark Isha as complete");
            System.out.println("6. Logout");
            System.out.println();

            System.out.print("Enter your choice: ");
            int choice = input.nextInt();
            input.nextLine(); // Consume the newline character

            if (choice >= 1 && choice <= 5) {
                // The choice number (1-5) corresponds to the index (0-4)
                int prayerIndex = choice - 1;
                PrayerLog prayerToUpdate = todayPrayers.get(prayerIndex);
                dbManager.markPrayerAsCompleted(prayerToUpdate.getId(), currentUser.getId());
                System.out.println("'" + prayerToUpdate.getPrayerName() + "' marked as complete!");
            } else if (choice == 6) {
                System.out.println("\nLogging out. Goodbye, " + currentUser.getUsername() + "!");
                break; // Exit the main application loop
            } else {
                System.out.println("Invalid input, please try again.");
            }
        }

        input.close(); // Close the scanner when the application is finished
    }
}