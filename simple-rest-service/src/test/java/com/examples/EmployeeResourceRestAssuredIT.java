package com.examples;

import static io.restassured.RestAssured.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class EmployeeResourceRestAssuredIT {
	
	private static final String EMPLOYEES = "employees";
	private HttpServer server;

	@BeforeClass
	public static void configureRestAssured() {
		RestAssured.baseURI = Main.BASE_URI;
	}
	
	@Before
	public void setUp() throws Exception {
		server = Main.startServer();
	}
	
	
	@After
	public void tearDown() throws Exception {
		server.shutdownNow();
	}
	
	@Test
	public void testGetOneEmployeeWithNonExistingId() {
		given().
				accept(MediaType.APPLICATION_XML).
		when().
				get(EMPLOYEES + "/foo").
		then().
				statusCode(404). // Status code: Not Found
				contentType(MediaType.TEXT_PLAIN).
				body(equalTo("Employee not found with id foo"));
	}
	
	@Test
	public void testGetOneEmployeeWithNonExistingIdJSON() {
		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(EMPLOYEES + "/foo").
		then().
			statusCode(404). // Status code: Not Found
			contentType(MediaType.TEXT_PLAIN).
			body(equalTo("Employee not found with id foo"));
	}
	
	@Test
	public void justForDemoCanAccessAlsoMyResource() {
		given().
			accept(MediaType.TEXT_PLAIN).
		when().
			get("myresource").
		then().
			statusCode(200).
			assertThat().
				contentType(MediaType.TEXT_PLAIN).
				and().
				body(equalTo("Got it!"));
	}
	
	@Test
	public void testPostNewEmployee() {
		JsonObject newObject = Json.createObjectBuilder()
					.add("name", "test employee")
					.add("salary", 1000)
					.build();
		Response response = given().
					contentType(MediaType.APPLICATION_JSON).
					body(newObject.toString()).when().post(EMPLOYEES);
	
		String id = response.body().path("id");
		String uri = response.header("Location");
	
		assertThat(uri, endsWith(id));
	
		// read the saved employee with the returned URI
		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(uri).
		then().
			statusCode(200).
			assertThat().
			body(
					"id", equalTo(id),
					"name", equalTo("test employee"),
					"salary", equalTo(1000)
			);
		
	}
	
	@Test
	public void testPostNewEmployeeConcurrent() {
			JsonObject newObject = Json.createObjectBuilder()
						.add("name", "test employee")
						.add("salary", 1000)
						.build();
			Collection<String> ids = new ConcurrentLinkedQueue<>();
			
			List<Thread> threads = IntStream.range(0, 10)
					.mapToObj(i -> new Thread(() ->	
						{
							Response response =
									given().
										contentType(MediaType.APPLICATION_JSON).
										body(newObject.toString()).
									when().
										post(EMPLOYEES);
							ids.add(response.path("id"));
						}
					))
					.peek(t -> t.start())
					.collect(Collectors.toList());
	
			// wait for all the threads to finish
			await().atMost(10, SECONDS)
				.until(() -> threads.stream().noneMatch(t -> t.isAlive()));
			// if there are duplicated ids then we had a race condition
			assertThat(ids).doesNotHaveDuplicates();
	
	}
	
	@Test
	public void testPutReplaceEmployee() {
		// we want to replace Employee("ID1", "First Employee", 1000)
		// with new Employee("ID1", "modified employee", 2000)
		JsonObject newObject = Json.createObjectBuilder()
					.add("name", "modified employee")
					.add("salary", 2000)
					.build();
		given().
			contentType(MediaType.APPLICATION_JSON).
			body(newObject.toString()).
		when().
			put(EMPLOYEES + "/ID1").
		then().
			statusCode(200).
			assertThat().
			body(
					"id", equalTo("ID1"),
					"name", equalTo("modified employee"),
					"salary", equalTo(2000)
			);
		// read the replaced employee
		given().
				accept(MediaType.APPLICATION_JSON).
		when().
				get(EMPLOYEES + "/ID1").
		then().
				statusCode(200).
				assertThat().
				body(
						"id", equalTo("ID1"),
						"name", equalTo("modified employee"),
						"salary", equalTo(2000)
				);
	}

	
	
	
	@Test
	public void testGetAllEmployees() {
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
				"employees.employee[1].salary", equalTo("2000"),
				"employees.employee[2].id", equalTo("ID3"),
				"employees.employee[2].name", equalTo("Third Employee"),
				"employees.employee[2].salary", equalTo("3000")
				);
	}
	
	@Test
	public void testGetAllEmployeesWithRootPaths() {
		// a variation of the above test showing how to test several XML elements
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
			  				"id", equalTo("ID2"),
			  				"name", equalTo("Second Employee"),
			  				"salary", equalTo("2000")
			  		).
			  		root("employees.employee[2]").
			  		body(
			  				"id", equalTo("ID3"),
			  				"name", equalTo("Third Employee"),
			  				"salary", equalTo("3000")
			  		);
	
	}
	
	@Test
	public void testGetOneEmployee() {
		given().
			   accept(MediaType.APPLICATION_XML).
		when().
			   get(EMPLOYEES + "/ID2").
		then().
			   statusCode(200).
			   assertThat().
			   body(
					   "employee.id", equalTo("ID2"),
					   "employee.name", equalTo("Second Employee"),
					   "employee.salary", equalTo("2000")
			   );
	}
	
	
	@Test
	public void testGetAllEmployeesJSON() {
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
		given().
			accept(MediaType.APPLICATION_JSON).
		when().
			get(EMPLOYEES + "/ID2").
		then().
			statusCode(200).
			assertThat().
			body(
					"id", equalTo("ID2"),
					"name", equalTo("Second Employee"),
					"salary", equalTo(2000)
					// NOTE: "salary" retains its integer type in JSON
					// so it must be equal to 2000 NOT "2000"
			);
	}
	
	@Test
	public void testPutBadRequestWhenIdIsPartOfTheBody() {
		// we want to replace Employee("ID1", "First Employee", 1000)
		// but the id should not be part of the body
		JsonObject newObject = Json.createObjectBuilder()
					.add("id", "ID1")
					.add("name", "modified employee")
					.add("salary", 2000)
					.build();
		
		given().
			contentType(MediaType.APPLICATION_JSON).body(newObject.toString()).
		when().
			put(EMPLOYEES + "/ID1").
		then().
			statusCode(400). // Bad Request
			assertThat().
			contentType(MediaType.TEXT_PLAIN).
			body(equalTo("Unexpected id specification for Employee"));
			
	}
	
	
}
