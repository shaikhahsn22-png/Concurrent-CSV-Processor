package com.ga.concurrent_csv_processor.Service;

import com.ga.concurrent_csv_processor.Model.Employee;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvReaderService {

    //method to read employee's data from csv file
    public List<Employee> readEmployees(){
        //list of employees
        ArrayList<Employee> employees = new ArrayList<>();

        //open the csv file
        InputStream inputStream = getClass().getResourceAsStream("/test_employees.csv"); //getResourceAsStream: find and open the file as bytes

        //check if file is null
        if(inputStream == null){
            throw new IllegalStateException("Employee CSV file was not found.");
        }

        //InputStreamReader: convert bytes into text char
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))){

            //read csv file one line at a time
            String line;
            while ((line = reader.readLine()) != null){
                //convert each row into employee object
                String[] value = line.split(",");

                //check num of column
                if(value.length != 6){
                    throw new IllegalStateException("Employee CSV contains a row without six columns.");
                }
                int id = Integer.parseInt(value[0]);
                String name = value[1];
                BigDecimal salary = new BigDecimal(value[2]);
                LocalDate joinDate = LocalDate.parse(value[3]);
                String role = value[4];
                double projectCompletion = Double.parseDouble(value[5]);

                //create employee object
                Employee employee = new Employee(id, name, salary, joinDate, role, projectCompletion);

                //add employee to the list
                employees.add(employee);
            }

        }catch (IOException e) {
            throw new IllegalStateException("Could not read the employee CSV file.", e);
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException e) {
            throw new IllegalStateException("Employee CSV contains an invalid number or joining date.", e);
        }


        return employees;
    }
}
