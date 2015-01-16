package huck.hestia.db.memory;

import huck.hestia.db.CreditCode;


class MemoryCreditCode implements CreditCode {
	private int id;
	private String name;
	private MemoryAsset asset;
	private String defaultDescription;
	
	public MemoryCreditCode(int id, String name, MemoryAsset asset, String defaultDescription) {
		id(id);
		name(name);
		asset(asset);
		defaultDescription(defaultDescription);
	}	
	
	@Override
	public int id() {
		return id;
	}
	public void id(int id) {
		this.id = id;
	}
	
	@Override
	public String name() {
		return name;
	}
	public void name(String name) {
		if( null == name || 0 == name.trim().length() ) {
			throw new IllegalArgumentException("name can not be null");
		}
		this.name = name;
	}
	
	@Override
	public MemoryAsset asset() {
		return asset;
	}
	public void asset(MemoryAsset asset) {
		this.asset = asset;
	}
	
	@Override
	public String defaultDescription() {
		return defaultDescription;
	}
	public void defaultDescription(String defaultDescription) {
		this.defaultDescription = defaultDescription;
	}
}
