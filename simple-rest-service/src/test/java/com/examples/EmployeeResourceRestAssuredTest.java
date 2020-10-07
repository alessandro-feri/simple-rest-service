package com.examples;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.examples.model.Employee;
import com.examples.service.EmployeeService;

import io.restassured.RestAssured;

public class EmployeeResourceRestAssuredTest extends JerseyTest {

	private static final String EMPLOYEES = "employees";

	@Mock
	private EmployeeService employeeService;

	@Override
	protected Application configure() {
		MockitoAnnotations.initMocks(this);
		// register only the EmployeeResource
		return new ResourceConfig(EmployeeResource.class)
			// inject the mock in our EmployeeResource
			.register(new AbstractBinder() {
				@Override
				protected void configure() {
					// differently from Guice,
					// bind(concrete).to(abstract)
					bind(employeeService)
						.to(EmployeeService.class);
				}
			});
	}

	@Before
	public void configureRestAssured() {
		// retrieve the base URI of the JerseyTest server
		RestAssured.baseURI = getBaseUri().toString();
	}

	@Test
	public void justForDemoCannotAccessAlsoMyResource() {
		given().
			accept(MediaType.TEXT_PLAIN).
		when().
			get("myresource").
		then().
			statusCode(404);
	}

	@Test
	public void testGetAllEmployees() {
		when(employeeService.allEmployees())
			.thenReturn(asList(
				new Employee("ID1", "First Employee", 1000),
				new Employee("ID2", "Second Employee", 2000)
			));

		given().
			accept(MediaType.APPLICATION_XML).
		when().
			get(EMPLOYEES).
		then().
			statusCode(200).
			assertThat().
			body(
			"employees.employee[0].id", equalTo("ID1"),
			"employees.employee[0].name", equalTo("First Employee"),
			"employees.employee[0].salary", equalTo("1000"),
			"employees.employee[1].id", equalTo("ID2"),
			"employees.employee[1].name", equalTo("Second Employee"),
			"employees.employee[1].salary", equalTo("2000")
			);
	}

	@Test
	public void testGetAllEmployeesWithRootPaths() {
		when(employeeService.allEmployees())
			.thenReturn(asList(
				new Employee("ID1", "First Employee", 1000),
				new Employee("ID2", "Second Employee", 2000)
			));

		// a variation of the above test showing how to
		// test several XML elements
		given().
			accept(MediaType.APPLICATION_XML).
		when().
			get(EMPLOYEES).
		then().
			statusCode(200).
			assertThat().
				root("employees.employee[0]").
				body(
					"id", equalTo("ID1"),
					"name", equalTo("First Employee"),
					"salary", equalTo("1000")
				).
				root("employees.employee[1]").
				body(
					"id", equalTo("ID2")
					// similar assertions for the other fields
				);
	}

	@Test
	public void testGetOneEmployee() {
		when(employeeService.getEmployeeById(anyString()))
			.thenReturn(
				new Employee("ID1", "An Employee", 2000));

		given().
			accept(MediaType.APPLICATION_XML).
		when().
			get(EMPLOYEES + "/ID1").
		then().
			statusCode(200).
			assertThat().
			body(
				"employee.id", equalTo("ID1"),
				"employee.name", equalTo("An Employee"),
				"employee.salary", equalTo("2000")
			);
	}

	@Test
	public void testGetAllEmployeesJSON() {
		when(employeeService.allEmployees())
			.thenReturn(asList(
				new Employee("ID1", "First Employee", 1000),
				new Employee("ID2", "Second Employee", 2000)
			));

		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(EMPLOYEES).
		then().
			statusCode(200).
			assertThat().
			body(
				"id[0]", equalTo("ID1"),
				"name[0]", equalTo("First Employee"),
				"salary[0]", equalTo(1000),
				// NOTE: "salary" retains its integer type in JSON
				// so it must be equal to 1000 NOT "1000"
				"id[1]", equalTo("ID2"),
				"name[1]", equalTo("Second Employee")
				// other checks omitted
			);
	}

	@Test
	public void testGetOneEmployeeJSON() {
		when(employeeService.getEmployeeById(anyString()))
			.thenReturn(
				new Employee("ID1", "An Employee", 2000));

		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(EMPLOYEES + "/ID1").
		then().
			statusCode(200).
			assertThat().
			body(
				"id", equalTo("ID1"),
				"name", equalTo("An Employee"),
				"salary", equalTo(2000)
				// NOTE: "salary" retains its integer type in JSON
				// so it must be equal to 2000 NOT "2000"
			);
	}

	@Test
	public void testCount() {
		List<Employee> employees = asList(new Employee(), new Employee());
		when(employeeService.allEmployees())
			.thenReturn(employees);

		when().
			get(EMPLOYEES + "/count").
		then().
			statusCode(200).
			assertThat().
			body(equalTo("" + employees.size()));
	}

	@Test
	public void testPostNewEmployee() {
		// values for the new Employee in the request body
		JsonObject newObject = Json.createObjectBuilder()
				.add("name", "passed name")
				.add("salary", 1000)
				.build();

		// when we pass an Employee with the values of the request body
		when(employeeService.addEmployee(new Employee(null, "passed name", 1000)))
			.thenReturn(new Employee("ID", "returned name", 2000));
			// the service returns a new Employee object
			// possibly with different values
			// but for sure with a generated id

		given().
			contentType(MediaType.APPLICATION_JSON).
			body(newObject.toString()).
		when().
			post(EMPLOYEES).
		then().
			statusCode(201).
			assertThat().
			// make sure we return the Employee returned by the service
			body(
				"id", equalTo("ID"),
				"name", equalTo("returned name"),
				"salary", equalTo(2000)
			).
			header("Location",
				response -> endsWith(EMPLOYEES + "/ID"));
	}

	@Test
	public void testPutEmployee() {
		// values for the new Employee in the request body
		// the id is not present
		JsonObject newObject = Json.createObjectBuilder()
				.add("name", "passed name")
				.add("salary", 1000)
				.build();

		// when we pass an Employee with the values of the request body
		// where we must pass the employee id of the URI
		when(employeeService.replaceEmployeeById("ID", new Employee(null, "passed name", 1000)))
			.thenReturn(new Employee("ID", "returned name", 2000));
			// the service returns a new Employee object
			// possibly with different values

		given().
			contentType(MediaType.APPLICATION_JSON).
			body(newObject.toString()).
		when().
			put(EMPLOYEES + "/ID").
		then().
			statusCode(200).
			assertThat().
			// make sure we return the Employee returned by the service
			body(
				"id", equalTo("ID"),
				"name", equalTo("returned name"),
				"salary", equalTo(2000)
			);
	}

	@Test
	public void testDeleteEmployee() {
		when(employeeService.deleteEmployeeById("ID"))
			.thenReturn(new Employee("ID", "employee", 1000))
			.thenReturn(null);

		when().
			delete(EMPLOYEES + "/ID").
		then().
			statusCode(202).
			assertThat().
			body(
				"id", equalTo("ID"),
				"name", equalTo("employee"),
				"salary", equalTo(1000)
			);

		// idempotence
		when().
			delete(EMPLOYEES + "/ID").
		then().
			statusCode(202).
			assertThat().
			body(Matchers.isEmptyString());
	}
}