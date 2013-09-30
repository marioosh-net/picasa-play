import play.Logger;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;



public class FlickrTest {
	
	static final String API_KEY = "433ea818849c5c5ed6ac545b243196b1";
	static final String SECRET = "17aebaefbdd995ef";
	static final String USER_ID = "96083601@N05";
	
	public FlickrTest() {
		try {
			Flickr flickr = new Flickr(API_KEY, SECRET, new REST());
			Photosets ps = flickr.getPhotosetsInterface().getList(USER_ID);
			for(Photoset p: ps.getPhotosets()) {
				System.out.println(p.getTitle());
			}
		} catch (FlickrException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		new FlickrTest();
	}
}
