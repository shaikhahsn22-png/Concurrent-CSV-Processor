package com.ga.concurrent_csv_processor.Service;

import com.ga.concurrent_csv_processor.Model.Employee;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
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

        try (BufferedReader reader = new BufferedReader(
                //InputStreamReader: convert bytes into text char, getResourceAsStream: find and open the file as bytes
                //search for the resource and opens a stream to read file content
                new InputStreamReader(getClass().getResourceAsStream("/test_employees.csv")))){

            //read csv file one line at a time
            String line;
            while ((line = reader.readLine()) != null){
                //convert each row into employee object
                String[] value = line.split(",");
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
            e.printStackTrace();
        }


        return employees;
    }
}
