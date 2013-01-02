package model;

import java.util.List;


public class Photo {
	private String title;
	private String id;
	private List<String> thumbs;
	private String content;
	private String albumId;
	private String[] keywords;
	private boolean pub;
	
	public Photo(String title, String id, List<String> thumbs, String content, String albumId, String[] keywords, boolean pub) {
		super();
		this.title = title;
		this.id = id;
		this.thumbs = thumbs;
		this.content = content;
		this.albumId = albumId;
		this.keywords = keywords;
		this.pub = pub;
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
	
}
