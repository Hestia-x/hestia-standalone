package huck.hestia.history;

import java.time.LocalDate;
import java.util.ArrayList;

public class BalanceChangeGroup {
	private String id;
	private LocalDate date;
	private String description;	
	private ArrayList<BalanceChange> balanceChangeList;
	
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
	
	public ArrayList<BalanceChange> getBalanceChangeList() {
		return balanceChangeList;
	}
	public void setBalanceChangeList(ArrayList<BalanceChange> balanceChangeList) {
		this.balanceChangeList = balanceChangeList;
	}
}
