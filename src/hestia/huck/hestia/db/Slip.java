package huck.hestia.db;

import java.time.LocalDateTime;

public interface Slip {
	public int id();
	public LocalDateTime slipDttm();
	public Shop shop();
}
