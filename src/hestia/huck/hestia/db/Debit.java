package huck.hestia.db;

import huck.hestia.history.Account;
import huck.hestia.history.AccountChanger;

import java.time.LocalDateTime;

public interface Debit extends AccountChanger {
	public int id();
	public Slip slip();
	public DebitCode debitCode();
	public String description();
	public int unitPrice();
	public int quantity();

	@Override
	public <Target extends Account> Target target(Class<Target> cls);
	@Override
	public int amount();
	@Override
	public int occurrenceId();
	@Override
	public LocalDateTime occurrenceDttm();
	@Override
	public String occurrenceLocation();
}
