package huck.hestia.db;

import huck.hestia.history.Account;


public interface CreditCode extends Account {
	public int id();
	public String name();
	public Asset asset();
	public String defaultDescription();
	
	@Override
	public int accountId();
	@Override
	public String accountName();
}
