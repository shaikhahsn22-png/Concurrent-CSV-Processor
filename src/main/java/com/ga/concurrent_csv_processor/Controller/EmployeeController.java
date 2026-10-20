package com.ga.concurrent_csv_processor.Controller;

import com.ga.concurrent_csv_processor.Model.EmployeeResult;
import com.ga.concurrent_csv_processor.Service.EmployeeProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeProcessingService service;
    /** Receives the existing concurrent processing service from Spring. */
    public EmployeeController(EmployeeProcessingService service) { this.service = service; }
    /** Returns JSON only after all employee tasks finish. */
    @PostMapping("/process")
    public List<EmployeeResult> processEmployees() { return service.startProcessing(); }
    /** Gives the dashboard a readable error instead of partial results. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleProcessingFailure(IllegalStateException error) {
        return ResponseEntity.internalServerError().body(Map.of("message", error.getMessage()));
    }
}
