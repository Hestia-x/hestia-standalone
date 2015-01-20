package huck.hestia.history;

import java.time.LocalDate;
import java.util.List;

public class BalanceChangeGroup {
	private String id;
	private LocalDate date;
	private String description;	
	private List<BalanceChange> balanceChangeList;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<BalanceChange> getBalanceChangeList() {
		return balanceChangeList;
	}
	public void setBalanceChangeList(List<BalanceChange> balanceChangeList) {
		this.balanceChangeList = balanceChangeList;
	}
	
	public BalanceChange getSummary() {
		int beforeSum = 0;
		int changeSum = 0;
		int balanceSum = 0;
		if( null != balanceChangeList ) {
			for( BalanceChange data : balanceChangeList ) {
				beforeSum += data.getBefore();
				changeSum += data.getChange();
				balanceSum += data.getBalance();
			}
		}
		return new BalanceChange(-1, "Summary", beforeSum, changeSum, balanceSum);
	}
}
