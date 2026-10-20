package com.ga.concurrent_csv_processor.Service;

import com.ga.concurrent_csv_processor.Model.Employee;
import com.ga.concurrent_csv_processor.Model.EmployeeResult;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class EmployeeProcessingService {
    private final SalaryCalculator salaryCalculator;
    private final CsvReaderService csvReaderService;
    private final ReentrantLock lock = new ReentrantLock();
    private final Semaphore semaphore = new Semaphore(3);

    /** receives the existing calculator and CSV reader */
    public EmployeeProcessingService(SalaryCalculator salaryCalculator, CsvReaderService csvReaderService) {
        this.salaryCalculator = salaryCalculator;
        this.csvReaderService = csvReaderService;
    }

    /** processes a separate batch with five workers(threads) and return complete results */
    public List<EmployeeResult> processEmployees(List<Employee> employees) {
        // Each request owns its lists, so simultaneous requests cannot mix results.
        List<EmployeeResult> results = new ArrayList<>();
        List<Exception> failures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        AtomicInteger processedCount = new AtomicInteger(0); //counter starts at 0

        for (Employee employee : employees) {
            executor.execute(() -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    EmployeeResult result = new EmployeeResult(employee, salaryCalculator.calculateNewSalary(employee));
                    lock.lock();
                    try {
                        results.add(result);
                        processedCount.incrementAndGet(); //increment counter
                    } finally {
                        lock.unlock();
                    }
                } catch (RuntimeException e) {

                    lock.lock();
                    try {
                        failures.add(e);
                    } finally {
                        lock.unlock();
                    }
                } finally {
                    semaphore.release();
                }
            });
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                throw new IllegalStateException("Processing timed out. Please try again.");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Employee processing was interrupted. Please try again.", e);
        }
        if (!failures.isEmpty() || results.size() != employees.size()) {
            throw new IllegalStateException("Some employees could not be processed. Please check the CSV data.");
        }
        results.sort(Comparator.comparingInt(EmployeeResult::getId));
        for (EmployeeResult result : results) {
            System.out.println("Name: " + result.getName() + " | Original Salary: "
                    + result.getOriginalSalary() + " | New Salary: " + result.getNewSalary());
        }
        System.out.println("Total employees processed: " + processedCount.get());
        return results;
    }

    /** reads the CSV again for every request and starts concurrent processing. */
    public List<EmployeeResult> startProcessing() {
        return processEmployees(csvReaderService.readEmployees());
    }
}
