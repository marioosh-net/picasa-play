package model;


public class Album {
	private String title;
	private String id;
	private int count;
	private int serviceIndex;
	private String thumbnailUrl;
	
	public Album(String id, String title, String thumbnailUrl, int count, int serviceIndex) {
		this.id = id;
		this.title = title;
		this.count = count;
		this.serviceIndex = serviceIndex;
		this.thumbnailUrl = thumbnailUrl;
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
	
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}

	
	public int getServiceIndex() {
		return serviceIndex;
	}
	
	
	public void setServiceIndex(int serviceIndex) {
		this.serviceIndex = serviceIndex;
	}
	
	
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
}
