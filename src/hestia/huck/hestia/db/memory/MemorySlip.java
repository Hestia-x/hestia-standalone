package huck.hestia.db.memory;

import huck.hestia.db.Slip;

import java.time.LocalDateTime;

class MemorySlip implements Slip {
	private int id;
	private LocalDateTime slipDttm;
	private MemoryShop shop;
	
	public MemorySlip(int id, LocalDateTime slipDttm, MemoryShop shop) {
		this.id = id;
		slipDttm(slipDttm);
		shop(shop);
	}
	
	@Override
	public int id() {
		return id;
	}
	
	@Override
	public LocalDateTime slipDttm() {
		return slipDttm;
	}
	public void slipDttm(LocalDateTime slipDttm) {
		if( null == slipDttm ) {
			throw new IllegalArgumentException("slipDttm can not be null");
		}
		this.slipDttm = slipDttm;
	}
	
	@Override
	public MemoryShop shop() {
		return shop;
	}
	public void shop(MemoryShop shop) {
		if( null == shop ) {
			throw new IllegalArgumentException("shop can not be null");
		}
		this.shop = shop;
	}
}
