package com.examples.repository;

import java.util.List;
import java.util.Optional;

import com.examples.model.Employee;

public interface EmployeeRepository {

	List<Employee> findAll();

	Optional<Employee> findOne(String id);

	Employee save(Employee employee);

	Employee deleteById(String id);
}