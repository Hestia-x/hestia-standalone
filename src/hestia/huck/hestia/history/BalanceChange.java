package huck.hestia.history;

public class BalanceChange {
	private int accountId;
	private String accountName;
	private int beginning;
	private int increase;
	private int decrease;
	private int ending;
	
	public BalanceChange(int accountId, String accountName, int beginning, int increase, int decrease) {
		this.accountId = accountId;
		this.accountName = accountName;
		this.beginning = beginning;
		this.increase = increase;
		this.decrease = decrease;
		this.ending = beginning+increase-decrease;
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

	public int getBeginning() {
		return beginning;
	}
	public void setBeginning(int beginning) {
		this.beginning = beginning;
	}

	public int getIncrease() {
		return increase;
	}
	public void setIncrease(int increase) {
		this.increase = increase;
	}

	public int getDecrease() {
		return decrease;
	}
	public void setDecrease(int decrease) {
		this.decrease = decrease;
	}

	public int getEnding() {
		return ending;
	}
	public void setEnding(int ending) {
		this.ending = ending;
	}

}
