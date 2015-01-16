package huck.hestia.db.data;

import huck.hestia.history.Account;
import huck.hestia.history.BalanceChanger;

import java.time.LocalDateTime;

public class Debit implements BalanceChanger {
	private int id;
	private Slip slip;
	private DebitCode debitCode;
	private String description;
	private int unitPrice;
	private int quantity;
	
	public Debit(int id, Slip slip, DebitCode debitCode, String description, int unitPrice, int quantity) {
		this.id = id;
		slip(slip);
		debitCode(debitCode);
		description(description);
		unitPrice(unitPrice);
		quantity(quantity);
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
	
	public DebitCode debitCode() {
		return debitCode;
	}
	void debitCode(DebitCode debitCode) {
		if( null == debitCode ) {
			throw new IllegalArgumentException("debitCode can not be null");
		}
		this.debitCode = debitCode;
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
	
	public int unitPrice() {
		return unitPrice;
	}
	void unitPrice(int unitPrice) {
		this.unitPrice = unitPrice;
	}
	
	public int quantity() {
		return quantity;
	}
	void quantity(int quantity) {
		this.quantity = quantity;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Account> T target(Class<T> cls) {
		if( cls.equals(Asset.class)) {			
			return (T)debitCode.asset();
		} else {
			return null;
		}
	}	
	@Override
	public int amount() {
		return unitPrice * quantity;
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
