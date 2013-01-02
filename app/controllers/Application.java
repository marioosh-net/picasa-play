package controllers;

import static play.Logger.debug;
import static play.Logger.error;
import static play.Logger.info;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import model.Album;
import model.Photo;
import model.Utils;
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
import com.google.gdata.util.ServiceException;

public class Application extends Controller {
	
	static final String THUMB_SIZE = "104c,72c,800";
	static final String IMG_SIZE = "1600";//"d";

	static public List<PicasawebService> myServices = new ArrayList<PicasawebService>();
	static private PicasawebService myService;
	
	static {
		try {
			info("Loading services...");
			Properties p = new Properties();
			InputStream in =  Application.class.getResourceAsStream("/resources/accounts.properties");
			if(in != null) {
				p.load(in);
				in.close();
			
				Enumeration e = p.propertyNames();
				List<String[]> l = new ArrayList<String[]>();
				while(e.hasMoreElements()) {
					String k = (String) e.nextElement();
					l.add(new String[]{k+"", p.getProperty(k)});
					PicasawebService myService = new PicasawebService("testApp");			
					myService.setUserCredentials(k+"", p.getProperty(k));
					myServices.add(myService);
				}
			
			} else {
				error("null inputstream");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static Result logout() throws MalformedURLException {
		session().clear();
		return ok(albums.render(getAlbums(), null));
	}
	
	public static Result login(String hash) throws MalformedURLException {
		if(hash.equals("password")) {
			session("user", "admin");
			return ok(albums.render(getAlbums(), null));
		}
		return ok(albums.render(getAlbums(), "login error"));
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
	
	public static Result albums(String message) throws IOException, ServiceException {
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default?kind=album&thumbsize="+THUMB_SIZE);
		
		List<Album> l = new ArrayList<Album>();		
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
		
		albumsPartial(null);
		return ok(albums.render(l, message));
	}
	
	public static Result albumsPartial(String message) throws IOException, ServiceException {
		debug("LOGGED: " + session("user"));
		return ok(albums.render(getAlbums(), message));
	}
	
	public static List<Album> getAlbums() throws MalformedURLException {
		info("Getting albums list...");
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default?kind=album,tag&thumbsize="+THUMB_SIZE+"&fields=entry(title,id,gphoto:id,gphoto:numphotos,media:group/media:thumbnail,media:group/media:keywords)");
		Query albumQuery = new Query(feedUrl);
		
		List<Album> l = new ArrayList<Album>();		
		int i = 0;
		for(PicasawebService s: myServices) {
			debug(feedUrl.toString());			
			try {
				UserFeed feed = s.query(albumQuery, UserFeed.class);
				for (GphotoEntry e : feed.getEntries()) {
					// Utils.describe(e);
					if(e.getGphotoId() != null) {
						
						if(session("user") != null || e.getTitle().getPlainText().endsWith("+")) {
							l.add(new Album(e.getGphotoId(), e.getTitle().getPlainText().replaceAll("\\+", ""), e.getExtension(MediaGroup.class).getThumbnails().get(0).getUrl(), e.getExtension(GphotoPhotosUsed.class).getValue(), i));
						}
					} else {
						// tag... (?kind=album,tag)
						debug("album TAG: "+e.getTitle().getPlainText());
					}
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
		return l;
	}
	
	public static Result photos(int serviceIndex, String albumId, int start, int max) throws IOException, ServiceException {
		info("Getting photos list...");
		myService = myServices.get(serviceIndex);
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"?kind=photo,tag"+"&thumbsize="+THUMB_SIZE+"&fields=id,title,entry(title,id,gphoto:id,gphoto:albumid,gphoto:numphotos,media:group/media:thumbnail,media:group/media:content,media:group/media:keywords)");
		debug(feedUrl.toString());
		Query photosQuery = new Query(feedUrl);
		
		// AlbumFeed feed = myService.getFeed(feedUrl, AlbumFeed.class);		
		AlbumFeed feed = myService.query(photosQuery, AlbumFeed.class);
		// describe(feed.getEntries().get(0));
		List<Photo> lp = new ArrayList<Photo>();
		for(GphotoEntry<PhotoEntry> e: feed.getEntries()) {
			// Utils.describe(e);
			// debug("EXTENSIONS:" + e.getExtensions()+"");
			MediaGroup g = e.getExtension(MediaGroup.class);
			if(g != null) {
				/*
				debug(g.getContents().size()+"");
				debug(g.getThumbnails().size()+"");
				debug("thumbs:"+g.getThumbnails().get(0).getUrl());
				debug("thumbs:"+g.getThumbnails().get(1).getUrl());
				debug("thumbs:"+g.getThumbnails().get(2).getUrl());
				debug("orig:"+g.getContents().get(0).getUrl());
				*/
				if(session("user") != null || g.getKeywords().getKeywords().contains("public")) {
					lp.add(new Photo(e.getTitle().getPlainText(), 
						e.getId(), 
						Arrays.asList(new String[]{g.getThumbnails().get(0).getUrl(), 
								g.getThumbnails().get(1).getUrl(), 
								g.getThumbnails().get(2).getUrl()}), 
						g.getContents().get(0).getUrl(), 
						e.getExtension(GphotoAlbumId.class).getValue(), 
						g.getKeywords().getKeywords().toArray(new String[]{})));
				}
			}
		}
		// debug("TITLE:"+feed.getTitle()+"");
		// return ok(photos.render(feed, (List<GphotoEntry<PhotoEntry>>)feed.getEntries<PhotoEntry>(), l));
		return ok(photos.render(feed, lp, getAlbums()));
	}
}
