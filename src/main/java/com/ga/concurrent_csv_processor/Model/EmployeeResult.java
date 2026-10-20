package com.ga.concurrent_csv_processor.Model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** One employee and the salary returned by the existing calculator. */
public class EmployeeResult {
    private final Employee employee;
    private final BigDecimal newSalary;

    /** Keeps the original employee alongside the calculated salary. */
    public EmployeeResult(Employee employee, BigDecimal newSalary) {
        this.employee = employee;
        this.newSalary = newSalary;
    }
    /** Returns the employee ID for JSON and sorting. */
    public int getId() { return employee.getId(); }
    /** Returns the name for display and search. */
    public String getName() { return employee.getName(); }
    /** Returns the employee role. */
    public String getRole() { return employee.getRole(); }
    /** Formats the original salary to three decimal places. */
    public BigDecimal getOriginalSalary() { return employee.getSalary().setScale(3, RoundingMode.HALF_UP); }
    /** Formats the calculator result to three decimal places. */
    public BigDecimal getNewSalary() { return newSalary.setScale(3, RoundingMode.HALF_UP); }
    /** Derives the displayed increase from the salaries without repeating salary rules. */
    public BigDecimal getIncreasePercentage() {
        if (employee.getSalary().signum() == 0) { return BigDecimal.ZERO; }
        return newSalary.subtract(employee.getSalary()).multiply(BigDecimal.valueOf(100))
                .divide(employee.getSalary(), 3, RoundingMode.HALF_UP);
    }
    /** Returns the original completion fraction: 0.8 means 80%. */
    public double getProjectCompletion() { return employee.getProjectCompletion(); }
    /** Labels actual increases; completion strictly above 80% earns the bonus label. */
    public String getStatus() {
        if (newSalary.compareTo(employee.getSalary()) <= 0) { return "No Increase"; }
        return employee.getProjectCompletion() > 0.8 ? "Bonus Applied" : "Increased";
    }
}
