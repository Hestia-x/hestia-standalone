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
import huck.hestia.history.BalanceChangeGroup;
import huck.hestia.history.HistoryGenerator;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

public class SlipController implements HestiaController {
	private HestiaDB db;
	private VelocityRenderer renderer;
	
	public SlipController(HestiaDB db, VelocityRenderer renderer) throws IOException {
		this.db = db;
		this.renderer = renderer;
	}
	
	@Override
	public HttpResponse controll(HttpRequest req, String matchPath) throws Exception {
		RequestPath path = new RequestPath(req.getRequestPath().substring(matchPath.length()));
		req.setAttribute("path", path);
		ActionFunction actionFunction;
		switch( path.size() ) {
		case 0: actionFunction = this::slipMain; break;
		case 1: actionFunction = this::slipDetail; break;
		default: actionFunction = null;
		}
		if( null == actionFunction ) {
			notFound(req);
		}
		return renderer.render(req, actionFunction);
	}
	
	private String slipMain(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
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
	private String slipDetail(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		RequestPath path = (RequestPath)req.getAttribute("path");
		Integer slipId = path.getInt(0, Integer.MIN_VALUE);
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
		return "/account_book/slip_detail.html";
	}
}

