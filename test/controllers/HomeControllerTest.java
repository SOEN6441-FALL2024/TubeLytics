package controllers;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

/** Unit test for HomeController Author: Deniz Dinchdonmez, Aidassj */
public class HomeControllerTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        // Building the application using Guice
        return new GuiceApplicationBuilder().build();
    }

    @Test
    public void testIndex() {
        // Creating a request to the root URL ("/")
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET).uri("/");

        // Routing the request and getting the result
        Result result = route(app, request);

        // Asserting that the response status is OK (200)
        assertEquals(OK, result.status());
    }

    @Test
    public void testSearchWithValidInput() {
        // Creating a request to the root URL ("/")
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET).uri("/?query=book");

        // Routing the request and getting the result
        Result result = route(app, request);

        // Asserting that the response status is OK (200)
        assertEquals(OK, result.status());
    }

    @Test
    public void testSearchEmptyInput() {
        // Creating a request to the root URL ("/")
        Http.RequestBuilder request = new Http.RequestBuilder().method(GET).uri("/?query=");

        // Routing the request and getting the result
        Result result = route(app, request);

        // Asserting that the response status is OK (200)
        assertEquals(OK, result.status());
    }

    @Test
    public void testSearchWithEmptyInput() {
        // Creating a request to the search URL with empty input
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(POST)
                .uri("/search")
                .bodyForm(new java.util.HashMap<String, String>() {{
                    put("searchTerm", "");
                }});

        // Routing the request and getting the result
        Result result = route(app, request);

        // Asserting that the response status is BAD_REQUEST (400) for empty input
        assertEquals(BAD_REQUEST, result.status());
        // Additional check to see if the content includes error message
    }
}