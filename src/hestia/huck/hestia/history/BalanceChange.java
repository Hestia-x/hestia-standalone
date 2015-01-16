package huck.hestia.history;

public class BalanceChange {
	private int accountId;
	private String accountName;
	private int before;
	private int change;
	private int balance;
	
	public BalanceChange(int accountId, String accountName, int before, int change, int balance) {
		this.accountId = accountId;
		this.accountName = accountName;
		this.before = before;
		this.change = change;
		this.balance = balance;
	}
	
	public int getAccountId() {
		return accountId;
	}
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public int getBefore() {
		return before;
	}
	public void setBefore(int before) {
		this.before = before;
	}
	
	public int getChange() {
		return change;
	}
	public void setChange(int change) {
		this.change = change;
	}
	
	public int getBalance() {
		return balance;
	}
	public void setBalance(int balance) {
		this.balance = balance;
	}
}
