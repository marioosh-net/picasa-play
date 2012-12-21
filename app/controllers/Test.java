package controllers;

import static play.Logger.debug;
import static play.Logger.error;
import static play.Logger.info;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.UserFeed;
import model.Album;
import model.Utils;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;
import play.mvc.Controller;
import play.mvc.Result;

public class Test extends Controller {
	
	static Random random = new Random();
	
	public static Result deleteAllAlbums() {
		try {
			URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default?kind=album");
			
			for(PicasawebService s: Application.myServices) {
				UserFeed feed = s.getFeed(feedUrl, UserFeed.class);
				for(AlbumEntry a: feed.getAlbumEntries()) {
					a.delete();
				}
			}
		} catch (Exception e) {
			return play.mvc.Results.internalServerError(e.getMessage());
		}
		return redirect("/?message=albums deleted");
	}
	
	public static Result loadTestData(int count) {
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
			Integer i = 0;
			
			if (content != null) {

				List<Element> trs = content.getAllElements(HTMLElementName.TR);
				System.out.println(trs.size() + " nodes found.");
				i = 0;
				Collections.shuffle(trs); // pomieszaj :)
				for (Element tr : trs) {
					List<Element> list = tr.getAllElements(HTMLElementName.IMG);

					Element tdWithTitle = tr.getFirstElement(HTMLElementName.TD);
					Element font = tdWithTitle.getFirstElement();
					String name = new TextExtractor(font.getContent()).toString();

					System.out.println("Output path: "+new File(outputDir).getAbsolutePath());
					
					URL postUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default");
					AlbumEntry myAlbum = new AlbumEntry();
					myAlbum.setTitle(new PlainTextConstruct(name));
					int index = random.nextInt(2);
					AlbumEntry insertedEntry = Application.myServices.get(index).insert(postUrl, myAlbum);
					// Utils.describe(insertedEntry);
					String albumId = insertedEntry.getId().substring(insertedEntry.getId().lastIndexOf('/')+1);
					
					URL albumPostUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default/albumid/"+albumId);
					
					// dodaj obrazki do produktu
					for (Element img : list) {
						String imageUrl = prefix + img.getAttributeValue("src");
						imageUrl = imageUrl.replaceFirst("thumbs/imp_", "posters/");
						String filename = imageUrl.substring(imageUrl.lastIndexOf('/')+1);
						
						info(imageUrl);
						
						PhotoEntry myPhoto = new PhotoEntry();
						myPhoto.setTitle(new PlainTextConstruct(filename));
						myPhoto.setClient("myClientName");
						InputStream in = new URL(imageUrl).openStream();
						MediaStreamSource myMedia = new MediaStreamSource(in, "image/jpeg");
						myPhoto.setMediaSource(myMedia);
						PhotoEntry returnedPhoto = Application.myServices.get(index).insert(albumPostUrl, myPhoto);
						in.close();
					}
					
					i++;
					if(i == count) {
						break;
					}
					
				}
				System.out.println(i + " products added.");
			}
			
			is.close();
			
			// return ok(i + " products added.");
			return redirect("/?message="+ i + " albums added.");

		} catch (Exception e) {
			e.printStackTrace();
			return play.mvc.Results.internalServerError(e.getMessage());
		}
		
	}
}
