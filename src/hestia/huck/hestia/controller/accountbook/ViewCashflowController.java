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
import huck.hestia.history.BalanceChangeGroup;
import huck.hestia.history.AccountHistory.GroupType;
import huck.hestia.history.HistoryGenerator;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;
import java.time.LocalDate;
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
		return null;
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
		List<Debit> debitList = db.retrieveDebitList(null);
		List<Credit> creditList = db.retrieveCreditList(null);
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
		
		String id = path.get(2);
		AccountHistory history = null;
		switch(path.get(1)) {
		case "debit/":
			List<Debit> debitList = db.retrieveDebitList(null);
			history = HistoryGenerator.createAccountHistory(beginDate, endDate, GroupType.OCCURRENCE, debitList, DebitCode.class, a->id.equals(""+a.id()));
			break;
		case "credit/":
			List<Credit> creditList = db.retrieveCreditList(null);
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

