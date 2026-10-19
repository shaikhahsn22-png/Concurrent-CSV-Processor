package com.ga.concurrent_csv_processor.Service;

import com.ga.concurrent_csv_processor.Model.Employee;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class SalaryCalculator {

    //method to calculate employee's salary
    public BigDecimal calculateNewSalary(Employee employee){
        //check eligibility (project completion)
        // if project completion below 60% = no increase
        if(employee.getProjectCompletion() < 0.6){
            return employee.getSalary();
        }

        //calculate role-based increase
        double roleIncrease = 0;
        if(employee.getRole().equalsIgnoreCase("director")){
            roleIncrease = 5;
        } else if (employee.getRole().equalsIgnoreCase("manager")) {
            roleIncrease = 2;
        } else if (employee.getRole().equalsIgnoreCase("employee")) {
            roleIncrease = 1;
        } else {
            roleIncrease = 0;
        }

        //calculate years of service
        //2% for every completed year of employment
        long yearsCompleted = ChronoUnit.YEARS.between(employee.getJoinDate(), LocalDate.now());
        double yearIncrease = yearsCompleted * 2;

        //calculate the final salary
        double totalIncrease = (roleIncrease + yearIncrease) / 100;
        BigDecimal newSalary = employee.getSalary().multiply(BigDecimal.valueOf(1 + totalIncrease));
        newSalary = newSalary.setScale(3, RoundingMode.HALF_UP); //to keep three digits after the decimal point

        //return new salary
        return newSalary;
    }
}
