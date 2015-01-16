package huck.hestia.controller.accountbook;

import huck.hestia.HestiaController;
import huck.hestia.HttpUtil;
import huck.hestia.RequestPath;
import huck.hestia.VelocityRenderer;
import huck.hestia.VelocityRenderer.ActionFunction;
import huck.hestia.db.Asset;
import huck.hestia.db.Credit;
import huck.hestia.db.Debit;
import huck.hestia.db.HestiaDB;
import huck.hestia.db.Slip;
import huck.hestia.history.AccountHistory;
import huck.hestia.history.AccountHistory.GroupType;
import huck.hestia.history.BalanceChangeGroup;
import huck.hestia.history.BalanceChanger;
import huck.hestia.history.HistoryGenerator;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
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
			return redirectTo(matchPath + "assets");
		}
		ActionFunction actionFunction;
		switch( path.get(0) ) {
		case "assets": actionFunction = this::assets; break;
		case "asset/": actionFunction =  this::asset; break;
		case "slip": actionFunction =  this::slip; break;
		default: actionFunction = null;
		}
		if( null == actionFunction ) {
			notFound(req);
		}
		return renderer.render(req, actionFunction);
		
	}
	
	private String assets(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		return assetHistory(req, valueMap, a->true);
	}
	private String asset(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		RequestPath path = (RequestPath)req.getAttribute("path");
		int assetId = path.getInt(1, Integer.MIN_VALUE);
		if( !db.retrieveAssetList(a->a.id()==assetId).isEmpty() ) {
			return assetHistory(req, valueMap, a->a.id()==assetId);	
		}
		notFound(req);
		return null;
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
	
	private String assetHistory(HttpRequest req, HashMap<String, Object> valueMap, Predicate<Asset> assetPredicate) throws Exception {
		LocalDate fromDate = HttpUtil.getLocalDate(req, "from", false);
		LocalDate toDate = HttpUtil.getLocalDate(req, "to", false);
		LocalDate today = LocalDate.now();
		String groupTypeStr = HttpUtil.getString(req, "group", false);
		GroupType groupType = GroupType.OCCURRENCE;
		try {
			groupType = GroupType.valueOf(groupTypeStr);
		} catch( Exception ignore ) {
		}
		
		//  기준 되는 LocalDateTime 생성.
		if( null == toDate ) {
			toDate = today;
		}
		if( null == fromDate ) {
			if( groupType == GroupType.MONTH ) {
				LocalDate a = toDate.minusMonths(3);
				LocalDate b = today.withDayOfYear(1);
				fromDate = a.isBefore(b) ? a : b;
			} else {
				LocalDate a = toDate.minusDays(5);
				LocalDate b = today.withDayOfMonth(1);
				fromDate = a.isBefore(b) ? a : b;
			}
			
		}
		if( fromDate.isAfter(toDate) ) {
			LocalDate tmp = fromDate;
			fromDate = toDate;
			toDate = tmp;
		}
		
		ArrayList<BalanceChanger> allBalanceChangerList = new ArrayList<>();
		allBalanceChangerList.addAll(db.retrieveCreditList(null));
		allBalanceChangerList.addAll(db.retrieveDebitList(null));			
		AccountHistory history = HistoryGenerator.createAccountHistory(fromDate, toDate, groupType, allBalanceChangerList, Asset.class, assetPredicate);
		HashMap<LocalDate, AtomicInteger> dateGroupCountMap = new HashMap<>();
		for( BalanceChangeGroup groupData : history.getBalanceChangeGroupList() ) {
			dateGroupCountMap.putIfAbsent(groupData.getDate(), new AtomicInteger(0));
			AtomicInteger cnt = dateGroupCountMap.get(groupData.getDate());
			cnt.addAndGet(groupData.getBalanceChangeList().size());
		}
		
		valueMap.put("history", history);
		valueMap.put("dateGroupCountMap", dateGroupCountMap);
		return "/account_book/asset_history.html";
	}
}

