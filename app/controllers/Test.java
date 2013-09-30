package controllers;

import static play.Logger.debug;
import static play.Logger.error;
import static play.Logger.info;
import interceptors.Logged;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import others.Role;
import others.Service;
import views.html.test;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.uploader.UploadMetaData;
import model.Album;
import model.Utils;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import play.Logger;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;

public class Test extends Controller {
	
	static Random random = new Random();

	@Logged(Role.ADMIN)
	public static Result deleteAllAlbums() {
		try {
			for(Service s: Application.myServices) {
				Photosets ps = s.getFlickr().getPhotosetsInterface().getList(s.getAuth().getUser().getId());
				for (Photoset e: ps.getPhotosets()) {
					s.getFlickr().getPhotosetsInterface().delete(e.getId());
				}
			}
		} catch (Exception e) {
			return play.mvc.Results.internalServerError(e.getMessage());
		}
		return redirect("/?message=albums deleted");
	}
	
	// @Logged(Role.ADMIN)
	public static Result loadTestData(int count, boolean create) {
		String address = "http://www.impawards.com/2012/std.html";
		String outputDir = "data";
		
		// obrazki
		try {

			URL url = new URL(address);
			URLConnection uc = url.openConnection();
			InputStream is = uc.getInputStream();
			String prefix = address.substring(0, address.lastIndexOf('/') + 1);

			// jericho
			Source source = new Source(is);
			List<Element> divs = source.getAllElements(HTMLElementName.DIV);
			Element content = null;
			for (Element div : divs) {
				String styleClass = div.getAttributeValue("class");
				if (styleClass != null && styleClass.equals("content")) {
					content = div;
					break;
				}
			}
			Integer i = 0, j = 0;
			
			if (content != null) {

				int index = random.nextInt(Application.myServices.size());
				
				List<Element> trs = content.getAllElements(HTMLElementName.TR);
				System.out.println(trs.size() + " nodes found.");
				Collections.shuffle(trs); // pomieszaj :)
				for (Element tr : trs) {
					List<Element> list = tr.getAllElements(HTMLElementName.IMG);

					Element tdWithTitle = tr.getFirstElement(HTMLElementName.TD);
					Element font = tdWithTitle.getFirstElement();
					String name = new TextExtractor(font.getContent()).toString();
										
					String albumId = "";
			
					int k = 0;
					// dodaj obrazki do produktu
					for (Element img : list) {
						
						String imageUrl = prefix + img.getAttributeValue("src");
						imageUrl = imageUrl.replaceFirst("thumbs/imp_", "posters/");
						String filename = imageUrl.substring(imageUrl.lastIndexOf('/')+1);
						
						info(imageUrl);
						InputStream in = new URL(imageUrl).openStream();
						UploadMetaData metaData = new UploadMetaData();
		                metaData.setHidden(true);
		                metaData.setPublicFlag(false);
		                metaData.setTitle(filename);
		                
		                Photoset photoset;
						if(k == 0 && create) {
							info("Creating album '"+name+"' ...");
							
							RequestContext.getRequestContext().setAuth(Application.myServices.get(index).getAuth()); // auth request
			                String photoId = Application.myServices.get(index).getFlickr().getUploader().upload(in, metaData);
							photoset = Application.myServices.get(index).getFlickr().getPhotosetsInterface().create(name, "", photoId);
						} else {
							List<Album> l = Application.getAlbums();
							Album a = l.get(random.nextInt(l.size()));
							albumId = a.getId();
							index = a.getServiceIndex();

			                String photoId = Application.myServices.get(index).getFlickr().getUploader().upload(in, metaData);
							Application.myServices.get(index).getFlickr().getPhotosetsInterface().addPhoto(albumId, photoId);
						}

						in.close();
						i++;
						
						if(!create && i >= count) {
							break;
						}
						k++;
					}
					
					if((create && j >= count) || (!create && i >= count)) {
						break;
					}
					
				}
				System.out.println(j + " albums, "+i+" photos added.");
				String uuid = Application.getUUID();
				Cache.set(uuid+"albums", null);
			}
			
			is.close();
			
			// return ok(i + " products added.");
			flash("message", j + " albums, "+i+" photos added.");
			return redirect("/");

		} catch (Exception e) {
			e.printStackTrace();
			return ok(e.getMessage() != null ? e.getMessage() : e+"");
		}
		
	}

	@Logged(Role.ADMIN)
	public static Result test() {
		if(request().method().equalsIgnoreCase("GET")) {
			return ok(test.render());
		} else {
			String count = request().body().asFormUrlEncoded().get("count")[0].trim();
			String[] b = request().body().asFormUrlEncoded().get("create");
			return redirect("/test/"+(b != null ? "1": "0")+(count.equals("") ? "" : "/"+count));
		}
	}
}
