package huck.hestia.history;

import java.time.LocalDateTime;

public interface BalanceChanger {
	public <T extends Account> T target(Class<T> cls);
	
	public int occurrenceId();
	public LocalDateTime occurrenceDttm();
	public String occurrenceLocation();
	public int amount();
}
