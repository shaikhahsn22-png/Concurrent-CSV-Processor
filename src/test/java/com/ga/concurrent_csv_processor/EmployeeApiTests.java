package com.ga.concurrent_csv_processor;

import com.ga.concurrent_csv_processor.Controller.EmployeeController;
import com.ga.concurrent_csv_processor.Service.EmployeeProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeApiTests {
    @LocalServerPort
    private int port;
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void dashboardAndAssetsAreServed() throws Exception {
        for (String path : new String[]{"/", "/style.css", "/script.js"}) {
            HttpResponse<String> response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertFalse(response.body().isBlank());
            if (path.equals("/")) assertTrue(response.body().contains("Process Employees"));
        }
    }

    @Test
    void endpointReturnsStructuredResultsOnRepeatedRequests() throws Exception {
        for (int i = 0; i < 2; i++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/employees/process"))
                    .POST(HttpRequest.BodyPublishers.noBody()).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("content-type").orElse("").contains("application/json"));
            assertEquals(30, response.body().split("\"originalSalary\":", -1).length - 1);
            assertTrue(response.body().contains("\"status\":\"Bonus Applied\""));
            assertTrue(response.body().contains("\"increasePercentage\":"));
        }
    }

    @Test
    void controllerReturnsReadableFailure() {
        EmployeeProcessingService service = mock(EmployeeProcessingService.class);
        when(service.startProcessing()).thenThrow(new IllegalStateException("CSV could not be read."));
        EmployeeController controller = new EmployeeController(service);
        IllegalStateException failure = assertThrows(IllegalStateException.class, controller::processEmployees);
        var response = controller.handleProcessingFailure(failure);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("CSV could not be read.", response.getBody().get("message"));
    }
}
