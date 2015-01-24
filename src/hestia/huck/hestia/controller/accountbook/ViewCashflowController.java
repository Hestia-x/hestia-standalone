package huck.hestia.controller.accountbook;

import huck.hestia.HestiaController;
import huck.hestia.RequestPath;
import huck.hestia.VelocityRenderer;
import huck.hestia.VelocityRenderer.ActionFunction;
import huck.hestia.db.Credit;
import huck.hestia.db.CreditCode;
import huck.hestia.db.Debit;
import huck.hestia.db.DebitCode;
import huck.hestia.db.HestiaDB;
import huck.hestia.history.AccountHistory;
import huck.hestia.history.AccountHistory.GroupType;
import huck.hestia.history.BalanceChangeGroup;
import huck.hestia.history.HistoryGenerator;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewCashflowController implements HestiaController {
	private HestiaDB db;
	private VelocityRenderer renderer;
	
	public ViewCashflowController(HestiaDB db, VelocityRenderer renderer) throws IOException {
		this.db = db;
		this.renderer = renderer;
	}
	
	@Override
	public HttpResponse controll(HttpRequest req, String matchPath) throws Exception {
		RequestPath path = new RequestPath(req.getRequestPath().substring(matchPath.length()));
		req.setAttribute("path", path);
		ActionFunction actionFunction;
		switch( path.size() ) {
		case 0: actionFunction = this::cashflowMain; break;
		case 1: actionFunction = this::cashflowMonthly; break;
		case 3: actionFunction = this::cashflowMonthlyDetail; break;
		default: actionFunction = null;
		}
		if( null == actionFunction ) {
			notFound(req);
		}
		return renderer.render(req, actionFunction);
	}
	
	private String cashflowMain(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		LocalDateTime fromDttm = LocalDateTime.MIN;
		LocalDateTime toDttm = LocalDateTime.MAX;
		LocalDate fromDate = fromDttm.toLocalDate().plusDays(1);
		LocalDate toDate = toDttm.toLocalDate().minusDays(1);
		
		List<Debit> debitList = db.retrieveDebitList(a -> a.occurrenceDttm().isBefore(toDttm)&&a.occurrenceDttm().isAfter(fromDttm));
		List<Credit> creditList = db.retrieveCreditList(a -> a.occurrenceDttm().isBefore(toDttm)&&a.occurrenceDttm().isAfter(fromDttm));
		AccountHistory income = HistoryGenerator.createAccountHistory(fromDate, toDate, GroupType.MONTH, creditList, CreditCode.class, a->null==a.asset());
		AccountHistory outcome = HistoryGenerator.createAccountHistory(fromDate, toDate, GroupType.MONTH, debitList, DebitCode.class, a->null==a.asset());
		
		HashMap<String, HashMap<String, Integer>> result = new HashMap<>();
		for( BalanceChangeGroup group : income.getBalanceChangeGroupList() ) {
			String month = group.getDate().format(DateTimeFormatter.ofPattern("uuuu-MM"));
			int value = 0-group.getSummary().getChange();
			HashMap<String, Integer> map = new HashMap<>();
			map.put("income", value);
			result.put(month, map);
		}
		for( BalanceChangeGroup group : outcome.getBalanceChangeGroupList() ) {
			String month = group.getDate().format(DateTimeFormatter.ofPattern("uuuu-MM"));
			int value = group.getSummary().getChange();
			HashMap<String, Integer> map = result.get(month);
			if( null == map ) {
				map = new HashMap<>();
				map.put("income", 0);
				result.put(month, map);	
			}
			map.put("outcome", value);
		}
		for( HashMap<String,Integer> data : result.values() ) {
			data.put("sum", data.get("income")-data.get("outcome"));
		}
		valueMap.put("result", result);
		return "/account_book/cashflow.html";
	}
	private String cashflowMonthly(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		RequestPath path = (RequestPath)req.getAttribute("path");
		String yearMonth = path.get(0);
		LocalDate beginDate = null;
		try {
			beginDate = LocalDate.parse(yearMonth.replace('/', '-') + "01", DateTimeFormatter.ISO_DATE);
		} catch( DateTimeParseException ex ) {
			notFound(req);
		}
		LocalDate endDate = beginDate.plusMonths(1).minusDays(1);
		LocalDateTime fromDttm = beginDate.atStartOfDay().minusNanos(1);
		LocalDateTime toDttm = endDate.plusDays(1).atStartOfDay();
		
		List<Debit> debitList = db.retrieveDebitList(a -> a.occurrenceDttm().isBefore(toDttm)&&a.occurrenceDttm().isAfter(fromDttm));
		List<Credit> creditList = db.retrieveCreditList(a -> a.occurrenceDttm().isBefore(toDttm)&&a.occurrenceDttm().isAfter(fromDttm));
		BalanceChangeGroup income = HistoryGenerator.createAccountHistory(beginDate, endDate, GroupType.MONTH, creditList, CreditCode.class, a->null==a.asset()).getBalanceChangeSummary();
		BalanceChangeGroup outcome = HistoryGenerator.createAccountHistory(beginDate, endDate, GroupType.MONTH, debitList, DebitCode.class, a->null==a.asset()).getBalanceChangeSummary();
		valueMap.put("from", beginDate);
		valueMap.put("to", endDate);
		valueMap.put("income", income);
		valueMap.put("outcome", outcome);
		return "/account_book/cashflow_monthly.html";
	}
	
	private String cashflowMonthlyDetail(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		RequestPath path = (RequestPath)req.getAttribute("path");
		String yearMonth = path.get(0);
		LocalDate beginDate = null;
		try {
			beginDate = LocalDate.parse(yearMonth.replace('/', '-') + "01", DateTimeFormatter.ISO_DATE);
		} catch( DateTimeParseException ex ) {
			notFound(req);
		}
		LocalDate endDate = beginDate.plusMonths(1).minusDays(1);
		LocalDateTime fromDttm = beginDate.atStartOfDay().minusNanos(1);
		LocalDateTime toDttm = endDate.plusDays(1).atStartOfDay();
		
		String id = path.get(2);
		AccountHistory history = null;
		switch(path.get(1)) {
		case "debit/":
			List<Debit> debitList = db.retrieveDebitList(a -> a.occurrenceDttm().isBefore(toDttm)&&a.occurrenceDttm().isAfter(fromDttm));
			history = HistoryGenerator.createAccountHistory(beginDate, endDate, GroupType.OCCURRENCE, debitList, DebitCode.class, a->id.equals(""+a.id()));
			break;
		case "credit/":
			List<Credit> creditList = db.retrieveCreditList(a -> a.occurrenceDttm().isBefore(toDttm)&&a.occurrenceDttm().isAfter(fromDttm));
			history = HistoryGenerator.createAccountHistory(beginDate, endDate, GroupType.OCCURRENCE, creditList, CreditCode.class, a->id.equals(""+a.id()));
			break;
		default:
			notFound(req);
		}
		HashMap<LocalDate, AtomicInteger> dateGroupCountMap = new HashMap<>();
		for( BalanceChangeGroup groupData : history.getBalanceChangeGroupList() ) {
			dateGroupCountMap.putIfAbsent(groupData.getDate(), new AtomicInteger(0));
			AtomicInteger cnt = dateGroupCountMap.get(groupData.getDate());
			cnt.addAndGet(groupData.getBalanceChangeList().size());
		}
		valueMap.put("from", beginDate);
		valueMap.put("to", endDate);
		valueMap.put("history", history);
		valueMap.put("dateGroupCountMap", dateGroupCountMap);
		return "/account_book/cashflow_monthly_detail.html";
	}
}

