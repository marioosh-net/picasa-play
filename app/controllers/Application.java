package controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import model.Album;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.AlbumFeed;
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
	
	public static Result auth() {
		myService = new PicasawebService("testApp");
		String requestUrl =
			    AuthSubUtil.getRequestUrl("http://localhost:9000/token",
			                        "https://picasaweb.google.com/data/",
			                        false,
			                        true);
		
		Logger.info(requestUrl);
		return redirect(requestUrl);
	}
	
	public static Result token(String sessionToken) {
		Logger.info("TOKEN:" + sessionToken);
		myService.setAuthSubToken(sessionToken, null);
		return ok(token.render(sessionToken));
	}
	
	public static Result albums() throws IOException, ServiceException {
		Logger.info(myService+"");
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default?kind=album");
		UserFeed myUserFeed = myService.getFeed(feedUrl, UserFeed.class);
		List<Album> l = new ArrayList<Album>();
		for(AlbumEntry a: myUserFeed.getAlbumEntries()) {
			String id = a.getId().substring(a.getId().lastIndexOf('/')+1);
			l.add(new Album(id, a.getTitle().getPlainText(), a.getPhotosUsed()));
		}
		return ok(albums.render(l));

		/*
		Logger.info(Play.application().path()+"");
		Logger.info(myService+"");
		if(name == null) {
			return ok("Hello World");
		}
		return ok("Hello "+name);
		*/
	}

	public static Result photos(String albumId) throws IOException, ServiceException {		
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"?kind=photo&thumbsize=72c&imgsize=800");
		AlbumFeed feed = myService.getFeed(feedUrl, AlbumFeed.class);			
		return ok(photos.render(feed.getPhotoEntries()));
	}
}
