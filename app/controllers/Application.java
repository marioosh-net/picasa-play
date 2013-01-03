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
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.media.mediarss.MediaGroup;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.ExifTags;
import com.google.gdata.data.photos.GphotoAlbumId;
import com.google.gdata.data.photos.GphotoEntry;
import com.google.gdata.data.photos.GphotoId;
import com.google.gdata.data.photos.GphotoPhotosUsed;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.TagEntry;
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
	
	/**
	 * album list
	 * @return
	 * @throws MalformedURLException
	 */
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
						
						if(session("user") != null || e.getTitle().getPlainText().endsWith("\u00A0")) {
							String t = e.getTitle().getPlainText();
							l.add(new Album(e.getGphotoId(), t, e.getExtension(MediaGroup.class).getThumbnails().get(0).getUrl(), e.getExtension(GphotoPhotosUsed.class).getValue(), i));
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
	
	/**
	 * photos in album list
	 * @param serviceIndex
	 * @param albumId
	 * @param start
	 * @param max
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static Result photos(int serviceIndex, String albumId, int start, int max) throws IOException, ServiceException {
		info("Getting photos list...");
		myService = myServices.get(serviceIndex);
		session("si", serviceIndex+"");
		session("ai", albumId+"");
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"?kind=photo,tag"+"&thumbsize="+THUMB_SIZE+
				"&fields=id,title,entry(title,id,gphoto:id,gphoto:albumid,gphoto:numphotos,media:group/media:thumbnail,media:group/media:content,media:group/media:keywords,exif:tags)");
		debug(feedUrl.toString());
		Query photosQuery = new Query(feedUrl);
		
		// AlbumFeed feed = myService.getFeed(feedUrl, AlbumFeed.class);		
		AlbumFeed feed = myService.query(photosQuery, AlbumFeed.class);
		if(feed.getTitle().getPlainText().endsWith("\u00A0")) {
			session("pub", "1");
		} else {
			session().remove("pub");
		}
		
		String t = feed.getTitle().getPlainText();
		session("aname", t);
		
		// describe(feed.getEntries().get(0));
		List<Photo> lp = new ArrayList<Photo>();
		for(GphotoEntry<PhotoEntry> e: feed.getEntries()) {
			// Utils.describe(e);
			// debug("EXTENSIONS:" + e.getExtensions()+"");
			MediaGroup g = e.getExtension(MediaGroup.class);
			ExifTags exif = e.getExtension(ExifTags.class);
			//Utils.describe(exif);
			
			if(g != null) {
				/*
				debug(g.getContents().size()+"");
				debug(g.getThumbnails().size()+"");
				debug("thumbs:"+g.getThumbnails().get(0).getUrl());
				debug("thumbs:"+g.getThumbnails().get(1).getUrl());
				debug("thumbs:"+g.getThumbnails().get(2).getUrl());
				debug("orig:"+g.getContents().get(0).getUrl());
				*/
				boolean pub = g.getKeywords().getKeywords().contains("public");
				if(session("user") != null || pub) {
					lp.add(new Photo(e.getTitle().getPlainText(), 
							e.getExtension(GphotoId.class).getValue(), 
						Arrays.asList(new String[]{g.getThumbnails().get(0).getUrl(), 
								g.getThumbnails().get(1).getUrl(), 
								g.getThumbnails().get(2).getUrl()}), 
						g.getContents().get(0).getUrl(), 
						e.getExtension(GphotoAlbumId.class).getValue(), 
						g.getKeywords().getKeywords().toArray(new String[]{}), pub, exif));
				}
			}
		}
		// debug("TITLE:"+feed.getTitle()+"");
		// return ok(photos.render(feed, (List<GphotoEntry<PhotoEntry>>)feed.getEntries<PhotoEntry>(), l));
		return ok(photos.render(feed, lp, null));
	}
	
	/**
	 * make photo public
	 * @param serviceIndex
	 * @param albumId
	 * @param photoId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static Result pub(int serviceIndex, String albumId, String photoId) throws IOException, ServiceException {
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"/photoid/"+photoId);
		TagEntry myTag = new TagEntry(); 
		myTag.setTitle(new PlainTextConstruct("public"));
		myServices.get(serviceIndex).insert(feedUrl, myTag);
		return ok("1");
	}

	/**
	 * make photo private
	 * @param serviceIndex
	 * @param albumId
	 * @param photoId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static Result priv(int serviceIndex, String albumId, String photoId) throws IOException, ServiceException {
		URL entryUrl = new URL("https://picasaweb.google.com/data/entry/api/user/default/albumid/"+albumId+"/photoid/"+photoId+"/tag/public");
		TagEntry te = myServices.get(serviceIndex).getEntry(entryUrl, TagEntry.class);
		te.delete();
		return ok("0");
		
		/*
		URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"/photoid/"+photoId+"?kind=tag&tag=public");
		debug(feedUrl+"");
		Query photosQuery = new Query(feedUrl);
		AlbumFeed searchResultsFeed = myServices.get(serviceIndex).query(photosQuery, AlbumFeed.class);
		for (TagEntry tag : searchResultsFeed.getTagEntries()) {
			if(tag.getTitle().getPlainText().equals("public")) {
				tag.delete();
				break;
			}
		}
		return ok("0");
		*/		
				
		/*
		TagEntry myTag = myServices.get(serviceIndex).getEntry(new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId+"/photoid/"+photoId+"/tag/public"), TagEntry.class);
		myTag.delete();
		// myServices.get(serviceIndex).insert(feedUrl, myTag);
		return ok("0");
		*/
	}

	/**
	 * make album public
	 * @param serviceIndex
	 * @param albumId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static Result pubAlbum(int serviceIndex, String albumId) throws IOException, ServiceException {
		URL feedUrl = new URL("https://picasaweb.google.com/data/entry/api/user/default/albumid/"+albumId);
		debug(feedUrl+"");
		AlbumEntry ae = myServices.get(serviceIndex).getEntry(feedUrl, AlbumEntry.class);
		ae.setTitle(new PlainTextConstruct(ae.getTitle().getPlainText()+"\u00A0"));
		ae.update();
		return ok("1");
	}
	
	/**
	 * make album private
	 * @param serviceIndex
	 * @param albumId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static Result privAlbum(int serviceIndex, String albumId) throws IOException, ServiceException {
		URL feedUrl = new URL("https://picasaweb.google.com/data/entry/api/user/default/albumid/"+albumId);
		debug(feedUrl+"");
		AlbumEntry ae = myServices.get(serviceIndex).getEntry(feedUrl, AlbumEntry.class);
		ae.setTitle(new PlainTextConstruct(ae.getTitle().getPlainText().replaceAll("\u00A0", "").replaceAll("\\+", "")));
		ae.update();
		return ok("0");
	}

}
