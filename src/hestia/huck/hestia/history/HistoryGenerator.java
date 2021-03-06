package huck.hestia.history;

import huck.hestia.history.AccountHistory.GroupType;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Predicate;

public class HistoryGenerator {
	public static <Target extends Account> AccountHistory createAccountHistory(LocalDate fromDate, LocalDate toDate, GroupType groupType, List<? extends AccountChanger> dataList, Class<Target> targetCls, Predicate<Target> targetPredicate) throws SQLException {
		LocalDateTime fromDttm = fromDate.atStartOfDay().minusNanos(1);
		LocalDateTime toDttm = toDate.plusDays(1).atStartOfDay();

		if( null == targetPredicate ) {
			targetPredicate = a->true;
		}
		// toDate 이 후 데이터는 제거.
		// fromDate 기준으로 이전 데이터, 조회 데이터 구분.
		ArrayList<AccountChanger> beforeChangerList = new ArrayList<AccountChanger>();
		ArrayList<AccountChanger> periodChangerList = new ArrayList<AccountChanger>();
		dataList.stream()
			.filter(a -> a.occurrenceDttm().isBefore(toDttm))
			.forEach(a -> (a.occurrenceDttm().isAfter(fromDttm)?periodChangerList:beforeChangerList).add(a));

		// 조회 기간 이전 데이터로 account별 요약표 작성.
		TreeMap<Integer, BalanceChange> targetSummaryMap = new TreeMap<>(); // accountId -> price
		for( AccountChanger changer : beforeChangerList ) {
			Target target = changer.target(targetCls);
			if( null == target || !targetPredicate.test(target)) continue;
			int change = changer.amount();
			BalanceChange targetSummary = targetSummaryMap.get(target.accountId());
			if( null == targetSummary ) {
				targetSummary = new BalanceChange(target.accountId(), target.accountName(), 0, 0, 0);
				targetSummaryMap.put(target.accountId(), targetSummary);
			}
			targetSummary.setBeginning(targetSummary.getBeginning() + change);
			targetSummary.setEnding(targetSummary.getEnding() + change);
		}

		// 조회 기간 데이터는 시간 기준으로 정렬 후 Group에 맞춰 account별 변동 데이터 작성. 
		periodChangerList.sort(Comparator.comparing(AccountChanger::occurrenceDttm).thenComparing(AccountChanger::occurrenceId));
		ArrayList<BalanceChangeGroup> changeGroupList = new ArrayList<>();
		HashSet<Integer> changedTargetIdSet = new HashSet<>();
		BalanceChangeGroup nGroup = null;
		TreeMap<Integer, BalanceChange> nChangeMap = null;
		for( AccountChanger changer : periodChangerList ) {
			Target target = changer.target(targetCls);
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
				changeData = new BalanceChange(target.accountId(), target.accountName(), targetSummary.getEnding(), 0, 0);
				nChangeMap.put(target.accountId(), changeData);
			}
			if( change > 0 ) {
				targetSummary.setIncrease(targetSummary.getIncrease() + change);
				changeData.setIncrease(changeData.getIncrease() + change);
			} else {
				targetSummary.setDecrease(targetSummary.getDecrease() - change);
				changeData.setDecrease(changeData.getDecrease() - change);
			}
			targetSummary.setEnding(targetSummary.getEnding() + change);
			changeData.setEnding(changeData.getEnding() + change);
		}
		if( null != nGroup ) {
			nGroup.setBalanceChangeList(new ArrayList<BalanceChange>(nChangeMap.values()));
		}
		
		// 변동도 없고 잔액도 없는 Account는 결과에서 제거.
		ArrayList<Integer> removeAccountIdList = new ArrayList<>();
		for( BalanceChange data : targetSummaryMap.values() ) {
			if( !changedTargetIdSet.contains(data.getAccountId()) && 0 == data.getBeginning() && 0 == data.getIncrease() && 0 == data.getDecrease() ) {
				removeAccountIdList.add(data.getAccountId());
			}
		}
		for( Integer accountId : removeAccountIdList ) {
			targetSummaryMap.remove(accountId);
		}
		

		BalanceChangeGroup summaryGroup = new BalanceChangeGroup();
		summaryGroup.setId("summary");
		summaryGroup.setDescription("summary");
		summaryGroup.setDate(fromDate);
		summaryGroup.setBalanceChangeList(new ArrayList<BalanceChange>(targetSummaryMap.values()));
		
		// 결과 리턴.
		AccountHistory result = new AccountHistory();
		result.setFromDate(fromDate);
		result.setToDate(toDate);
		result.setGroupType(groupType);
		result.setBalanceChangeSummary(summaryGroup);
		result.setBalanceChangeGroupList(changeGroupList);
		return result;
	}
}
