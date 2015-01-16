package huck.hestia.history;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.function.Function;

public class AccountHistory {
	public static enum GroupType {
		OCCURRENCE(
			a->""+a.occurrenceId(),
			a->a.occurrenceLocation(),
			a->a.occurrenceDttm().toLocalDate()
		),
		DAY(
			a->a.occurrenceDttm().toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE),
			a->a.occurrenceDttm().toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE),
			a->a.occurrenceDttm().toLocalDate()
		),
		MONTH(
			a->a.occurrenceDttm().toLocalDate().withDayOfMonth(1).format(DateTimeFormatter.BASIC_ISO_DATE),
			a->a.occurrenceDttm().toLocalDate().withDayOfMonth(1).format(DateTimeFormatter.BASIC_ISO_DATE),
			a->a.occurrenceDttm().toLocalDate().withDayOfMonth(1)
		)
		;		
		private Function<BalanceChanger, String> idExtractor;
		private Function<BalanceChanger, String> descriptionExtractor;
		private Function<BalanceChanger, LocalDate> localDateExtractor;
		private GroupType(Function<BalanceChanger, String> idExtractor, Function<BalanceChanger, String> descriptionExtractor, Function<BalanceChanger, LocalDate> localDateExtractor) {
			this.idExtractor = idExtractor;
			this.descriptionExtractor = descriptionExtractor;
			this.localDateExtractor = localDateExtractor;
		}		
		public Function<BalanceChanger, String> idExtractor() {
			return idExtractor;
		}
		public Function<BalanceChanger, String> descriptionExtractor() {
			return descriptionExtractor;
		}
		public Function<BalanceChanger, LocalDate> localDateExtractor() {
			return localDateExtractor;
		}
	}
	
	private GroupType groupType;
	private LocalDate fromDate;
	private LocalDate toDate;
	private TreeMap<Integer, BalanceChange> balanceChangeSummaryMap;
	private BalanceChange balanceChangeSummary;
	private ArrayList<BalanceChangeGroup> balanceChangeGroupList;

	public GroupType getGroupType() {
		return groupType;
	}
	public void setGroupType(GroupType groupType) {
		this.groupType = groupType;
	}
	
	public LocalDate getFromDate() {
		return fromDate;
	}
	public void setFromDate(LocalDate fromDate) {
		this.fromDate = fromDate;
	}
	
	public LocalDate getToDate() {
		return toDate;
	}
	public void setToDate(LocalDate toDate) {
		this.toDate = toDate;
	}

	public BalanceChange getBalanceChangeSummary() {
		return balanceChangeSummary;
	}
	public void setBalanceChangeSummary(BalanceChange balanceChangeSummary) {
		this.balanceChangeSummary = balanceChangeSummary;
	}
	
	public TreeMap<Integer, BalanceChange> getBalanceChangeSummaryMap() {
		return balanceChangeSummaryMap;
	}
	public void setBalanceChangeSummaryMap(TreeMap<Integer, BalanceChange> balanceChangeSummaryMap) {
		this.balanceChangeSummaryMap = balanceChangeSummaryMap;
	}
	
	public ArrayList<BalanceChangeGroup> getBalanceChangeGroupList() {
		return balanceChangeGroupList;
	}
	public void setBalanceChangeGroupList(
			ArrayList<BalanceChangeGroup> balanceChangeGroupList) {
		this.balanceChangeGroupList = balanceChangeGroupList;
	}
}