package com.examples;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MyResourceTest {

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and Main.startServer())
        // --
        // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

        target = c.target(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdownNow();
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetIt() {
        String responseMsg = target.path("myresource").request(MediaType.TEXT_PLAIN).get(String.class);
        assertEquals("Got it!", responseMsg);
    }
    
    @Test
    public void testGetItXML() {
    	String responseMsg =target.path("myresource").request(MediaType.TEXT_XML).get(String.class);
    	assertEquals("<?xml version=\"1.0\"?>\n" +
    			"<hello>Got it (XML)!</hello>\n" + 
    			"", responseMsg);
    }
    @Test
    public void testGetItHTML() {
    	String responseMsg = target.path("myresource").request(MediaType.TEXT_HTML).get(String.class);
    	assertEquals("<html>\n" + 
    			"<head>\n" +
    			"<title>Hello Jersey</title>\n" +
    			"</head>\n" +
    			"<body>Got it (HTML)!</body>\n" +
    			"</html>\n" +
    			"", responseMsg);

    }
    
}
