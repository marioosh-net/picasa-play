package model;

import java.util.List;
import com.google.gdata.data.photos.ExifTags;


public class Photo {
	private String title;
	private String id;
	private List<String> thumbs;
	private String content;
	private String albumId;
	private String[] keywords;
	private boolean pub;
	private ExifTags exif;
	
	public Photo(String title, String id, List<String> thumbs, String content, String albumId, String[] keywords, boolean pub, ExifTags exif) {
		super();
		this.title = title;
		this.id = id;
		this.thumbs = thumbs;
		this.content = content;
		this.albumId = albumId;
		this.keywords = keywords;
		this.pub = pub;
		this.exif = exif;
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

	
	public ExifTags getExif() {
		return exif;
	}
	
	
	public void setExif(ExifTags exif) {
		this.exif = exif;
	}
}
