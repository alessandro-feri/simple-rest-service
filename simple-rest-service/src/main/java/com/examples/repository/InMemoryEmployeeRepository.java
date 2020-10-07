package com.examples.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.examples.model.Employee;

/**
 * An example repository implementation for employees.
 * 
 * In a real application this should be handled by a database.
 */
public class InMemoryEmployeeRepository implements EmployeeRepository {

	private Map<String, Employee> employees;

	@Inject
	public InMemoryEmployeeRepository(Map<String, Employee> employees) {
		this.employees = employees;
		// initialize the "db" with some contents
		put(new Employee("ID1", "First Employee", 1000));
		put(new Employee("ID2", "Second Employee", 2000));
		put(new Employee("ID3", "Third Employee", 3000));
	}

	/**
	 * Assumes that {@link Employee#getEmployeeId()} does not return null.
	 * 
	 * @param employee
	 *            {@link Employee#getEmployeeId()} must not return null.
	 */
	private void put(Employee employee) {
		employees.put(employee.getEmployeeId(), employee);
	}

	@Override
	public List<Employee> findAll() {
		return new ArrayList<>(employees.values());
	}

	@Override
	public Optional<Employee> findOne(String id) {
		return Optional.ofNullable(employees.get(id));
	}

	/**
	 * If the passed employee has no id, then it is generated automatically.
	 * 
	 * @param employee
	 * @return the saved employee
	 */
	public Employee save(Employee employee) {
		if (employee.getEmployeeId() == null) {
			// dumb way of generating an automatic ID
			employee.setEmployeeId("ID" + (employees.size() + 1));
		}
		// Map.put adds a new element or replace an existing one
		// with the given key
		put(employee);
		return employee;
	}

	@Override
	public Employee deleteById(String id) {
		return employees.remove(id);
	}
}