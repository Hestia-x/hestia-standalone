package huck.hestia.db;

import huck.hestia.history.Account;

public interface Asset extends Account {
	public int id();
	public String name();
	public String description();

	@Override
	public int accountId();
	@Override
	public String accountName();
}
