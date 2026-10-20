package com.ga.concurrent_csv_processor;

import com.ga.concurrent_csv_processor.Model.Employee;
import com.ga.concurrent_csv_processor.Model.EmployeeResult;
import com.ga.concurrent_csv_processor.Service.CsvReaderService;
import com.ga.concurrent_csv_processor.Service.EmployeeProcessingService;
import com.ga.concurrent_csv_processor.Service.SalaryCalculator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeProcessingServiceTests {
    private final SalaryCalculator calculator = new SalaryCalculator();
    private final EmployeeProcessingService service = new EmployeeProcessingService(calculator, new CsvReaderService());

    @Test
    void repeatedRunsHaveCompleteIndependentResults() {
        List<EmployeeResult> first = service.startProcessing();
        List<EmployeeResult> second = service.startProcessing();
        assertEquals(30, first.size());
        assertEquals(30, second.size());
        assertNotSame(first, second);
        assertEquals(30, second.stream().map(EmployeeResult::getId).distinct().count());
        assertEquals(21, second.stream().filter(result -> !result.getStatus().equals("No Increase")).count());
        assertEquals(3, second.stream().filter(result -> result.getStatus().equals("Bonus Applied")).count());
    }

    @Test
    void simultaneousBatchesDoNotMixResults() throws Exception {
        ExecutorService callers = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<List<EmployeeResult>> first = callers.submit(() -> {
                start.await();
                return service.processEmployees(List.of(employee(101, 0.9)));
            });
            Future<List<EmployeeResult>> second = callers.submit(() -> {
                start.await();
                return service.processEmployees(List.of(employee(202, 0.6)));
            });
            start.countDown();
            assertEquals(List.of(101), first.get(10, TimeUnit.SECONDS).stream().map(EmployeeResult::getId).toList());
            assertEquals(List.of(202), second.get(10, TimeUnit.SECONDS).stream().map(EmployeeResult::getId).toList());
        } finally {
            callers.shutdownNow();
        }
    }

    @Test
    void workerFailureDoesNotReturnPartialSuccess() {
        SalaryCalculator failingCalculator = mock(SalaryCalculator.class);
        when(failingCalculator.calculateNewSalary(any())).thenThrow(new IllegalArgumentException("Bad data"));
        EmployeeProcessingService failingService = new EmployeeProcessingService(failingCalculator, new CsvReaderService());
        assertThrows(IllegalStateException.class, () -> failingService.processEmployees(List.of(employee(1, 0.9))));
    }

    @Test
    void existingCalculatorPreservesEligibilityAndBonusBoundaries() {
        double[] completion = {0.59, 0.60, 0.80, 0.81};
        String[] expectedSalary = {"1000.000", "1050.000", "1050.000", "1075.000"};
        String[] expectedStatus = {"No Increase", "Increased", "Increased", "Bonus Applied"};
        for (int i = 0; i < completion.length; i++) {
            Employee employee = employee(i, completion[i]);
            EmployeeResult result = new EmployeeResult(employee, calculator.calculateNewSalary(employee));
            assertEquals(new BigDecimal(expectedSalary[i]), result.getNewSalary());
            assertEquals(expectedStatus[i], result.getStatus());
        }
    }

    // Two completed years plus the employee's 1% role increase gives 5% before bonus.
    private Employee employee(int id, double completion) {
        return new Employee(id, "Employee " + id, new BigDecimal("1000"),
                LocalDate.now().minusYears(2), "Employee", completion);
    }
}
