package com.example.prog4swa.service;

import com.example.prog4swa.model.Employee;
import com.example.prog4swa.repository.EmployeeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
@Transactional
public class EmployeeService {
    private final EmployeeRepository repository;
    private final EntityManager entityManager;

    public List<Employee> getEmployees() {
        return repository.findAll();
    }

    public Employee getEmployeeById(int id) {
        Optional<Employee> employee = repository.findById(id);
        if(employee.isPresent()){
            return employee.get();
        } else {
            throw new RuntimeException("Resource Employee Not Found");
        }
    }

    public void addOrUpdateEmployee(Employee newEmployee){
        repository.save(newEmployee);
    }

    public boolean isSerialNumberExists(String serialNumber) {
        Optional<Employee> optionalEmployee = repository.findBySerialNumber(serialNumber);
        return optionalEmployee.isPresent();
    }

    public List<Employee> customSearch(String firstName, String lastName, String gender, String position, String hireDate, String departureDate, String countryCode, String sort) {
        StringBuilder queryString = new StringBuilder("SELECT e FROM Employee e WHERE 1 = 1");
        if (firstName != null && !firstName.isEmpty()) {
            queryString.append(" AND e.firstName ILIKE :firstName");
        }
        if (lastName != null && !lastName.isEmpty()) {
            queryString.append(" AND e.lastName ILIKE :lastName");
        }
        if (gender != null && !gender.isEmpty()) {
            queryString.append(" AND e.gender = :gender");
        }
        if (position != null && !position.isEmpty()) {
            queryString.append(" AND e.position ILIKE :position");
        }
        if (hireDate != null && !hireDate.isEmpty()) {
            queryString.append(" AND e.hireDate = :hireDate");
        }
        if (departureDate != null && !departureDate.isEmpty()) {
            queryString.append(" AND e.departureDate = :departureDate");
        }
        if (countryCode != null && !countryCode.isEmpty()) {
            queryString.append(" AND EXISTS (SELECT p FROM e.phoneNumbers p WHERE p ILIKE :countryCode)");
        }

        if (!sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            String sortOrder = sortParams[1];
            queryString.append(" ORDER BY e.").append(sortField).append(" ").append(sortOrder);
        }

        Query query = entityManager.createQuery(queryString.toString());

        if (firstName != null && !firstName.isEmpty()) {
            query.setParameter("firstName", "%" + firstName.trim() + "%");
        }
        if (lastName != null && !lastName.isEmpty()) {
            query.setParameter("lastName", "%" + lastName.trim() + "%");
        }
        if (gender != null && !gender.isEmpty()) {
            query.setParameter("gender", Employee.Gender.valueOf(gender));
        }
        if (position != null && !position.isEmpty()) {
            query.setParameter("position", "%" + position.trim() + "%");
        }
        if (hireDate != null && !hireDate.isEmpty()) {
            query.setParameter("hireDate", LocalDate.parse(hireDate));
        }
        if (departureDate != null && !departureDate.isEmpty()) {
            query.setParameter("departureDate", LocalDate.parse(departureDate));
        }
        if (countryCode != null && !countryCode.isEmpty()) {
            query.setParameter("countryCode", "%" + countryCode.trim() + "%");
        }

        return query.getResultList();
    }

    public String convertToBase64Photo(MultipartFile photoFile) {
        try {
            byte[] photoBytes = photoFile.getBytes();
            return Base64.getEncoder().encodeToString(photoBytes);
        } catch (IOException e) {
            return null;
        }
    }

    public String generateSerialNumber(LocalDate hireDate) {
        Random random = new Random();
        int randomNumber = random.nextInt(90000) + 10000; //10000 - 99999
        String serialNumber = "EMP-" + hireDate.toString() + "-" + randomNumber;

        while (isSerialNumberExists(serialNumber)) {
            randomNumber = random.nextInt(90000) + 10000;
            serialNumber = "EMP-" + hireDate.toString() + "-" + randomNumber;
        }

        return serialNumber;
    }

    public String formatPhoneNumbersToString(List<String> phoneNumbers) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner("\n");

        for (String phoneNumber : phoneNumbers) {
            joiner.add(phoneNumber.trim());
        }
        return joiner.toString();
    }

    public List<String> formatStringToPhoneNumbers(String phoneNumbers) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            return new ArrayList<>();
        }
        String[] phoneNumbersArray = phoneNumbers.split("\n");
        List<String> formattedPhoneNumbers = new ArrayList<>();
        for (String phoneNumber : phoneNumbersArray) {
            formattedPhoneNumbers.add(phoneNumber.trim());
        }
        return formattedPhoneNumbers;    }
}
