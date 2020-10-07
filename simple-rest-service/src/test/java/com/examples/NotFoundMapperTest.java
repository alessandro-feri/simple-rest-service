package com.examples;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;

public class NotFoundMapperTest extends JerseyTest {
		

	/**
	 * A mock REST resource with a GET end-point we stub for throwing a
	 * {@link NotFoundException}.
	 */
	@Path("testpath")
	public static class MockResource {
		@GET
		@Path("notfound")
		public String testEndPoint() {
			throw new NotFoundException("an error message");
		}
	}

	@Override
	protected Application configure() {
		// just register the MockResource and the
		// mapper we want to test
		return new ResourceConfig()
			.register(NotFoundMapper.class)
			.register(MockResource.class);
	}

	@Before
	public void configureRestAssured() {
		// retrieve the base URI of the JerseyTest server
		RestAssured.baseURI = getBaseUri().toString();
	}

	@Test
	public void testNotFoundResponse() {
		when().
			get("testpath/notfound").
		then().
			statusCode(404).
			contentType(MediaType.TEXT_PLAIN).
			body(equalTo("an error message"));
	}
}
