package huck.hestia.db.data;


public class Shop {
	private int id;
	private String name;
	
	public Shop(int id, String name) {
		id(id);
		name(name);
	}
	
	public int id() {
		return id;
	}
	void id(int id) {
		this.id = id;
	}
	
	public String name() {
		return name;
	}
	void name(String name) {
		if( null == name || 0 == name.trim().length() ) {
			throw new IllegalArgumentException("name can not be null");
		}
		this.name = name;
	}
}
