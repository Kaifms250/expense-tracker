package com.expensetracker.web;

import com.expensetracker.AppContext;
import com.expensetracker.model.Budget;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.MonthlyReport;
import com.expensetracker.service.BudgetService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiHandler implements HttpHandler {
    private final AppContext app;

    public ApiHandler(AppContext app) {
        this.app = app;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (path.startsWith("/api/categories")) {
                handleCategories(exchange, path, method);
            } else if (path.startsWith("/api/expenses")) {
                handleExpenses(exchange, path, method);
            } else if (path.startsWith("/api/budgets/status")) {
                handleBudgetStatus(exchange, method);
            } else if (path.startsWith("/api/budgets")) {
                handleBudgets(exchange, path, method);
            } else if (path.startsWith("/api/reports")) {
                handleReports(exchange, method);
            } else if (path.startsWith("/api/insights")) {
                handleInsights(exchange, method);
            } else if (path.startsWith("/api/settings")) {
                handleSettings(exchange, method);
            } else {
                HttpUtil.sendError(exchange, 404, "Not found");
            }
        } catch (IllegalArgumentException e) {
            HttpUtil.sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtil.sendError(exchange, 500, "Internal server error");
        }
    }

    private void handleCategories(HttpExchange exchange, String path, String method) throws IOException {
        if (path.equals("/api/categories") && "GET".equals(method)) {
            HttpUtil.sendJson(exchange, 200, app.categoryService().getAllCategories());
            return;
        }
        if (path.equals("/api/categories") && "POST".equals(method)) {
            NameRequest req = HttpUtil.parseBody(exchange, NameRequest.class);
            Category category = app.categoryService().addCategory(req.name);
            HttpUtil.sendJson(exchange, 201, category);
            return;
        }
        if (path.startsWith("/api/categories/") && "DELETE".equals(method)) {
            String id = path.substring("/api/categories/".length());
            if (app.categoryService().deleteCategory(id)) {
                app.budgetService().removeBudget(id);
                HttpUtil.sendNoContent(exchange);
            } else {
                HttpUtil.sendError(exchange, 404, "Category not found");
            }
            return;
        }
        HttpUtil.sendError(exchange, 405, "Method not allowed");
    }

    private void handleExpenses(HttpExchange exchange, String path, String method) throws IOException {
        if (path.equals("/api/expenses") && "GET".equals(method)) {
            String monthParam = HttpUtil.queryParams(exchange).get("month");
            List<Expense> expenses;
            if (monthParam != null && !monthParam.isBlank()) {
                expenses = app.expenseService().getExpensesForMonth(parseMonth(monthParam));
            } else {
                expenses = app.expenseService().getAllExpenses();
            }
            HttpUtil.sendJson(exchange, 200, toExpenseViews(expenses));
            return;
        }
        if (path.equals("/api/expenses") && "POST".equals(method)) {
            ExpenseRequest req = HttpUtil.parseBody(exchange, ExpenseRequest.class);
            LocalDate date = req.date != null && !req.date.isBlank()
                    ? LocalDate.parse(req.date)
                    : LocalDate.now();
            Expense expense = app.expenseService().addExpense(
                    req.amount, req.categoryId, date, req.description);
            HttpUtil.sendJson(exchange, 201, toExpenseView(expense));
            return;
        }
        if (path.startsWith("/api/expenses/") && "DELETE".equals(method)) {
            String id = path.substring("/api/expenses/".length());
            if (app.expenseService().deleteExpense(id)) {
                HttpUtil.sendNoContent(exchange);
            } else {
                HttpUtil.sendError(exchange, 404, "Expense not found");
            }
            return;
        }
        HttpUtil.sendError(exchange, 405, "Method not allowed");
    }

    private void handleBudgets(HttpExchange exchange, String path, String method) throws IOException {
        if (path.equals("/api/budgets") && "GET".equals(method)) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Budget budget : app.budgetService().getAllBudgets()) {
                String categoryName = app.categoryService().getCategoryById(budget.getCategoryId())
                        .map(Category::getName)
                        .orElse("Unknown");
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", budget.getId());
                item.put("categoryId", budget.getCategoryId());
                item.put("categoryName", categoryName);
                item.put("monthlyLimit", budget.getMonthlyLimit());
                result.add(item);
            }
            HttpUtil.sendJson(exchange, 200, result);
            return;
        }
        if (path.equals("/api/budgets") && "POST".equals(method)) {
            BudgetRequest req = HttpUtil.parseBody(exchange, BudgetRequest.class);
            Budget budget = app.budgetService().setBudget(req.categoryId, req.monthlyLimit);
            HttpUtil.sendJson(exchange, 201, budget);
            return;
        }
        if (path.startsWith("/api/budgets/") && "DELETE".equals(method)) {
            String categoryId = path.substring("/api/budgets/".length());
            if (app.budgetService().removeBudget(categoryId)) {
                HttpUtil.sendNoContent(exchange);
            } else {
                HttpUtil.sendError(exchange, 404, "Budget not found");
            }
            return;
        }
        HttpUtil.sendError(exchange, 405, "Method not allowed");
    }

    private void handleBudgetStatus(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equals(method)) {
            HttpUtil.sendError(exchange, 405, "Method not allowed");
            return;
        }
        YearMonth month = resolveMonth(HttpUtil.queryParams(exchange).get("month"));
        List<BudgetService.BudgetStatus> statuses = app.budgetService().getBudgetStatusForMonth(month);
        List<Map<String, Object>> result = new ArrayList<>();
        for (BudgetService.BudgetStatus status : statuses) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("categoryName", status.categoryName());
            item.put("limit", status.limit());
            item.put("spent", status.spent());
            item.put("remaining", status.remaining());
            item.put("percentUsed", status.percentUsed());
            item.put("overBudget", status.isOverBudget());
            result.add(item);
        }
        HttpUtil.sendJson(exchange, 200, result);
    }

    private void handleInsights(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equals(method)) {
            HttpUtil.sendError(exchange, 405, "Method not allowed");
            return;
        }
        YearMonth month = resolveMonth(HttpUtil.queryParams(exchange).get("month"));
        HttpUtil.sendJson(exchange, 200, app.insightsService().getInsights(month));
    }

    private void handleSettings(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            HttpUtil.sendJson(exchange, 200, app.insightsService().getSettings());
            return;
        }
        if ("PUT".equals(method) || "POST".equals(method)) {
            SettingsRequest req = HttpUtil.parseBody(exchange, SettingsRequest.class);
            var settings = app.insightsService().updateSettings(req.monthlyIncome, req.userName);
            HttpUtil.sendJson(exchange, 200, settings);
            return;
        }
        HttpUtil.sendError(exchange, 405, "Method not allowed");
    }

    private void handleReports(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equals(method)) {
            HttpUtil.sendError(exchange, 405, "Method not allowed");
            return;
        }
        YearMonth month = resolveMonth(HttpUtil.queryParams(exchange).get("month"));
        MonthlyReport report = app.reportService().generateMonthlyReport(month);
        HttpUtil.sendJson(exchange, 200, report);
    }

    private List<Map<String, Object>> toExpenseViews(List<Expense> expenses) {
        List<Map<String, Object>> views = new ArrayList<>();
        for (Expense expense : expenses) {
            views.add(toExpenseView(expense));
        }
        return views;
    }

    private Map<String, Object> toExpenseView(Expense expense) {
        String categoryName = app.categoryService().getCategoryById(expense.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", expense.getId());
        view.put("amount", expense.getAmount());
        view.put("categoryId", expense.getCategoryId());
        view.put("categoryName", categoryName);
        view.put("date", expense.getDate().toString());
        view.put("description", expense.getDescription());
        return view;
    }

    private YearMonth resolveMonth(String monthParam) {
        if (monthParam == null || monthParam.isBlank()) {
            return YearMonth.now();
        }
        return parseMonth(monthParam);
    }

    private YearMonth parseMonth(String value) {
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid month format. Use yyyy-MM.");
        }
    }

    private static class NameRequest {
        String name;
    }

    private static class ExpenseRequest {
        double amount;
        String categoryId;
        String date;
        String description;
    }

    private static class BudgetRequest {
        String categoryId;
        double monthlyLimit;
    }

    private static class SettingsRequest {
        double monthlyIncome;
        String userName;
    }
}
