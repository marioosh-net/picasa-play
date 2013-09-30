package others;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.auth.Auth;

public class Service {
	Flickr flickr;
	Auth auth;
	
	public Service(Flickr flickr, Auth auth) {
		super();
		this.flickr = flickr;
		this.auth = auth;
	}

	public Flickr getFlickr() {
		return flickr;
	}
	
	public void setFlickr(Flickr flickr) {
		this.flickr = flickr;
	}
	
	public Auth getAuth() {
		return auth;
	}
	
	public void setAuth(Auth auth) {
		this.auth = auth;
	}
	
	
}
