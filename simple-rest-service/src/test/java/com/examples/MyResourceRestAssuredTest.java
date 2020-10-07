package com.examples;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import javax.ws.rs.core.MediaType;

public class MyResourceRestAssuredTest {
	
	private HttpServer server;
	
	@Before
	public void setUp() throws Exception {
		server = Main.startServer();
	}
	
	@BeforeClass
	public static void configureRestAssured() {
		RestAssured.baseURI = Main.BASE_URI;
	}
	
	@After
	public void tearDown() throws Exception {
		server.shutdown();
	}
	
	@Test
	public void testGetItText() {
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
	public void testGetItXML() {
		given().
			accept(MediaType.TEXT_XML).
		when().
			get("myresource").
		then().
			statusCode(200).
			assertThat().
				contentType(MediaType.TEXT_XML).
				and().
				body("hello", equalTo("Got it (XML)!"));
	}

	@Test
	public void testGetItHTML() {
		given().
			accept(MediaType.TEXT_HTML).
		when().
			get("myresource").
		then().
			statusCode(200).
			assertThat().
				contentType(MediaType.TEXT_HTML).
				and().
				body("html.head.title", equalTo("Hello Jersey")).
				body("html.body", equalTo("Got it (HTML)!"));
	}
	


}
