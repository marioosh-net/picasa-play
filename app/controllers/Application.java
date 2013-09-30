package controllers;

import interceptors.Geo;
import interceptors.Logged;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import model.Album;
import model.Photo;
import org.apache.commons.beanutils.BeanUtils;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import others.Role;
import others.Service;
import play.Logger;
import play.api.templates.Html;
import play.cache.Cache;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.albums;
import views.html.albumslist;
import views.html.auth;
import views.html.exif;
import views.html.main;
import views.html.photos;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.photos.Exif;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.tags.Tag;
import exceptions.NoAccountsException;

public class Application extends Controller {
	
	static final String CONFIG = "config.xml";
	static final String THUMB_SIZE = "104c,72c,800";
	static final String IMG_SIZE = "1600";//"d";
	static final String API_URL = "https://picasaweb.google.com/data/entry/api/user/default";
	static final String API_FEED_URL = "https://picasaweb.google.com/data/feed/api/user/default";
	static public final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	static final Map<String, Object[]> local = new HashMap<String, Object[]>();
	static public final Map<String, Object> settings = new HashMap<String, Object>();
	static public List<Service> myServices = new ArrayList<Service>();
	static private Service myService;

	static Token requestToken;
	static Token accessToken;
	
	/**
	 * load configuration from config.xml
	 * init picasa services
	 * 
	 * @throws NoAccountsException
	 */
	public static void loadServices() throws NoAccountsException {
		
		try {
			Logger.info("Loading services...");
			myServices.clear();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			InputStream in;			
			if(System.getProperty("config") != null && new File(System.getProperty("config")).canRead()) {
				in =  new FileInputStream(new File(System.getProperty("config")));
			} else {
				if(new File(".", CONFIG).canRead()) {
					in = new FileInputStream(new File(".", CONFIG));
				} else {
					in =  Application.class.getResourceAsStream("/resources/"+CONFIG);
				}
			}
			Document doc = builder.parse(in);
			in.close();
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			/**
			 * picasa accounts
			 */
			NodeList l = (NodeList) xpath.evaluate("//flickr/account", doc, XPathConstants.NODESET);
			for(int i=0; i < l.getLength(); i++) {
				Node account = l.item(i);
				String apiKey = account.getAttributes().getNamedItem("apiKey").getNodeValue();
				String secret = account.getAttributes().getNamedItem("secret").getNodeValue();
				String accessToken = account.getAttributes().getNamedItem("accessToken").getNodeValue();
				String accessSecret = account.getAttributes().getNamedItem("accessSecret").getNodeValue();
				Flickr myService = new Flickr(apiKey, secret, new REST());
                AuthInterface a = myService.getAuthInterface();
				Auth auth = a.checkToken(new Token(accessToken, accessSecret));
				myService.setAuth(auth);
				myServices.add(new Service(myService, auth));
			}
			
			/**
			 * local accounts
			 */
			l = (NodeList) xpath.evaluate("//settings/local/account", doc, XPathConstants.NODESET);
			for(int i=0; i < l.getLength(); i++) {
				Node account = l.item(i);
				String username = account.getAttributes().getNamedItem("login").getNodeValue();
				String password = account.getAttributes().getNamedItem("password").getNodeValue();
				Role role = Role.valueOf(account.getAttributes().getNamedItem("role").getNodeValue().toUpperCase());
				local.put(username, new Object[]{password, role});
			}

			/**
			 * settings
			 */
			Node n = (Node) xpath.evaluate("//settings/title", doc, XPathConstants.NODE);
			settings.put("title", n.getTextContent());
			
			in.close();
			
			Logger.info(settings.toString());
		
			if(myServices.size() == 0) {
				throw new NoAccountsException();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new NoAccountsException(e);
		}
	}
	
	/**
	 * logout
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 * @throws FlickrException 
	 */
	public static Result logout() throws IOException, FlickrException {
		session().clear();
		// Cache.set("albums", null);
		return ok(albums.render(getAlbums()));
	}
	
	/**
	 * login
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 * @throws FlickrException 
	 */
	public static Result login() throws IOException, FlickrException {
		
		String uuid = getUUID();
		final Map<String, String[]> values = request().body().asFormUrlEncoded();
	    final String pass = values.get("pass")[0];
	    final String login = values.get("login")[0];
	    Object[] o = local.get(login);
	    if(o != null && o[0].equals(pass)) {
	    	session("user", login);
	    	session("role", ((Role)o[1]).name());
	    	Cache.set(uuid+"albums", null);
	    	return redirect("/");
	    }
		flash("message", Messages.get("loginerror"));
		return ok(albums.render(getAlbums()));
	}
	
	/**
	 * main page (root) with album covers
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 * @throws NoAccountsException
	 */
	public static Result albums() throws IOException, NoAccountsException {
		if(request().queryString().get("lang") != null) {
			response().setCookie("lang", request().queryString().get("lang")[0]);
		}		
		try {
			return ok(albums.render(getAlbums()));
		} catch (FlickrException e) {
			Logger.error(e.getMessage(), e);
			loadServices();
			return redirect("/");
		} 
	}
	
	/**
	 * album list
	 * @return
	 * @throws ServiceException 
	 * @throws IOException 
	 * @throws FlickrException 
	 */
	@Geo
	public static List<Album> getAlbums() throws FlickrException {
				
		String uuid = getUUID();
		List<Album> cached = (List<Album>) Cache.get(uuid+"albums");
		if(cached != null) {
			Logger.debug("CACHED");
			return cached; 
		} else {
			Logger.debug("NOT CACHED");
			Logger.info("Getting albums list ("+new SimpleDateFormat("dd.MM.yyyy hh:ss:mm").format(new Date(System.currentTimeMillis()))+" | IP: "+request().remoteAddress()+")...");
			
			List<Album> l = new ArrayList<Album>();		
			int i = 0;
			for(Service se: myServices) {
				Flickr s = se.getFlickr();
				//RequestContext.getRequestContext().setAuth(s.getAuth()); // auth request
				PhotosetsInterface psi = s.getPhotosetsInterface();
				Photosets ps = psi.getList(se.getAuth().getUser().getId());
				for (Photoset e: ps.getPhotosets()) {
					l.add(new Album(e.getId(), e.getTitle(), e.getPrimaryPhoto().getSmallSquareUrl().replaceFirst("_s", "_q"), e.getPhotoCount(), i, true, myServices.get(i).getAuth().getUser().getId()));
				}
				i++;
			}
			Collections.sort(l, new Comparator<Album>() {
				@Override
				public int compare(Album o1, Album o2) {
					return o2.getTitle().compareTo(o1.getTitle());
				}});
			Cache.set(uuid+"albums", l, 3600);
			return l;
		}
	}

	/**
	 * full page with opened album (by url)
	 * @param serviceIndex
	 * @param albumId
	 * @param start
	 * @param max
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static Result direct(int serviceIndex, String albumId, int start, int max) throws IOException, FlickrException {
		return ok(main.render(albumId+"", albumslist.render(getAlbums(), albumId), photosHtml(serviceIndex, albumId, start, max)));
	}
	
	/**
	 * photos in album as Result
	 * @param serviceIndex
	 * @param albumId
	 * @param start
	 * @param max
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 * @throws FlickrException 
	 */
	public static Result photos(int serviceIndex, String albumId, int start, int max) throws IOException, FlickrException {
		return ok(photosHtml(serviceIndex, albumId, start, max));
	}
	
	/**
	 * photos in album as HTML
	 * @param serviceIndex
	 * @param albumId
	 * @param start
	 * @param max
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	private static Html photosHtml(int serviceIndex, String albumId, int start, int max) throws IOException, FlickrException {
		Logger.info("Getting photos list ("+new SimpleDateFormat("dd.MM.yyyy hh:ss").format(new Date(System.currentTimeMillis()))+" | IP: "+request().remoteAddress()+")...");
		myService = myServices.get(serviceIndex);
		session("si", serviceIndex+"");
		session("ai", albumId+"");
		
		PhotosetsInterface in = myService.getFlickr().getPhotosetsInterface();
		
		//RequestContext.getRequestContext().setAuth(myService.getAuth()); // auth request
		Photoset ps = in.getInfo(albumId);
		Logger.info("TITLE: "+ps.getTitle());
		
		// RequestContext.getRequestContext().setAuth(myService.getAuth()); // auth request to get private albums
		PhotoList<com.flickr4java.flickr.photos.Photo> pl = in.getPhotos(albumId, new HashSet<String>(){{add("tags,url_sq,url_t,date_taken");}}, Flickr.PRIVACY_LEVEL_PUBLIC, max, start);
		Logger.info("PHOTOS :"+ pl.size()+"");
		
		// AlbumFeed feed = myService.getFeed(feedUrl, AlbumFeed.class);		
		//AlbumFeed feed = myService.query(photosQuery, AlbumFeed.class);
		//if(feed.getTitle().getPlainText().endsWith("\u00A0")) {
			session("pub", "1");
		//} else {
//			session().remove("pub");
		//}
		
		String t = ps.getTitle();
		session("aname", t);
		Logger.debug("total:"+pl.getTotal());
		Logger.debug("perPage:"+pl.getPerPage());
		Logger.debug("start:"+pl.getPage());
		java.util.HashMap<String, Integer> map = new java.util.HashMap<String, Integer>();
		map.put("total",pl.getTotal());
		map.put("start",pl.getPage());
		map.put("per",pl.getPerPage());
		
		List<Integer> pages = new ArrayList<Integer>();
		for(int i = 1; i <= pl.getTotal()/pl.getPerPage() + (pl.getTotal()%pl.getPerPage() == 0 ? 0 : 1); i++) {
			pages.add(i);
		}
		
		List<Photo> lp = new ArrayList<Photo>();
		for(com.flickr4java.flickr.photos.Photo e: pl) {
			Logger.info(e.getTitle() + ": "+e.isPublicFlag());
			Collection<Exif> exif = null;//myService.getFlickr().getPhotosInterface().getExif(e.getId(), e.getSecret());
			Collection<Tag> tags1 = e.getTags();
			List<String> tags = new ArrayList<String>();
			for(Tag tag: tags1) {
				tags.add(tag.getValue());
			}
			
			//if(g != null) {
				boolean pub = e.isPublicFlag();
				if(session("user") != null || pub || true) {
					lp.add(new Photo(e.getTitle(), 
							e.getId(), 
						Arrays.asList(new String[]{e.getSmallSquareUrl().replaceFirst("_s", "_q"), e.getThumbnailUrl(), e.getLargeUrl()}), 
						e.getUrl(),
						albumId,
						tags.toArray(new String[]{}),
						pub, exif, e.getSecret()));
				}
			//}
		}
		Logger.info("SIZE: "+lp.size());
		
		return photos.render(ps, lp, null, map, pages);
	}
	
	/**
	 * make photo public
	 * @param serviceIndex
	 * @param albumId
	 * @param photoId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 * @throws FlickrException 
	 */
	@Logged(Role.ADMIN)
	public static Result pub(int serviceIndex, String albumId, String photoId) throws IOException, FlickrException {
		com.flickr4java.flickr.photos.Photo p = myServices.get(serviceIndex).getFlickr().getPhotosInterface().getPhoto(photoId);
		Collection<Tag> tags = p.getTags();
		Tag tag = null;
		for(Tag t: tags) {
			if(t.getValue().equals("public")) {
				tag = t;
				break;
			}
		}
		if(tag == null) {
			Tag t1 = new Tag();
			t1.setValue("public");
			tags.add(t1);
		}
		p.setTags(tags);
		
		/*
		URL feedUrl = new URL(API_FEED_URL+"/albumid/"+albumId+"/photoid/"+photoId);
		Logger.debug(feedUrl+"");
		TagEntry myTag = new TagEntry(); 
		myTag.setTitle(new PlainTextConstruct("public"));
		myServices.get(serviceIndex).insert(feedUrl, myTag);
		*/
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
	 * @throws FlickrException 
	 *
	@Logged(Role.ADMIN)
	public static Result priv(int serviceIndex, String albumId, String photoId) throws IOException, ServiceException, FlickrException {
		com.flickr4java.flickr.photos.Photo p = myServices.get(serviceIndex).getPhotosInterface().getPhoto(photoId);
		Collection<Tag> tags = p.getTags();
		Tag tag = null;
		for(Tag t: tags) {
			if(t.getValue().equals("public")) {
				tag = t;
				break;
			}
		}
		if(tag != null) {
			tags.remove(tag);
		}
		p.setTags(tags);
		
		/*
		URL entryUrl = new URL(API_URL+"/albumid/"+albumId+"/photoid/"+photoId+"/tag/public");
		TagEntry te = myServices.get(serviceIndex).getEntry(entryUrl, TagEntry.class);
		te.delete();
		*/
		//return ok("0");
	//}

	/**
	 * make album public
	 * @param serviceIndex
	 * @param albumId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 *
	@Logged(Role.ADMIN)
	public static Result pubAlbum(int serviceIndex, String albumId) throws IOException, ServiceException {
		
		PhotosetsInterface in = myServices.get(serviceIndex).getPhotosetsInterface();
		Photoset ps = in.getInfo(albumId);
		
		URL feedUrl = new URL(API_URL+"/albumid/"+albumId);
		Logger.debug(feedUrl+"");
		AlbumEntry ae = myServices.get(serviceIndex).getEntry(feedUrl, AlbumEntry.class);
		ae.setTitle(new PlainTextConstruct(ae.getTitle().getPlainText()+"\u00A0"));
		ae.update();
		return ok("1");
	}
	*/
	
	/**
	 * make album private
	 * @param serviceIndex
	 * @param albumId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 *
	@Logged(Role.ADMIN)
	public static Result privAlbum(int serviceIndex, String albumId) throws IOException, ServiceException {
		URL feedUrl = new URL(API_URL+"/albumid/"+albumId);
		Logger.debug(feedUrl+"");
		AlbumEntry ae = myServices.get(serviceIndex).getEntry(feedUrl, AlbumEntry.class);
		ae.setTitle(new PlainTextConstruct(ae.getTitle().getPlainText().replaceAll("\u00A0", "").replaceAll("\\+", "")));
		ae.update();
		return ok("0");
	}
	*/

	/**
	 * get exif tags
	 * @param serviceIndex
	 * @param albumId
	 * @param photoId
	 * @return
	 * @throws IOException
	 * @throws ServiceException
	 */
	public static Result exif(int serviceIndex, String albumId, String photoId, String photoSecret) throws IOException, FlickrException {
		Flickr f = myServices.get(serviceIndex).getFlickr();
		return ok(exif.render(f.getPhotosInterface().getExif(photoId, photoSecret)));
	}

	public static String getUUID() {
		String uuid = session("uuid");
		if(uuid==null) {
			uuid=java.util.UUID.randomUUID().toString();
			session("uuid", uuid);
		}
		return session("uuid");
	}
	
	public static Result flickrAuth() throws Exception {
		
		String oauth_token = request().getQueryString("oauth_token");
		String oauth_verifier = request().getQueryString("oauth_verifier");
		String apiKey = null;
		String secret = null;
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			InputStream in;			
			if(System.getProperty("config") != null && new File(System.getProperty("config")).canRead()) {
				in =  new FileInputStream(new File(System.getProperty("config")));
			} else {
				if(new File(".", CONFIG).canRead()) {
					in = new FileInputStream(new File(".", CONFIG));
				} else {
					in =  Application.class.getResourceAsStream("/resources/"+CONFIG);
				}
			}
			Document doc = builder.parse(in);
			in.close();
			
			XPath xpath = XPathFactory.newInstance().newXPath();
			
			/**
			 * picasa accounts
			 */
			NodeList l = (NodeList) xpath.evaluate("//flickr/account", doc, XPathConstants.NODESET);
			for(int i=0; i < l.getLength(); i++) {
				Node account = l.item(i);
				apiKey = account.getAttributes().getNamedItem("apiKey").getNodeValue();
				secret = account.getAttributes().getNamedItem("secret").getNodeValue();
			}
		} catch (Exception e) {
			throw new NoAccountsException(e);
		}
		
		Flickr myService = new Flickr(apiKey, secret, new REST());
		AuthInterface a = myService.getAuthInterface();
		
		if(oauth_verifier == null || oauth_verifier.equals("")) {
			requestToken = a.getRequestToken("http://localhost:9000/auth");
			Logger.info(requestToken.toString());
			String url = a.getAuthorizationUrl(requestToken, com.flickr4java.flickr.auth.Permission.DELETE);
			return redirect(url);
		} else {
			accessToken = a.getAccessToken(requestToken, new Verifier(oauth_verifier));
		}
		
		return ok(auth.render(accessToken.toString()));
	}

}
