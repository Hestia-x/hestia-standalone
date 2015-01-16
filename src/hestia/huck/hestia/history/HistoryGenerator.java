package huck.hestia.history;

import huck.hestia.history.AccountHistory.GroupType;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.function.Predicate;

public class HistoryGenerator {
	public static <T extends Account> AccountHistory createAccountHistory(LocalDate fromDate, LocalDate toDate, GroupType groupType, ArrayList<BalanceChanger> dataList, Class<T> targetCls, Predicate<T> targetPredicate) throws SQLException {
		LocalDateTime fromDttm = fromDate.atStartOfDay().minusNanos(1);
		LocalDateTime toDttm = toDate.plusDays(1).atStartOfDay();

		if( null == targetPredicate ) {
			targetPredicate = a->true;
		}
		// toDate 이 후 데이터는 제거.
		// fromDate 기준으로 이전 데이터, 조회 데이터 구분.
		ArrayList<BalanceChanger> beforeChangerList = new ArrayList<BalanceChanger>();
		ArrayList<BalanceChanger> periodChangerList = new ArrayList<BalanceChanger>();
		dataList.stream()
			.filter(a -> a.occurrenceDttm().isBefore(toDttm))
			.forEach(a -> (a.occurrenceDttm().isAfter(fromDttm)?periodChangerList:beforeChangerList).add(a));

		// 조회 기간 이전 데이터로 account별 요약표 작성.
		TreeMap<Integer, BalanceChange> targetSummaryMap = new TreeMap<>(); // accountId -> price
		for( BalanceChanger changer : beforeChangerList ) {
			T target = changer.target(targetCls);
			if( null == target || !targetPredicate.test(target)) continue;
			int change = changer.amount();
			BalanceChange targetSummary = targetSummaryMap.get(target.accountId());
			if( null == targetSummary ) {
				targetSummary = new BalanceChange(target.accountId(), target.accountName(), 0, 0, 0);
				targetSummaryMap.put(target.accountId(), targetSummary);
			}
			targetSummary.setBefore(targetSummary.getBefore() + change);
			targetSummary.setBalance(targetSummary.getBalance() + change);
		}

		// 조회 기간 데이터는 시간 기준으로 정렬 후 Group에 맞춰 account별 변동 데이터 작성. 
		periodChangerList.sort(Comparator.comparing(BalanceChanger::occurrenceDttm).thenComparing(BalanceChanger::occurrenceId));
		ArrayList<BalanceChangeGroup> changeGroupList = new ArrayList<>();
		HashSet<Integer> changedTargetIdSet = new HashSet<>();
		BalanceChangeGroup nGroup = null;
		TreeMap<Integer, BalanceChange> nChangeMap = null;
		for( BalanceChanger changer : periodChangerList ) {
			T target = changer.target(targetCls);
			if( null == target || !targetPredicate.test(target) ) continue;
			changedTargetIdSet.add(target.accountId());
			
			int change = changer.amount();
			
			String groupId = groupType.idExtractor().apply(changer);
			if( null == nGroup || !nGroup.getId().equals(groupId) ) {
				if( null != nGroup ) {
					nGroup.setBalanceChangeList(new ArrayList<BalanceChange>(nChangeMap.values()));
				}
				nGroup = new BalanceChangeGroup();
				nGroup.setId(groupId);
				nGroup.setDescription(groupType.descriptionExtractor().apply(changer));
				nGroup.setDate(groupType.localDateExtractor().apply(changer));
				changeGroupList.add(nGroup);
				nChangeMap = new TreeMap<>();
			}
			
			BalanceChange targetSummary = targetSummaryMap.get(target.accountId());
			if( null == targetSummary ) {
				targetSummary = new BalanceChange(target.accountId(), target.accountName(), 0, 0, 0);
				targetSummaryMap.put(target.accountId(), targetSummary);
			}
			BalanceChange changeData = nChangeMap.get(target.accountId());
			if( null == changeData ) {
				changeData = new BalanceChange(target.accountId(), target.accountName(), targetSummary.getBalance(), 0, targetSummary.getBalance());
				nChangeMap.put(target.accountId(), changeData);
			}
			targetSummary.setChange(targetSummary.getChange() + change);
			targetSummary.setBalance(targetSummary.getBalance() + change);
			changeData.setChange(changeData.getChange() + change);
			changeData.setBalance(changeData.getBalance() + change);
		}
		if( null != nGroup ) {
			nGroup.setBalanceChangeList(new ArrayList<BalanceChange>(nChangeMap.values()));
		}
		
		// 변동도 없고 잔액도 없는 Account는 결과에서 제거.
		ArrayList<Integer> removeAccountIdList = new ArrayList<>();
		for( BalanceChange data : targetSummaryMap.values() ) {
			if( !changedTargetIdSet.contains(data.getAccountId()) && 0 == data.getBefore() && 0 == data.getChange() && 0 == data.getBalance() ) {
				removeAccountIdList.add(data.getAccountId());
			}
		}
		for( Integer accountId : removeAccountIdList ) {
			targetSummaryMap.remove(accountId);
		}
		
		// Account 전체 요약표.
		int beforeSum = 0;
		int changeSum = 0;
		int balanceSum = 0;
		for( BalanceChange data : targetSummaryMap.values() ) {
			beforeSum += data.getBefore();
			changeSum += data.getChange();
			balanceSum += data.getBalance();
		}
		BalanceChange summary = new BalanceChange(-1, "Summary", beforeSum, changeSum, balanceSum);
		
		// 결과 리턴.
		AccountHistory result = new AccountHistory();
		result.setFromDate(fromDate);
		result.setToDate(toDate);
		result.setGroupType(groupType);
		result.setBalanceChangeSummaryMap(targetSummaryMap);
		result.setBalanceChangeSummary(summary);
		result.setBalanceChangeGroupList(changeGroupList);
		return result;
	}
}
