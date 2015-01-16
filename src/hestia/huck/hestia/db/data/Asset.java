package huck.hestia.db.data;

import huck.hestia.history.Account;

public class Asset implements Account {
	private int id;
	private String name;
	private String description;
	
	public Asset(int id, String name, String description) {
		id(id);
		name(name);
		description(description);
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
	
	public String description() {
		return description;
	}
	void description(String description) {
		this.description = description;
	}
	
	@Override
	public int accountId() {
		return id();
	}
	@Override
	public String accountName() {
		return name();
	}
}
