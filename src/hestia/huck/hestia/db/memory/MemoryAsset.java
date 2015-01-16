package huck.hestia.db.memory;

import huck.hestia.db.Asset;

class MemoryAsset implements Asset {
	private int id;
	private String name;
	private String description;
	
	public MemoryAsset(int id, String name, String description) {
		id(id);
		name(name);
		description(description);
	}
	
	@Override
	public int id() {
		return id;
	}
	void id(int id) {
		this.id = id;
	}
	
	@Override
	public String name() {
		return name;
	}
	void name(String name) {
		if( null == name || 0 == name.trim().length() ) {
			throw new IllegalArgumentException("name can not be null");
		}
		this.name = name;
	}
	
	@Override
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
