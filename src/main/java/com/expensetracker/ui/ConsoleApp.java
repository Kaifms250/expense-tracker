package com.expensetracker.ui;

import com.expensetracker.model.Category;
import com.expensetracker.model.CategorySummary;
import com.expensetracker.model.Expense;
import com.expensetracker.model.MonthlyReport;
import com.expensetracker.service.BudgetService;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.service.ReportService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final Scanner scanner;
    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final BudgetService budgetService;
    private final ReportService reportService;

    public ConsoleApp(Scanner scanner,
                      ExpenseService expenseService,
                      CategoryService categoryService,
                      BudgetService budgetService,
                      ReportService reportService) {
        this.scanner = scanner;
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.budgetService = budgetService;
        this.reportService = reportService;
    }

    public void run() {
        categoryService.initializeDefaults();
        printWelcome();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> handleExpensesMenu();
                case 2 -> handleCategoriesMenu();
                case 3 -> handleBudgetsMenu();
                case 4 -> handleMonthlyReport();
                case 0 -> {
                    System.out.println("\nGoodbye! Keep tracking your spending.\n");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void printWelcome() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("       SMART EXPENSE TRACKER");
        System.out.println("   Know where your money goes");
        System.out.println("========================================");
        System.out.println();
    }

    private void printMainMenu() {
        System.out.println("--- Main Menu ---");
        System.out.println("1. Expenses");
        System.out.println("2. Categories");
        System.out.println("3. Budgets");
        System.out.println("4. Monthly Report");
        System.out.println("0. Exit");
    }

    private void handleExpensesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Expenses ---");
            System.out.println("1. Add expense");
            System.out.println("2. View all expenses");
            System.out.println("3. View this month's expenses");
            System.out.println("4. Delete expense");
            System.out.println("0. Back");

            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> addExpense();
                case 2 -> listExpenses(expenseService.getAllExpenses(), "All Expenses");
                case 3 -> listExpenses(expenseService.getExpensesForMonth(YearMonth.now()), "This Month");
                case 4 -> deleteExpense();
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void addExpense() {
        try {
            double amount = readDouble("Amount: $");
            Category category = selectCategory("Select category");
            if (category == null) {
                return;
            }
            LocalDate date = readDate("Date (yyyy-MM-dd, Enter for today): ", LocalDate.now());
            System.out.print("Description (optional): ");
            String description = scanner.nextLine().trim();

            Expense expense = expenseService.addExpense(amount, category.getId(), date, description);
            System.out.printf("Expense added: $%.2f in %s on %s%n",
                    expense.getAmount(), category.getName(), expense.getDate().format(DATE_FORMAT));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void listExpenses(List<Expense> expenses, String title) {
        System.out.println("\n--- " + title + " ---");
        if (expenses.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }

        double total = 0;
        for (Expense expense : expenses) {
            String categoryName = categoryService.getCategoryById(expense.getCategoryId())
                    .map(Category::getName)
                    .orElse("Unknown");
            System.out.printf("  [%s] $%.2f | %s | %s | %s%n",
                    expense.getId().substring(0, 8),
                    expense.getAmount(),
                    expense.getDate().format(DATE_FORMAT),
                    categoryName,
                    expense.getDescription().isEmpty() ? "-" : expense.getDescription());
            total += expense.getAmount();
        }
        System.out.printf("Total: $%.2f (%d items)%n", total, expenses.size());
    }

    private void deleteExpense() {
        System.out.print("Enter expense ID (first 8 chars shown in list): ");
        String partialId = scanner.nextLine().trim();
        Optional<Expense> match = expenseService.getAllExpenses().stream()
                .filter(e -> e.getId().startsWith(partialId))
                .findFirst();

        if (match.isEmpty()) {
            System.out.println("Expense not found.");
            return;
        }

        Expense expense = match.get();
        if (expenseService.deleteExpense(expense.getId())) {
            System.out.printf("Deleted expense: $%.2f on %s%n",
                    expense.getAmount(), expense.getDate().format(DATE_FORMAT));
        } else {
            System.out.println("Failed to delete expense.");
        }
    }

    private void handleCategoriesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Categories ---");
            System.out.println("1. View categories");
            System.out.println("2. Add category");
            System.out.println("3. Delete category");
            System.out.println("0. Back");

            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> listCategories();
                case 2 -> addCategory();
                case 3 -> deleteCategory();
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void listCategories() {
        List<Category> categories = categoryService.getAllCategories();
        System.out.println("\n--- Categories ---");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, categories.get(i).getName());
        }
    }

    private void addCategory() {
        System.out.print("Category name: ");
        String name = scanner.nextLine().trim();
        try {
            Category category = categoryService.addCategory(name);
            System.out.println("Category added: " + category.getName());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteCategory() {
        Category category = selectCategory("Select category to delete");
        if (category == null) {
            return;
        }
        if (categoryService.deleteCategory(category.getId())) {
            budgetService.removeBudget(category.getId());
            System.out.println("Category deleted: " + category.getName());
        } else {
            System.out.println("Failed to delete category.");
        }
    }

    private void handleBudgetsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Budgets ---");
            System.out.println("1. View budget status (this month)");
            System.out.println("2. Set / update budget");
            System.out.println("3. Remove budget");
            System.out.println("0. Back");

            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> showBudgetStatus(YearMonth.now());
                case 2 -> setBudget();
                case 3 -> removeBudget();
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void showBudgetStatus(YearMonth month) {
        List<BudgetService.BudgetStatus> statuses = budgetService.getBudgetStatusForMonth(month);
        System.out.printf("\n--- Budget Status: %s ---%n", month.format(MONTH_FORMAT));
        if (statuses.isEmpty()) {
            System.out.println("No budgets set. Use 'Set / update budget' to create one.");
            return;
        }

        for (BudgetService.BudgetStatus status : statuses) {
            String indicator = status.isOverBudget() ? "OVER BUDGET" : "OK";
            System.out.printf("  %-15s $%.2f / $%.2f (%.0f%%) [%s]%n",
                    status.categoryName(),
                    status.spent(),
                    status.limit(),
                    status.percentUsed(),
                    indicator);
        }
    }

    private void setBudget() {
        Category category = selectCategory("Select category for budget");
        if (category == null) {
            return;
        }
        try {
            double limit = readDouble("Monthly limit: $");
            budgetService.setBudget(category.getId(), limit);
            System.out.printf("Budget set for %s: $%.2f/month%n", category.getName(), limit);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void removeBudget() {
        Category category = selectCategory("Select category to remove budget");
        if (category == null) {
            return;
        }
        if (budgetService.removeBudget(category.getId())) {
            System.out.println("Budget removed for: " + category.getName());
        } else {
            System.out.println("No budget found for that category.");
        }
    }

    private void handleMonthlyReport() {
        YearMonth month = readMonth("Report month (yyyy-MM, Enter for current): ", YearMonth.now());
        MonthlyReport report = reportService.generateMonthlyReport(month);

        System.out.println();
        System.out.println("========================================");
        System.out.printf("     MONTHLY REPORT: %s%n", month.format(MONTH_FORMAT));
        System.out.println("========================================");
        System.out.printf("Total spent:     $%.2f%n", report.getTotalSpent());

        if (report.getPreviousMonthTotal() > 0 || report.getTotalSpent() > 0) {
            double change = report.getChangeFromPreviousMonth();
            String direction = change >= 0 ? "more" : "less";
            System.out.printf("vs last month:   $%.2f %s%n", Math.abs(change), direction);
        }

        System.out.println("\n--- By Category ---");
        if (report.getCategorySummaries().isEmpty()) {
            System.out.println("No spending recorded this month.");
        } else {
            for (CategorySummary summary : report.getCategorySummaries()) {
                String budgetInfo = summary.getBudgetLimit() > 0
                        ? String.format(" (budget: $%.2f, remaining: $%.2f%s)",
                        summary.getBudgetLimit(),
                        summary.getRemaining(),
                        summary.isOverBudget() ? " - OVER!" : "")
                        : "";
                System.out.printf("  %-15s $%.2f%s%n",
                        summary.getCategoryName(), summary.getSpent(), budgetInfo);
            }
        }

        showBudgetStatus(month);
        System.out.println();
    }

    private Category selectCategory(String prompt) {
        List<Category> categories = categoryService.getAllCategories();
        if (categories.isEmpty()) {
            System.out.println("No categories available.");
            return null;
        }

        System.out.println("\n" + prompt + ":");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, categories.get(i).getName());
        }

        int choice = readInt("Enter number: ");
        if (choice < 1 || choice > categories.size()) {
            System.out.println("Invalid selection.");
            return null;
        }
        return categories.get(choice - 1);
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }

    private LocalDate readDate(String prompt, LocalDate defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(input, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Using default.");
            return defaultValue;
        }
    }

    private YearMonth readMonth(String prompt, YearMonth defaultValue) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        try {
            return YearMonth.parse(input, MONTH_FORMAT);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid month format. Using current month.");
            return defaultValue;
        }
    }
}
