package com.expensetracker;

import com.expensetracker.ui.ConsoleApp;
import com.expensetracker.web.WebServer;

import java.util.Scanner;

public class Main {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        AppContext app = AppContext.create();

        if (args.length > 0 && "console".equalsIgnoreCase(args[0])) {
            app.categoryService().initializeDefaults();
            ConsoleApp console = new ConsoleApp(
                    new Scanner(System.in),
                    app.expenseService(),
                    app.categoryService(),
                    app.budgetService(),
                    app.reportService());
            console.run();
            return;
        }

        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                // use default port
            }
        }

        WebServer.run(app, port);
        Thread.currentThread().join();
    }
}
