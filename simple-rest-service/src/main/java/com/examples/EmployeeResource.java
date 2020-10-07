package com.examples;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.examples.model.Employee;
import com.examples.service.EmployeeService;

/**
 * Root resource (exposed at "employees" path)
 */
@Path("employees")
public class EmployeeResource {

	@Inject
	private EmployeeService employeeService;

	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Employee> getAllEmployees() {
		return employeeService.allEmployees();
	}

	@GET
	// Defines that the next path parameter after "employees is
	// treated as a parameter and passed to the EmployeeResource
	// Allows to type http://localhost:8080/myapp/employees/ID1
	// ID1 will be treated as parameter "id" and passed to this method
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Employee getOneEmployee(@PathParam("id") String id) {
		return employeeService.getEmployeeById(id);
	}

	// returns the number of employees
	// Use http://localhost:8080/myapp/employees/count
	// to get the total number of records
	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		return String.valueOf(employeeService.allEmployees().size());
	}

	/**
	 * Add a new Employee to the database.
	 * 
	 * @param employee
	 * @param uriInfo
	 * @return
	 * @throws URISyntaxException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addEmployee(Employee employee, @Context UriInfo uriInfo) throws URISyntaxException {
		Employee saved = employeeService.addEmployee(employee);
		return Response
			.created(new URI(
				uriInfo.getAbsolutePath() + "/" + saved.getEmployeeId()))
			.entity(saved)
			.build();
	}

	/**
	 * Replaces an existing Employee given its id, with the values of the passed
	 * Employee.
	 * 
	 * @param id
	 * @param employee
	 * @return
	 */
	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Employee replaceEmployee(@PathParam("id") String id, Employee employee) {
		return employeeService.replaceEmployeeById(id, employee);
	}

	/**
	 * Deletes the Employee with the given id.
	 * 
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteEmployee(@PathParam("id") String id) {
		return Response
			.accepted(employeeService.deleteEmployeeById(id))
			.build();
	}
}