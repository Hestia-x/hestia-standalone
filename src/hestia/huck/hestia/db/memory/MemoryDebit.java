package huck.hestia.db.memory;

import huck.hestia.db.Asset;
import huck.hestia.db.Debit;
import huck.hestia.db.DebitCode;
import huck.hestia.history.Account;

import java.time.LocalDateTime;

class MemoryDebit implements Debit {
	private int id;
	private MemorySlip slip;
	private MemoryDebitCode debitCode;
	private String description;
	private int unitPrice;
	private int quantity;
	
	public MemoryDebit(int id, MemorySlip slip, MemoryDebitCode debitCode, String description, int unitPrice, int quantity) {
		this.id = id;
		slip(slip);
		debitCode(debitCode);
		description(description);
		unitPrice(unitPrice);
		quantity(quantity);
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
	public MemoryDebitCode debitCode() {
		return debitCode;
	}
	public void debitCode(MemoryDebitCode debitCode) {
		if( null == debitCode ) {
			throw new IllegalArgumentException("debitCode can not be null");
		}
		this.debitCode = debitCode;
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
	public int unitPrice() {
		return unitPrice;
	}
	public void unitPrice(int unitPrice) {
		if( 0 >= unitPrice ) {
			throw new IllegalArgumentException("unitPrice must be positive.");
		}
		this.unitPrice = unitPrice;
	}
	
	@Override
	public int quantity() {
		return quantity;
	}
	public void quantity(int quantity) {
		if( 0 >= quantity ) {
			throw new IllegalArgumentException("quantity must be positive.");
		}
		this.quantity = quantity;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public <Target extends Account> Target target(Class<Target> cls) {
		if( cls.equals(Asset.class)) {			
			return (Target)debitCode.asset();
		} else if( cls.equals(DebitCode.class)) {			
			return (Target)debitCode;
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
