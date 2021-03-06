package huck.hestia.history;

import java.time.LocalDateTime;

public interface AccountChanger {
	public <Target extends Account> Target target(Class<Target> cls);
	
	public int occurrenceId();
	public LocalDateTime occurrenceDttm();
	public String occurrenceLocation();
	public int amount();
}
