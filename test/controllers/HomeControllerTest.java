package controllers;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.GET;
import static play.test.Helpers.route;

import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

/**
 * Unit test for HomeController
 * Author: Deniz Dinchdonmez, Aidassj
 */
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
}
