package com.expensetracker.ui;

import com.expensetracker.model.Category;
import com.expensetracker.model.CategorySummary;
import com.expensetracker.model.Expense;
import com.expensetracker.model.Goal;
import com.expensetracker.model.GoalSummary;
import com.expensetracker.model.MonthlyReport;
import com.expensetracker.service.BudgetService;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.ExpenseService;
import com.expensetracker.service.GoalService;
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
    private final GoalService goalService;

    public ConsoleApp(Scanner scanner,
                      ExpenseService expenseService,
                      CategoryService categoryService,
                      BudgetService budgetService,
                      ReportService reportService,
                      GoalService goalService) {
        this.scanner = scanner;
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.budgetService = budgetService;
        this.reportService = reportService;
        this.goalService = goalService;
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
                case 4 -> handleGoalsMenu();
                case 5 -> handleMonthlyReport();
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
        System.out.println("4. Goals");
        System.out.println("5. Monthly Report");
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

    // ── Goals Menu ────────────────────────────────────────────────────────────

    private void handleGoalsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Goals ---");
            System.out.println("1. View all goals");
            System.out.println("2. Create new goal");
            System.out.println("3. Add savings to goal");
            System.out.println("4. Update goal details");
            System.out.println("5. Delete goal");
            System.out.println("0. Back");

            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> viewAllGoals();
                case 2 -> createNewGoal();
                case 3 -> addSavingsToGoal();
                case 4 -> updateGoalDetails();
                case 5 -> deleteGoal();
                case 0 -> back = true;
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void viewAllGoals() {
        List<Goal> goals = goalService.getAllGoals();
        System.out.println("\n--- Your Savings Goals ---");
        if (goals.isEmpty()) {
            System.out.println("No goals found. Create your first goal!");
            return;
        }

        // Summary
        com.expensetracker.model.GoalSummary summary = goalService.getGoalSummary();
        System.out.printf("  Total Target: ₹%.2f  |  Total Saved: ₹%.2f  |  Overall: %.0f%%%n%n",
                summary.getTotalTargetAmount(),
                summary.getTotalSavedAmount(),
                summary.getOverallProgressPercent());

        for (int i = 0; i < goals.size(); i++) {
            Goal g = goals.get(i);
            String statusLabel = switch (g.getStatus()) {
                case ACHIEVED       -> "✅ Achieved";
                case BEHIND_SCHEDULE -> "⚠️  Behind";
                default             -> "✅ On Track";
            };
            System.out.printf("  [%d] %s %s%n", i + 1, g.getIcon(), g.getName());
            System.out.printf("      Progress : %.0f%%  (₹%.2f / ₹%.2f)%n",
                    g.getProgressPercent(), g.getCurrentSavings(), g.getTargetAmount());
            System.out.printf("      Monthly  : ₹%.2f/mo   Days left: %d   Status: %s%n",
                    g.getRequiredMonthlySavings(), g.getDaysRemaining(), statusLabel);
            System.out.printf("      Recommendation: %s%n%n",
                    goalService.getRecommendation(g.getId()));
        }
    }

    private void createNewGoal() {
        System.out.println("\n--- Create New Goal ---");
        try {
            System.out.print("Goal name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Goal name cannot be empty.");
                return;
            }

            double targetAmount = readPositiveDouble("Target amount (₹): ");
            System.out.print("Current savings (₹, Enter for 0): ");
            String savingsInput = scanner.nextLine().trim();
            double currentSavings = savingsInput.isEmpty() ? 0.0 : Double.parseDouble(savingsInput);

            System.out.println("Deadline type:");
            System.out.println("  1. Specific date (yyyy-MM-dd)");
            System.out.println("  2. Duration in months");
            int deadlineChoice = readInt("Choose: ");

            Goal goal;
            if (deadlineChoice == 1) {
                LocalDate targetDate = readFutureDate("Target date (yyyy-MM-dd): ");
                if (targetDate == null) return;
                goal = goalService.createGoal(name, targetAmount, currentSavings, targetDate, null);
            } else if (deadlineChoice == 2) {
                int months = readInt("Duration (months, 1-600): ");
                goal = goalService.createGoalWithDuration(name, targetAmount, currentSavings, months);
            } else {
                System.out.println("Invalid choice.");
                return;
            }

            System.out.printf("%nGoal created: %s %s%n", goal.getIcon(), goal.getName());
            System.out.printf("  Progress : %.0f%%  (₹%.2f / ₹%.2f)%n",
                    goal.getProgressPercent(), goal.getCurrentSavings(), goal.getTargetAmount());
            System.out.printf("  Required : ₹%.2f/month  |  Days left: %d%n",
                    goal.getRequiredMonthlySavings(), goal.getDaysRemaining());

        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addSavingsToGoal() {
        List<Goal> goals = goalService.getAllGoals();
        if (goals.isEmpty()) {
            System.out.println("No goals found. Create a goal first.");
            return;
        }

        System.out.println("\n--- Add Savings ---");
        Goal selected = selectGoal(goals, "Select goal to add savings to");
        if (selected == null) return;

        try {
            double amount = readPositiveDouble("Amount to add (₹): ");
            Goal updated = goalService.addSavings(selected.getId(), amount);
            System.out.printf("₹%.2f added to '%s'%n", amount, updated.getName());
            System.out.printf("New balance: ₹%.2f / ₹%.2f (%.0f%%) — %s%n",
                    updated.getCurrentSavings(),
                    updated.getTargetAmount(),
                    updated.getProgressPercent(),
                    updated.getStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateGoalDetails() {
        List<Goal> goals = goalService.getAllGoals();
        if (goals.isEmpty()) {
            System.out.println("No goals found.");
            return;
        }

        System.out.println("\n--- Update Goal ---");
        Goal selected = selectGoal(goals, "Select goal to update");
        if (selected == null) return;

        System.out.println("What would you like to update?");
        System.out.println("  1. Target amount");
        System.out.println("  2. Current savings");
        System.out.println("  3. Target date");
        System.out.println("  0. Cancel");

        int choice = readInt("Choose: ");
        try {
            switch (choice) {
                case 1 -> {
                    double newTarget = readPositiveDouble("New target amount (₹): ");
                    Goal updated = goalService.updateTargetAmount(selected.getId(), newTarget);
                    System.out.printf("Target updated to ₹%.2f. Monthly required: ₹%.2f%n",
                            updated.getTargetAmount(), updated.getRequiredMonthlySavings());
                }
                case 2 -> {
                    double newSavings = readPositiveDouble("New current savings (₹): ");
                    Goal updated = goalService.updateCurrentSavings(selected.getId(), newSavings);
                    System.out.printf("Savings updated to ₹%.2f (%.0f%%)%n",
                            updated.getCurrentSavings(), updated.getProgressPercent());
                }
                case 3 -> {
                    LocalDate newDate = readFutureDate("New target date (yyyy-MM-dd): ");
                    if (newDate == null) return;
                    Goal updated = goalService.updateTargetDate(selected.getId(), newDate);
                    System.out.printf("Target date updated to %s. Monthly required: ₹%.2f%n",
                            updated.getTargetDate(), updated.getRequiredMonthlySavings());
                }
                case 0 -> System.out.println("Cancelled.");
                default -> System.out.println("Invalid option.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteGoal() {
        List<Goal> goals = goalService.getAllGoals();
        if (goals.isEmpty()) {
            System.out.println("No goals found.");
            return;
        }

        System.out.println("\n--- Delete Goal ---");
        Goal selected = selectGoal(goals, "Select goal to delete");
        if (selected == null) return;

        System.out.printf("Delete '%s'? (y/n): ", selected.getName());
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Cancelled.");
            return;
        }

        if (goalService.deleteGoal(selected.getId())) {
            System.out.println("Goal deleted: " + selected.getName());
        } else {
            System.out.println("Failed to delete goal.");
        }
    }

    private Goal selectGoal(List<Goal> goals, String prompt) {
        System.out.println("\n" + prompt + ":");
        for (int i = 0; i < goals.size(); i++) {
            Goal g = goals.get(i);
            System.out.printf("  %d. %s %s  (%.0f%% — ₹%.2f/₹%.2f)%n",
                    i + 1, g.getIcon(), g.getName(),
                    g.getProgressPercent(), g.getCurrentSavings(), g.getTargetAmount());
        }
        int choice = readInt("Enter number: ");
        if (choice < 1 || choice > goals.size()) {
            System.out.println("Invalid selection.");
            return null;
        }
        return goals.get(choice - 1);
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val > 0) return val;
                System.out.println("Please enter a value greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid amount.");
            }
        }
    }

    private LocalDate readFutureDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            LocalDate date = LocalDate.parse(input, DATE_FORMAT);
            if (!date.isAfter(LocalDate.now())) {
                System.out.println("Date must be in the future.");
                return null;
            }
            return date;
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
            return null;
        }
    }

    // ── Monthly Report ────────────────────────────────────────────────────────

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
