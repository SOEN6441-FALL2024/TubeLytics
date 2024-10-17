package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

/**
 * This controller contains an action to handle HTTP requests to the application's home page.
 *
 * @author Deniz Dinchdonmez
 */
public class HomeController extends Controller {

  // This method renders the index page
  public Result index() {
    return ok(index.render());
  }
}
