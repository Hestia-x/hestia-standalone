package huck.hestia.db.data;

import huck.hestia.history.Account;

public class DebitCode implements Account {
	private int id;
	private String name;
	private Asset asset;
	private String defaultDescription;
	
	public DebitCode(int id, String name, Asset asset, String defaultDescription) {
		id(id);
		name(name);
		asset(asset);
		defaultDescription(defaultDescription);
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
	
	public Asset asset() {
		return asset;
	}
	void asset(Asset asset) {
		this.asset = asset;
	}
	
	public String defaultDescription() {
		return defaultDescription;
	}
	void defaultDescription(String defaultDescription) {
		this.defaultDescription = defaultDescription;
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
