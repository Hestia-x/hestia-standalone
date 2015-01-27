package huck.hestia.db.memory;

import huck.hestia.db.Asset;
import huck.hestia.db.Credit;
import huck.hestia.db.CreditCode;
import huck.hestia.history.Account;

import java.time.LocalDateTime;

class MemoryCredit implements Credit {
	private int id;
	private MemorySlip slip;
	private MemoryCreditCode creditCode;
	private String description;
	private int price;
	
	public MemoryCredit(int id, MemorySlip slip, MemoryCreditCode creditCode, String description, int price) {
		this.id = id;
		slip(slip);
		creditCode(creditCode);
		description(description);
		price(price);
	}
	
	@Override
	public int id() {
		return id;
	}
	
	@Override
	public MemorySlip slip() {
		return slip;
	}
	public void slip(MemorySlip slip) {
		if( null == slip ) {
			throw new IllegalArgumentException("slip can not be null");
		}
		this.slip = slip;
	}
	
	@Override
	public MemoryCreditCode creditCode() {
		return creditCode;
	}
	public void creditCode(MemoryCreditCode creditCode) {
		if( null == creditCode ) {
			throw new IllegalArgumentException("creditCode can not be null");
		}
		this.creditCode = creditCode;
	}
	
	@Override
	public String description() {
		return description;
	}
	public void description(String description) {
		if( null == description || 0 == description.trim().length() ) {
			throw new IllegalArgumentException("description can not be null");
		}
		this.description = description;
	}
	
	@Override
	public int price() {
		return price;
	}
	public void price(int price) {
		if( 0 >= price ) {
			throw new IllegalArgumentException("price must be positive.");
		}
		this.price = price;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public <Target extends Account> Target target(Class<Target> cls) {
		if( cls.equals(Asset.class)) {			
			return (Target)creditCode.asset();
		} else if( cls.equals(CreditCode.class)) {			
			return (Target)creditCode;
		} else {
			return null;
		}
	}
	@Override
	public int amount() {
		return 0-price;
	}
	@Override
	public int occurrenceId() {
		return slip().id();
	}
	@Override
	public LocalDateTime occurrenceDttm() {
		return slip.slipDttm();
	}
	@Override
	public String occurrenceLocation() {
		return slip.shop().name();
	}
}
