package model;

import java.util.Collection;
import java.util.List;
import com.flickr4java.flickr.photos.Exif;

public class Photo {
	private String title;
	private String id;
	private List<String> thumbs;
	private String content;
	private String albumId;
	private String secret;
	private String[] keywords;
	private boolean pub;
	private Collection<Exif> exif;
	
	public Photo(String title, String id, List<String> thumbs, String content, String albumId, String[] keywords, boolean pub, Collection<Exif> exif, String secret) {
		super();
		this.title = title;
		this.id = id;
		this.thumbs = thumbs;
		this.content = content;
		this.albumId = albumId;
		this.keywords = keywords;
		this.pub = pub;
		this.exif = exif;
		this.secret = secret;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public List<String> getThumbs() {
		return thumbs;
	}
	
	public void setThumbs(List<String> thumbs) {
		this.thumbs = thumbs;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	
	public String getAlbumId() {
		return albumId;
	}
	
	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}
	

	
	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}
	
	
	public String[] getKeywords() {
		return keywords;
	}

	
	public boolean isPub() {
		return pub;
	}
	
	
	public void setPub(boolean pub) {
		this.pub = pub;
	}

	
	public Collection<Exif> getExif() {
		return exif;
	}
	
	
	public void setExif(Collection<Exif> exif) {
		this.exif = exif;
	}

	
	public String getSecret() {
		return secret;
	}
	
	
	public void setSecret(String secret) {
		this.secret = secret;
	}
}
