# Concurrent CSV Data Processor

## Project Overview

This project is a Java Spring Boot application that reads employee data from a CSV file and calculates salary increases based on different conditions.

The application uses multithreading to process employees at the same time instead of processing them one by one. It also uses thread pooling, ReentrantLock, and Semaphore to manage threads and keep the results safe.

The main goal of this project is to practice Java concurrency and apply salary calculation rules to employee data.

## Technologies Used

- Java 17
- Spring Boot
- Maven
- IntelliJ IDEA
- Git and GitHub
- ExecutorService
- ReentrantLock
- Semaphore
- BufferedReader

## Project Structure

The project contains the following main classes:

### 1. Employee.java

This class stores employee information, including:

- Employee ID
- Name
- Current salary
- Joining date
- Role
- Project completion percentage

It includes a constructor, getters, and a `toString()` method.

### 2. CsvReaderService.java

This class reads employee data from the `test_employees.csv` file using BufferedReader.

It reads the file line by line, splits each row into values, and creates an Employee object for each valid row.

It also checks whether the CSV file exists and whether each row contains the correct number of columns.

### 3. SalaryCalculator.java

This class calculates the new salary for each employee based on the project requirements.

The salary calculation follows these rules:

**Project Completion:**
- Below 60%: No salary increase.
- 60% or above: Eligible for a salary increase.

**Role-Based Increase:**
- Director: 5%
- Manager: 2%
- Employee: 1%

**Years of Service:**
- Each completed year adds 2% to the salary increase.
- Employees who have not completed one full year do not receive the years-of-service increase.

The total increase is calculated by adding the role percentage and years-of-service percentage.

The new salary is then calculated using the original salary and the total increase.

BigDecimal is used for salary calculations, and the final result is rounded to three decimal places.

### 4. EmployeeProcessingService.java

This class handles the concurrent processing of employee records.

It uses an ExecutorService with a fixed thread pool of five worker threads.

Each employee is submitted as a task, and an available worker thread calculates the employee's new salary.

A Semaphore with three permits is used to limit the number of threads processing employees at the same time.

ReentrantLock is used when adding salary results to a shared ArrayList. This prevents multiple threads from modifying the list at the same time.

After submitting all tasks, the executor is shut down and the application waits for the tasks to finish before displaying the results.

### 5. ConcurrentCsvProcessorApplication.java

This is the main Spring Boot application class.

It starts the application and calls the employee processing service using CommandLineRunner.

## CSV File

The application reads employee information from:

`src/main/resources/test_employees.csv`

Each row contains six values in the following order:

`ID, Name, Salary, Joining Date, Role, Project Completion`

Example:

```csv
1,Alice,52000.0,2019-05-12,Employee,0.8
```

The project completion value is stored as a decimal, where `0.8` means 80%.

## How to Run the Project

1. Clone or download the project from GitHub.
2. Open the project in IntelliJ IDEA.
3. Make sure Java 17 and Maven are available.
4. Make sure `test_employees.csv` is inside `src/main/resources`.
5. Let Maven download the required dependencies.
6. Run `ConcurrentCsvProcessorApplication.java`.
7. Check the console to see the original and updated salaries.

The application is configured to run on port 8081.

## How Concurrency Works

The application uses three main concurrency techniques.

**1. Thread Pooling**

ExecutorService creates a fixed thread pool with five workers. The workers are reused to process all the employee tasks.

**2. ReentrantLock**

ReentrantLock protects the shared results list. Only one thread can add a result to the list at a time.

**3. Semaphore**

Semaphore limits the number of employees being processed at the same time to three, even though the thread pool contains five workers.

After processing, each worker releases its semaphore permit so another worker can continue.

## Testing and Results

I tested the application using the provided CSV file containing 30 employees.

The application successfully:

- Read the employee data from the CSV file.
- Calculated salary increases using the required conditions.
- Kept the original salary for employees with less than 60% project completion.
- Used five worker threads to process employee tasks.
- Used Semaphore to limit processing to three threads at a time.
- Used ReentrantLock to protect the shared results list.
- Displayed the original and new salaries.
- Processed all 30 employee records.

I also tested the CSV reader with a missing file and an invalid row containing the wrong number of columns.

For the Semaphore test, I temporarily added a one-second delay to observe how three threads processed tasks at the same time. I removed the delay after testing.

**Final Output:**

```text
All employee tasks completed successfully
Total employees processed: 30
```
