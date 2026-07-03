package com.expensetracker.model;

import java.util.UUID;

public class Category {
    private String id;
    private String name;

    public Category() {
        this.id = UUID.randomUUID().toString();
    }

    public Category(String name) {
        this();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
