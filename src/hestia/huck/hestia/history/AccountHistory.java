package huck.hestia.history;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
		private Function<AccountChanger, String> idExtractor;
		private Function<AccountChanger, String> descriptionExtractor;
		private Function<AccountChanger, LocalDate> localDateExtractor;
		private GroupType(Function<AccountChanger, String> idExtractor, Function<AccountChanger, String> descriptionExtractor, Function<AccountChanger, LocalDate> localDateExtractor) {
			this.idExtractor = idExtractor;
			this.descriptionExtractor = descriptionExtractor;
			this.localDateExtractor = localDateExtractor;
		}		
		public Function<AccountChanger, String> idExtractor() {
			return idExtractor;
		}
		public Function<AccountChanger, String> descriptionExtractor() {
			return descriptionExtractor;
		}
		public Function<AccountChanger, LocalDate> localDateExtractor() {
			return localDateExtractor;
		}
	}
	
	private GroupType groupType;
	private LocalDate fromDate;
	private LocalDate toDate;
	private BalanceChangeGroup balanceChangeSummary;
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

	public BalanceChangeGroup getBalanceChangeSummary() {
		return balanceChangeSummary;
	}
	public void setBalanceChangeSummary(BalanceChangeGroup balanceChangeSummary) {
		this.balanceChangeSummary = balanceChangeSummary;
	}
	
	public ArrayList<BalanceChangeGroup> getBalanceChangeGroupList() {
		return balanceChangeGroupList;
	}
	public void setBalanceChangeGroupList(
			ArrayList<BalanceChangeGroup> balanceChangeGroupList) {
		this.balanceChangeGroupList = balanceChangeGroupList;
	}
}