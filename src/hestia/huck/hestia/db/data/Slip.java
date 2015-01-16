package huck.hestia.db.data;

import java.time.LocalDateTime;

public class Slip {
	private int id;
	private LocalDateTime slipDttm;
	private Shop shop;
	
	public Slip(int id, LocalDateTime slipDttm, Shop shop) {
		this.id = id;
		slipDttm(slipDttm);
		shop(shop);
	}
	
	public int id() {
		return id;
	}
	
	public LocalDateTime slipDttm() {
		return slipDttm;
	}
	void slipDttm(LocalDateTime slipDttm) {
		if( null == slipDttm ) {
			throw new IllegalArgumentException("slipDttm can not be null");
		}
		this.slipDttm = slipDttm;
	}
	
	public Shop shop() {
		return shop;
	}
	void shop(Shop shop) {
		if( null == shop ) {
			throw new IllegalArgumentException("shop can not be null");
		}
		this.shop = shop;
	}
}
