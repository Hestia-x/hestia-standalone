package huck.hestia.db.memory;

import huck.hestia.db.Shop;


class MemoryShop implements Shop {
	private int id;
	private String name;
	
	public MemoryShop(int id, String name) {
		id(id);
		name(name);
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
}
