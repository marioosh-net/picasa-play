package controllers;

import play.*;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {

	/*
	public static Result index() {
		return ok(index.render("Your new application is ready."));
	}
	*/

	public static Result index(String name) {
		if(name == null) {
			return ok("Hello World");
		}
		return ok("Hello "+name);
	}

}
