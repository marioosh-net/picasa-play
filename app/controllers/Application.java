package controllers;

import static play.Logger.info;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import model.Album;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.albums;
import views.html.photos;
import views.html.token;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class Application extends Controller {
	
	static final String THUMB_SIZE = "104c,72c,800";
	static final String IMG_SIZE = "1600";//"d";

	static private List<PicasawebService> myServices = new ArrayList<PicasawebService>();
	static private PicasawebService myService;
	
	static {
		try {
			String[][] accounts = new String[][]{
					{"waclaw.bezimienny@gmail.com", "b7FEvW4mSy9C"},
					{"waclaw.bezimienny2@gmail.com", "b7FEvW4mSy9C"}
					};
			
			for(String[] account: accounts) {
				PicasawebService myService = new PicasawebService("testApp");			
				myService.setUserCredentials(account[0], account[1]);
				myServices.add(myService);
			}
			
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}		
	}
	
	public static Result auth() {
		myService = new PicasawebService("testApp");
		String requestUrl = AuthSubUtil.getRequestUrl("http://localhost:9000/token", "https://picasaweb.google.com/data/", false, true);
		info(requestUrl);
		return redirect(requestUrl);
	}
	
	public static Result token(String sessionToken) {
		info("TOKEN:" + sessionToken);
		myService.setAuthSubToken(sessionToken, null);
		return ok(token.render(sessionToken));
	}
	
	public static Result albums() throws IOException, ServiceException {
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default?kind=album&thumbsize="+THUMB_SIZE);
		
		List<Album> l = new ArrayList<Album>();		
		int i = 0;
		for(PicasawebService myService: myServices) {
			UserFeed myUserFeed = myService.getFeed(feedUrl, UserFeed.class);
			for(AlbumEntry a: myUserFeed.getAlbumEntries()) {
				String id = a.getId().substring(a.getId().lastIndexOf('/')+1);
				l.add(new Album(id, a.getTitle().getPlainText(), a.getMediaThumbnails().get(0).getUrl(), a.getPhotosUsed(), i));
			}
		i++;
		}
		Collections.sort(l, new Comparator<Album>() {
			@Override
			public int compare(Album o1, Album o2) {
				return o1.getTitle().compareTo(o2.getTitle());
			}});
		return ok(albums.render(l));
	}

	public static Result photos(int serviceIndex, String albumId) throws IOException, ServiceException {
		myService = myServices.get(serviceIndex);
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"?kind=photo&thumbsize="+THUMB_SIZE+"&imgmax="+IMG_SIZE);
		AlbumFeed feed = myService.getFeed(feedUrl, AlbumFeed.class);
		return ok(photos.render(feed));
	}
}
