package com.expensetracker.web;

import com.expensetracker.AppContext;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {
    private final HttpServer server;

    public WebServer(AppContext app, int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        server.createContext("/api", new ApiHandler(app)).getFilters().add(new CorsFilter());
        server.createContext("/", new StaticFileHandler()).getFilters().add(new CorsFilter());
        server.setExecutor(Executors.newFixedThreadPool(4));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    public static void run(AppContext app, int port) throws IOException {
        app.categoryService().initializeDefaults();
        WebServer webServer = new WebServer(app, port);
        webServer.start();
        System.out.println();
        System.out.println("========================================");
        System.out.println("   Smart Expense Tracker — Web UI");
        System.out.println("========================================");
        System.out.println("Open in browser: http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop.");
        System.out.println();
    }
}
