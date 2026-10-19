package com.ga.concurrent_csv_processor.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {
    //fields
    private int id;
    private String name;
    private BigDecimal salary;
    private LocalDate joinDate;
    private String role;
    private double projectCompletion;

    //constructor
    public Employee(int id, String name, BigDecimal salary, LocalDate joinDate, String role, double projectCompletion) {
        this.id = id;
        this.name = name;
        this.salary = salary;
        this.joinDate = joinDate;
        this.role = role;
        this.projectCompletion = projectCompletion;
    }

    //getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public String getRole() {
        return role;
    }

    public double getProjectCompletion() {
        return projectCompletion;
    }
}
