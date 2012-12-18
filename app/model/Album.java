package model;


public class Album {
	private String title;
	private String id;
	private int count;
	
	public Album(String id, String title, int count) {
		this.id = id;
		this.title = title;
		this.count = count;
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
	
}
