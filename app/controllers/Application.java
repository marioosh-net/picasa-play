package controllers;

import static play.Logger.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.beanutils.BeanMap;
import model.Album;
import model.Photo;
import play.mvc.Controller;
import play.mvc.Result;
import scala.actors.threadpool.Arrays;
import views.html.albums;
import views.html.photos;
import views.html.token;
import com.google.gdata.client.Query;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.media.mediarss.MediaGroup;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.GphotoAlbumId;
import com.google.gdata.data.photos.GphotoEntry;
import com.google.gdata.data.photos.GphotoPhotosUsed;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class Application extends Controller {
	
	static final String THUMB_SIZE = "104c,72c,800";
	static final String IMG_SIZE = "1600";//"d";

	static private List<PicasawebService> myServices = new ArrayList<PicasawebService>();
	static private PicasawebService myService;
	static private List<Album> l;
	
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
		
		l = new ArrayList<Album>();		
		int i = 0;
		for(PicasawebService s: myServices) {
			UserFeed feed = s.getFeed(feedUrl, UserFeed.class);
			for(AlbumEntry a: feed.getAlbumEntries()) {
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
		
		albumsPartial();
		return ok(albums.render(l));
	}
	
	public static Result albumsPartial() throws IOException, ServiceException {
		info("Getting albums list...");
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default?kind=album&thumbsize="+THUMB_SIZE+"&fields=entry(title,id,gphoto:id,gphoto:numphotos,media:group/media:thumbnail)");
		Query albumQuery = new Query(feedUrl);
		
		l = new ArrayList<Album>();		
		int i = 0;
		for(PicasawebService s: myServices) {
			try {
				UserFeed feed = s.query(albumQuery, UserFeed.class);
				for (GphotoEntry e : feed.getEntries()) {
					// describe(e);
					l.add(new Album(e.getGphotoId(), e.getTitle().getPlainText(), e.getExtension(MediaGroup.class).getThumbnails().get(0).getUrl(), e.getExtension(GphotoPhotosUsed.class).getValue(), i));
				}
			} catch (Exception e) {
				e.printStackTrace();
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
	
	public static Result photos(int serviceIndex, String albumId, int start, int max) throws IOException, ServiceException {
		info("Getting photos list...");
		myService = myServices.get(serviceIndex);
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"?kind=photo,tag&thumbsize="+THUMB_SIZE+"&imgmax="+IMG_SIZE+"&fields=title,entry(title,id,gphoto:id,gphoto:albumid,gphoto:numphotos,media:group/media:content,media:group/media:thumbnail)"+(max!= 0 ? "&max-results="+max: "")+(start!= 0 ? "&start-index="+start: ""));
		Query photosQuery = new Query(feedUrl);
		
		// AlbumFeed feed = myService.getFeed(feedUrl, AlbumFeed.class);		
		AlbumFeed feed = myService.query(photosQuery, AlbumFeed.class);
		// describe(feed.getEntries().get(0));
		if(l == null) {
			albumsPartial();
		}
		List<Photo> lp = new ArrayList<Photo>();
		for(GphotoEntry<PhotoEntry> e: feed.getEntries()) {
			describe(e);
			info("CLASS:"+e);
			debug("EXTENSIONS:" + e.getExtensions()+"");
			MediaGroup g = e.getExtension(MediaGroup.class);
			if(g != null) {
				debug(g.getContents().size()+"");
				debug(g.getThumbnails().size()+"");
				debug("thumbs:"+g.getThumbnails().get(0).getUrl());
				debug("thumbs:"+g.getThumbnails().get(1).getUrl());
				debug("thumbs:"+g.getThumbnails().get(2).getUrl());
				debug("orig:"+g.getContents().get(0).getUrl());
				lp.add(new Photo(e.getTitle().getPlainText(), e.getId(), Arrays.asList(new String[]{g.getThumbnails().get(0).getUrl(), g.getThumbnails().get(1).getUrl(), g.getThumbnails().get(2).getUrl()}), g.getContents().get(0).getUrl(), e.getExtension(GphotoAlbumId.class).getValue()));
			}
		}
		debug("TITLE:"+feed.getTitle()+"");
		// return ok(photos.render(feed, (List<GphotoEntry<PhotoEntry>>)feed.getEntries<PhotoEntry>(), l));
		return ok(photos.render(feed, lp, l));
	}
	
	private static void describe(Object o) {
		debug("DESCRIBE "+o+" --------------- START");
		BeanMap m = new BeanMap(o);
		for(Object k: m.keySet()) {
			String key = (String) k;
			try {
				debug(key+ " = " + m.get(key)+"");
			} catch (Exception e) {
				warn(key + " retrieving error");
			}
		}
		debug("DESCRIBE "+o+" ---------------- END");
	}
}
