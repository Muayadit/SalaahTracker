package com.muayad.salaahtracker;

import java.io.Console;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;

import io.javalin.Javalin;
import java.util.Map;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

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
            
            ctx.json(Map.of("status", "success", "username", loggedInUser.getUsername()));
        } else {
            // Failure!
            ctx.status(401); 
            ctx.json(Map.of("status", "failure", "message", "Wrong username or password"));
        }
    });


        System.out.println("========================================");
        System.out.println("Salaah Tracker Web Server Started!");
        System.out.println("Go to http://localhost:7070 in a browser.");
        System.out.println("========================================");


    }
}