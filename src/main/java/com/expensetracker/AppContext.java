package com.expensetracker;



import com.expensetracker.repository.BudgetRepository;

import com.expensetracker.repository.CategoryRepository;

import com.expensetracker.repository.ExpenseRepository;

import com.expensetracker.repository.SettingsRepository;

import com.expensetracker.service.BudgetService;

import com.expensetracker.service.CategoryService;

import com.expensetracker.service.ExpenseService;

import com.expensetracker.service.InsightsService;

import com.expensetracker.service.ReportService;



import java.nio.file.Path;



public class AppContext {

    private final CategoryService categoryService;

    private final ExpenseService expenseService;

    private final BudgetService budgetService;

    private final ReportService reportService;

    private final InsightsService insightsService;



    private AppContext(CategoryService categoryService,

                       ExpenseService expenseService,

                       BudgetService budgetService,

                       ReportService reportService,

                       InsightsService insightsService) {

        this.categoryService = categoryService;

        this.expenseService = expenseService;

        this.budgetService = budgetService;

        this.reportService = reportService;

        this.insightsService = insightsService;

    }



    public static AppContext create() {

        Path dataDir = Path.of(System.getProperty("user.home"), ".smart-expense-tracker");



        CategoryRepository categoryRepository = new CategoryRepository(dataDir);

        ExpenseRepository expenseRepository = new ExpenseRepository(dataDir);

        BudgetRepository budgetRepository = new BudgetRepository(dataDir);

        SettingsRepository settingsRepository = new SettingsRepository(dataDir);



        CategoryService categoryService = new CategoryService(categoryRepository);

        ExpenseService expenseService = new ExpenseService(expenseRepository, categoryRepository);

        BudgetService budgetService = new BudgetService(budgetRepository, categoryRepository, expenseRepository);

        ReportService reportService = new ReportService(expenseRepository, categoryRepository, budgetRepository);

        InsightsService insightsService = new InsightsService(

                categoryRepository, expenseRepository, budgetRepository, settingsRepository);



        return new AppContext(categoryService, expenseService, budgetService, reportService, insightsService);

    }



    public CategoryService categoryService() {

        return categoryService;

    }



    public ExpenseService expenseService() {

        return expenseService;

    }



    public BudgetService budgetService() {

        return budgetService;

    }



    public ReportService reportService() {

        return reportService;

    }



    public InsightsService insightsService() {

        return insightsService;

    }

}


