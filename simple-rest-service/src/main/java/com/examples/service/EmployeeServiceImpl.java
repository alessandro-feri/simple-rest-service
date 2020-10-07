package com.examples.service;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import com.examples.model.Employee;
import com.examples.repository.EmployeeRepository;

public class EmployeeServiceImpl implements EmployeeService {

	private EmployeeRepository employeeRepository;

	@Inject
	public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@Override
	public synchronized List<Employee> allEmployees() {
		return employeeRepository.findAll();
	}

	@Override
	public synchronized Employee getEmployeeById(String id) {
		return employeeRepository
			.findOne(id)
			.orElseThrow(
				() -> new NotFoundException("Employee not found with id " + id));
	}

	@Override
	public synchronized Employee addEmployee(Employee employee) {
		sanityChecks(employee);
		return employeeRepository.save(employee);
	}

	@Override
	public synchronized Employee replaceEmployeeById(String id, Employee employee) {
		sanityChecks(employee);
		if (!employeeRepository.findOne(id).isPresent())
			throw new NotFoundException("Employee not found with id " + id);
		employee.setEmployeeId(id);
		return employeeRepository.save(employee);
	}

	private void sanityChecks(Employee employee) {
		if (employee == null) {
			throw new BadRequestException("Missing values for Employee");
		}
		if (employee.getEmployeeId() != null) {
			throw new BadRequestException("Unexpected id specification for Employee");
		}
	}

	@Override
	public synchronized Employee deleteEmployeeById(String id) {
		return employeeRepository.deleteById(id);
	}

}