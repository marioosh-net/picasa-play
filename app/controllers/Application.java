package controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
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
	
	public static Result index(String name) throws IOException, ServiceException {
		Logger.info(myService+"");
		Logger.info(name);
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default?kind=album");
		UserFeed myUserFeed = myService.getFeed(feedUrl, UserFeed.class);
		return ok(albums.render(myUserFeed.getAlbumEntries()));

		/*
		Logger.info(Play.application().path()+"");
		Logger.info(myService+"");
		if(name == null) {
			return ok("Hello World");
		}
		return ok("Hello "+name);
		*/
	}

}
