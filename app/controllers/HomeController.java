package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index; // Make sure to import the index view

public class HomeController extends Controller {

  // This method renders the index page
  public Result index() {
    return ok(index.render()); // No need to pass request header anymore
  }
}
