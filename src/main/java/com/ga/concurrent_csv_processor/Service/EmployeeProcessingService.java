package com.ga.concurrent_csv_processor.Service;

import com.ga.concurrent_csv_processor.Model.Employee;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class EmployeeProcessingService {
    private final SalaryCalculator salaryCalculator;
    private final CsvReaderService csvReaderService;

    //store calculated salary results
    private final List<String> results = new ArrayList<>();

    //protect the shared results list
    private final ReentrantLock lock = new ReentrantLock();

    //limited number of threads to access a resource simultaneously
    private final Semaphore semaphore = new Semaphore(3);

    public EmployeeProcessingService(SalaryCalculator salaryCalculator, CsvReaderService csvReaderService) {
        this.salaryCalculator = salaryCalculator;
        this.csvReaderService = csvReaderService;
    }

    /**
     * Process employees salaries using thread pool.
     * 5 worker threads are used to process the employees.
     * A semaphore allows only three threads to process at the same time.
     * ReentrantLock makes sure only one thread adds a result to the list at a time.
     * After processing, the semaphore permit is released.
     * The executor waits for all tasks to finish before printing the results.
     *
     * @param employees the list of employees to process
     */
    public void processEmployees(List<Employee> employees){
        //create a fixed-size thread pool with 5 worker threads
        ExecutorService executor = Executors.newFixedThreadPool(5);

        //submit employee tasks
        for (int i = 0; i < employees.size(); i++) {
            //retrieve current employee
            Employee employee = employees.get(i);

            //send the task to the thread pool
            executor.submit(() -> {
                try {
                    //call semaphore
                    semaphore.acquire();
                    System.out.println("Thread: " + Thread.currentThread().getName()
                            + " | Processing Employee: " + employee.getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                try {
                    //process one employee
                    BigDecimal newSalary = salaryCalculator.calculateNewSalary(employee);
                    String result = "Name: " + employee.getName() + " | Original Salary: " + employee.getSalary()+ " | New Salary: "+newSalary;

                    //adding one result to the list at a time using locks
                    lock.lock();
                    try {
                        results.add(result);
                    } finally {
                        lock.unlock();
                    }
                } finally {
                    semaphore.release();
                }


            });
        }

        //shutdown the executor
        executor.shutdown();
        try{
            //wait for all tasks to finish
            boolean finished =executor.awaitTermination(1, TimeUnit.MINUTES);
            if (finished) {
                System.out.println("All employee tasks completed successfully");
                //print the list
                for (String result : results){
                    System.out.println(result);
                }
                //print total employees processed
                System.out.println("Total employees processed: "+ results.size());
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
        lock.lock();
        try {
            //clear the results list
            results.clear();
        }finally {
            lock.unlock();
        }

        //read the employees file
        List<Employee> employees = csvReaderService.readEmployees();

        //start processing employees salaries
        processEmployees(employees);
    }
}
