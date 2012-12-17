package controllers;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.util.AuthenticationException;
import play.*;
import play.mvc.*;
import views.html.*;

public class Application extends Controller {

	static private PicasawebService myService;
	static {
		try {
			myService = new PicasawebService("testApp");			
			myService.setUserCredentials("waclaw.bezimienny@gmail.com", "b7FEvW4mSy9C");
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}		
	}
	
	public static Result index(String name) {

		Logger.info(Play.application().path()+"");
		Logger.info(myService+"");
		if(name == null) {
			return ok("Hello World");
		}
		return ok("Hello "+name);
	}

}
