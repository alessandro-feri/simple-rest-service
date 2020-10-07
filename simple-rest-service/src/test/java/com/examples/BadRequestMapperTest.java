package com.examples;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import static org.hamcrest.Matchers.equalTo;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;

import static io.restassured.RestAssured.when;


public class BadRequestMapperTest extends JerseyTest {
	
	@Path("testpath")
	public static class MockResource {
		@GET
		@Path("badrequest")
		public String testEndPoint() {
			throw new BadRequestException("an error message");
		}
	}
	
	@Override
	protected Application configure() {
		// just register the MockResource and the
		// mapper we want to test
		return new ResourceConfig()
				.register(BadRequestMapper.class)
				.register(MockResource.class);
	}
	
	@Before
	public void configureRestAssured() {
		// retrieve the base URI of the JerseyTest server
		RestAssured.baseURI = getBaseUri().toString();
	}
	
	@Test
	public void testBadRequestResponse() {
		when().
				get("testpath/badrequest").
		then().
				statusCode(400).
				contentType(MediaType.TEXT_PLAIN).
				body(equalTo("an error message"));
	}

}
