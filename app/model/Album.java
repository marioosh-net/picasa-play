package model;


public class Album {
	private String title;
	private String id;
	private int count;
	private int serviceIndex;
	private String serviceUser;
	private String thumbnailUrl;
	private boolean pub;
	
	public Album(String id, String title, String thumbnailUrl, int count, int serviceIndex, boolean pub, String serviceUser) {
		this.id = id;
		this.title = title;
		this.count = count;
		this.serviceIndex = serviceIndex;
		this.thumbnailUrl = thumbnailUrl;
		this.pub = pub;
		this.serviceUser = serviceUser;
	}

	public String getTitle() {
		return title;
	}
	
	public String getTitleShort() {
		if(title != null && title.length() > 42) {
			return title.substring(0, 39)+"...";
		}
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
	
	
	public boolean isPub() {
		return pub;
	}
	
	
	public void setPub(boolean pub) {
		this.pub = pub;
	}
	
	
	public String getServiceUser() {
		return serviceUser;
	}
	
	public void setServiceUser(String serviceUser) {
		this.serviceUser = serviceUser;
	}
}
