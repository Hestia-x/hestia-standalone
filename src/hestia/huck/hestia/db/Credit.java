package huck.hestia.db;

import huck.hestia.history.Account;
import huck.hestia.history.BalanceChanger;

import java.time.LocalDateTime;

public interface Credit extends BalanceChanger {
	public int id();
	public Slip slip();
	public CreditCode creditCode();
	public String description();
	public int price();

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
