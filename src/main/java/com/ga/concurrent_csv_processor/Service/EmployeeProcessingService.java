package com.ga.concurrent_csv_processor.Service;

import com.ga.concurrent_csv_processor.Model.Employee;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class EmployeeProcessingService {
    private final SalaryCalculator salaryCalculator;
    private final CsvReaderService csvReaderService;

    public EmployeeProcessingService(SalaryCalculator salaryCalculator, CsvReaderService csvReaderService) {
        this.salaryCalculator = salaryCalculator;
        this.csvReaderService = csvReaderService;
    }

    //thread pooling method
    public void processEmployees(List<Employee> employees){
        //create a fixed-size thread pool with 5 worker threads
        ExecutorService executor = Executors.newFixedThreadPool(5);

        //submit employee tasks
        for (int i = 0; i < employees.size(); i++) {
            //retrieve current employee
            Employee employee = employees.get(i);

            //send the task to the thread pool
            executor.submit(() -> {
                //process one employee
                BigDecimal newSalary = salaryCalculator.calculateNewSalary(employee);
                System.out.println("Name: " + employee.getName() + " | Original Salary: " + employee.getSalary()+ " | New Salary: "+newSalary);
            });
        }

        //shutdown the executor
        executor.shutdown();
        try{
            //wait for all tasks to finish
            boolean finished =executor.awaitTermination(1, TimeUnit.MINUTES);
            if (finished) {
                System.out.println("All employee tasks completed successfully");
            } else {
                System.out.println("Timeout! Some employee tasks have not finished");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    //method to start processing
    public void startProcessing(){
        //read the employees file
        List<Employee> employees = csvReaderService.readEmployees();

        //start processing employees salaries
        processEmployees(employees);
    }
}
