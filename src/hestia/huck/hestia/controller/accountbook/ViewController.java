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
import huck.hestia.db.Slip;
import huck.hestia.history.AccountHistory;
import huck.hestia.history.AccountHistory.GroupType;
import huck.hestia.history.HistoryGenerator;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

public class ViewController implements HestiaController {
	private HestiaDB db;
	private VelocityRenderer renderer;
	
	public ViewController(HestiaDB db, VelocityRenderer renderer) throws IOException {
		this.db = db;
		this.renderer = renderer;
	}
	
	@Override
	public HttpResponse controll(HttpRequest req, String matchPath) throws Exception {
		RequestPath path = new RequestPath(req.getRequestPath().substring(matchPath.length()));
		req.setAttribute("path", path);
		if( 0 == path.size() ) {
			return redirectTo(matchPath + "asset/");
		}
		ActionFunction actionFunction;
		switch( path.get(0) ) {
		case "slip/": actionFunction =  this::slip; break;
		case "cashflow": actionFunction =  this::cashflowMain; break;
		case "cashflow/": actionFunction =  this::cashflowDetail; break;
		default: actionFunction = null;
		}
		if( null == actionFunction ) {
			notFound(req);
		}
		return renderer.render(req, actionFunction);
		
	}
	
	private String slip(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		RequestPath path = (RequestPath)req.getAttribute("path");
		Integer slipId = path.getInt(1, Integer.MIN_VALUE);
		List<Slip> slipList = db.retrieveSlipList(a->a.id()==slipId);
		if( slipList.isEmpty() ) {
			notFound(req);
		}
		List<Debit> debitList = db.retrieveDebitList(a->a.slip().id()==slipId);
		List<Credit> creditList = db.retrieveCreditList(a->a.slip().id()==slipId);
		IntSummaryStatistics debitSummary = debitList.stream().collect(Collectors.summarizingInt(a->a.unitPrice()*a.quantity()));
		IntSummaryStatistics creditSummary = creditList.stream().collect(Collectors.summarizingInt(a->a.price()));
		valueMap.put("slip", slipList.get(0));
		valueMap.put("debitList", debitList);
		valueMap.put("creditList", creditList);
		valueMap.put("debitSummary", debitSummary);
		valueMap.put("creditSummary", creditSummary);
		return "/account_book/slip.html";
	}
	private String cashflowMain(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		return null;
	}
	private String cashflowDetail(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		RequestPath path = (RequestPath)req.getAttribute("path");
		String yearMonth = path.get(1);
		LocalDate beginDate = null;
		try {
			beginDate = LocalDate.parse(yearMonth.replace('_', '-') + "-01", DateTimeFormatter.ISO_DATE);
		} catch( DateTimeParseException ex ) {
			notFound(req);
		}
		LocalDate endDate = beginDate.plusMonths(1).minusDays(1);
		List<Debit> debitList = db.retrieveDebitList(null);
		List<Credit> creditList = db.retrieveCreditList(null);
		AccountHistory income = HistoryGenerator.createAccountHistory(beginDate, endDate, GroupType.MONTH, creditList, CreditCode.class, a->null!=a.asset());
		AccountHistory outcome = HistoryGenerator.createAccountHistory(beginDate, endDate, GroupType.MONTH, debitList, DebitCode.class, a->null!=a.asset());
		valueMap.put("income", income);
		valueMap.put("outcome", outcome);
		return "/account_book/cashflow.html";
	}
}

