package huck.hestia.db.data;

import huck.hestia.history.Account;
import huck.hestia.history.BalanceChanger;

import java.time.LocalDateTime;

public class Credit implements BalanceChanger {
	private int id;
	private Slip slip;
	private CreditCode creditCode;
	private String description;
	private int price;
	
	public Credit(int id, Slip slip, CreditCode creditCode, String description, int price) {
		this.id = id;
		slip(slip);
		creditCode(creditCode);
		description(description);
		price(price);
	}
	
	public int id() {
		return id;
	}
	
	public Slip slip() {
		return slip;
	}
	void slip(Slip slip) {
		if( null == slip ) {
			throw new IllegalArgumentException("slip can not be null");
		}
		this.slip = slip;
	}
	
	public CreditCode creditCode() {
		return creditCode;
	}
	void creditCode(CreditCode creditCode) {
		if( null == creditCode ) {
			throw new IllegalArgumentException("creditCode can not be null");
		}
		this.creditCode = creditCode;
	}
	
	public String description() {
		return description;
	}
	void description(String description) {
		if( null == description || 0 == description.trim().length() ) {
			throw new IllegalArgumentException("description can not be null");
		}
		this.description = description;
	}
	
	public int price() {
		return price;
	}
	void price(int price) {
		this.price = price;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Account> T target(Class<T> cls) {
		if( cls.equals(Asset.class)) {			
			return (T)creditCode.asset();
		} else {
			return null;
		}
	}
	@Override
	public int amount() {
		return price;
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
